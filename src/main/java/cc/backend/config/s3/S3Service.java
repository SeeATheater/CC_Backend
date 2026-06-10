package cc.backend.config.s3;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.image.FilePath;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;

    private final S3Client s3Client;
    private final MemberRepository memberRepository;

    @Value("${AWS_BUCKET_DOMAIN}")
    private String BUCKET_DOMAIN;

    @Value("${AWS_S3_BUCKET}")
    private String bucketName;

    //파일 확장자 whitelist
    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "gif");

    /**
     * presigned url을 생성해 주는 메소드. bucket v3에 생성해 줌
     * @param imageExtension 이미지의 확장자
     * @return upload url, public url
     */
    //단일 객체 put용 url - 사진 하나 올리기용
    public Map<String, String> createPresignedPutUrl(String imageExtension, FilePath filePath, Long memberId) {

        memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        if (imageExtension == null || imageExtension.isBlank()) {
            throw new GeneralException(ErrorStatus.INVALID_FILE_EXTENSION);
        }

        String ext = imageExtension.toLowerCase();

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String keyName = filePath + "/" + uuid + "." + ext;

        // 파일 확장자 whitelist 검사
        if (!ALLOWED_EXT.contains(ext)) {
            throw new GeneralException(ErrorStatus.INVALID_FILE_EXTENSION);
        }

//        // MIME 타입 처리 (jpg는 image/jpeg)
//        String mimeType = switch (ext) {
//            case "jpg", "jpeg" -> "image/jpeg";
//            case "png" -> "image/png";
//            case "gif" -> "image/gif";
//            default -> throw new GeneralException(ErrorStatus.INVALID_FILE_EXTENSION);
//        };

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(objectRequest)
                        .build());

        String uploadUrl = presignedRequest.url().toString();
        String imageUrl = BUCKET_DOMAIN + keyName;

        log.info("Presigned URL to upload a file to: {}", uploadUrl);
        log.info("HTTP method: {}", presignedRequest.httpRequest().method());

        return Map.of(
                "uploadUrl", uploadUrl,
                "imageUrl", imageUrl,
                "keyName", keyName
        );
    }

    //단일 객체 get용 url - 사진 하나 조회용
    public String createPresignedGetUrl(String keyName, Long memberId) {

        memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        return createPresignedGetUrl(keyName);
    }

    // Public content remains private in S3; only a short-lived signed URL is exposed.
    public String createPresignedGetUrl(String keyName) {

        //예외처리 필요-추후개발(사진 업로드할때 아무것도 안넣으면 url 필드가 비어서 get할때도 계속 에러남 - 막아두거나 따로 처리해야할듯)
//        if (keyName == null || keyName.isBlank()) {
//            throw new GeneralException(ErrorStatus.INVALID_S3_KEY);
//        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(getObjectRequest)
                        .build());

        String getUrl = presignedRequest.url().toString();
        log.info("Presigned GET URL: {}", getUrl);

        return getUrl;
    }

    public Map<String, String> createPresignedGetUrls(List<String> keyNames, Long memberId) {

        memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        // key별 presigned URL 생성
        return keyNames.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> {    //key를  인자로 받아 presigned Url 반환하는 람다 함수
                            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(key)
                                    .build();

                            return s3Presigner.presignGetObject(
                                    GetObjectPresignRequest.builder()
                                            .signatureDuration(Duration.ofMinutes(10))
                                            .getObjectRequest(getObjectRequest)
                                            .build()
                            ).url().toString();
                        }
                ));
    }

    public void deleteFile(String keyName, Long memberId) {
        // 회원 검증
        memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        // S3에 존재하는지 확인
        if (!doesObjectExist(keyName, memberId)) {
            throw new GeneralException(ErrorStatus.NOT_FOUND_IN_S3);
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Deleted file from S3: {}", keyName);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", keyName, e);
            throw new RuntimeException("파일 삭제 실패: " + e.getMessage());
        }
    }

    /**
     * 다중 put용 presigned URL 생성
     * @param extensions 이미지 확장자 리스트 (ex: ["png", "jpg", "jpeg"])
     * @return keyName과 uploadUrl 리스트 반환
     */
    public List<Map<String, String>> createPresignedPutUrls(List<String> extensions, FilePath filePath, Long memberId) {
        return extensions.stream()
                .map(ext -> createPresignedPutUrl(ext, filePath, memberId))
                .collect(Collectors.toList());
    }

    public boolean doesObjectExist(String keyName, Long memberId) {

        memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        if (keyName == null || keyName.isBlank()) {
            throw new GeneralException(ErrorStatus.INVALID_S3_KEY);
        }

        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        try {
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            // 존재하지 않는 게 아니라 실제 장애이므로 로깅 후 재던짐
            log.error("Error checking S3 object: {}", e.awsErrorDetails().errorMessage());
            throw e;
        }
    }

}
