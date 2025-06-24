package cc.backend.config.s3;

import cc.backend.image.FilePath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;

    private final S3Client s3Client;

    private static final String BUCKET_DOMAIN = "https://ccbucket-0528.s3.ap-northeast-2.amazonaws.com/";

    @Value("${AWS_S3_BUCKET}")
    private String bucket2;

    /**
     * presigned url을 생성해 주는 메소드. bucket v3에 생성해 줌
     * @param imageExtension 이미지의 확장자
     * @return upload url, public url
     */
    //단일 객체 용 url - 사진 하나 올리기용
    public Map<String, String> createPresignedUrl(String imageExtension, FilePath filePath) {

        String keyName = UUID.randomUUID() + "." + imageExtension;
        keyName = filePath + "/" +  keyName.replace("-", "");
        String contentType = "image/" + imageExtension;
        Map<String, String> metadata = Map.of(
                "fileType", contentType,
                "Content-Type", contentType
        );

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket2)
                .key(keyName)
                .metadata(metadata)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))  // The URL expires in 10 minutes.
                .putObjectRequest(objectRequest)
                .build();


        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String myURL = presignedRequest.url().toString();
        myURL = myURL.replace(BUCKET_DOMAIN, "");
        String publicUrl = BUCKET_DOMAIN + keyName;
        log.info("Presigned URL to upload a file to: {}", myURL);
        log.info("HTTP method: {}", presignedRequest.httpRequest().method());

        Map<String, String> map = new ConcurrentHashMap<>();
        map.put("uploadUrl", myURL);
        map.put("publicUrl", publicUrl);
        map.put("keyName", keyName);

        return map;
    }

    public void deleteFile(String keyName) {
        if(doesObjectExist(keyName)) {
            try {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucket2)
                        .key(keyName)
                        .build();

                s3Client.deleteObject(deleteObjectRequest);
                log.info("Deleted file from S3: {}", keyName);
            } catch (Exception e) {
                log.error("Failed to delete file from S3: {}", keyName, e);
                throw new RuntimeException("파일 삭제 실패: " + e.getMessage());
            }

        }
        throw new RuntimeException("해당 파일이 S3에 존재하지 않음");

    }

    /**
     * 다중 presigned URL 생성
     * @param extensions 이미지 확장자 리스트 (ex: ["png", "jpg", "jpeg"])
     * @return keyName과 uploadUrl, publicUrl 리스트 반환
     */
    public List<Map<String, String>> createPresignedUrls(List<String> extensions, FilePath filePath) {
        return extensions.stream()
                .map(ext -> createPresignedUrl(ext, filePath))
                .collect(Collectors.toList());
    }


    public boolean doesObjectExist(String keyName) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket2)
                    .key(keyName)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

}
