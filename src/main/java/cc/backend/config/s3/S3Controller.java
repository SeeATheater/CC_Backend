package cc.backend.config.s3;

import cc.backend.image.FilePath;
import cc.backend.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@Tag(name = "S3 접근 Presigned Url 요청 api")
@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
public class S3Controller {
    private final S3Service s3Service;

    /**
     * presigned URL을 생성하여 반환
     * @param imageExtension 확장자 (예: jpg, png 등)
     * @return 업로드 URL과 public URL
     */
    @Operation(summary = "Image 한 개 업로드용 url 요청 API", description = "image 하나 = url 하나 필요")
    @GetMapping("/uploadUrl")
    public ResponseEntity<Map<String, String>> getPresignedPutUrl(
            @Parameter(description = "업로드할 이미지 파일의 확장자(jpg, jpeg, png, gif)", required = true) @RequestParam @NotBlank String imageExtension,
            @Parameter(description = "업로드할 기능(board, photoAlbum, amateurShow)", required = true) @RequestParam FilePath filePath,
            @AuthenticationPrincipal(expression = "member") Member member) {

        return ResponseEntity.ok(s3Service.createPresignedPutUrl(imageExtension, filePath, member.getId()));
    }

    /**
     * 다중 presigned URL 생성 요청
     * @param extensions 이미지 확장자 리스트 (예: ["png", "jpg"])
     * @return List<Map<String, String>> 각 객체는 keyName, uploadUrl, publicUrl 포함
     */
    @PostMapping("/uploadUrls")
    @Operation(summary = "Image 여러 개 업로드용 url 요청 API", description = "업로드할 image 개수 만큼 url 필요")
    public ResponseEntity<List<Map<String, String>>> getPresignedPutUrls (
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "각 이미지의 확장자 jpg, jpeg, png, gif ...", required = true) @RequestBody List<@NotBlank String> extensions,
            @Parameter(description = "업로드할 기능(board, photoAlbum, amateurShow)", required = true) @RequestParam FilePath filePath,
            @AuthenticationPrincipal(expression = "member") Member member) {

        return ResponseEntity.ok(s3Service.createPresignedPutUrls(extensions, filePath, member.getId()));
    }

    @Operation(summary = "단일 파일 조회용 presigned URL", description = "keyName에 해당하는 파일 조회 URL 반환")
    @GetMapping("/getUrl")
    public ResponseEntity<String> getPresignedGetUrl(
            @Parameter(description = "S3에 저장된 파일 keyName", required = true) @RequestParam @NotBlank String keyName,
            @AuthenticationPrincipal(expression = "member") Member member) {

        String url = s3Service.createPresignedGetUrl(keyName, member.getId());
        return ResponseEntity.ok(url);
    }

    @Operation(summary = "다중 파일 조회용 presigned URL", description = "keyName 리스트에 해당하는 파일 조회 URL 리스트 반환")
    @PostMapping("/getUrls")
    public ResponseEntity<Map<String, String>> getPresignedGetUrls(
            @Parameter(description = "S3에 저장된 파일 keyName 리스트", required = true) @RequestBody List<@NotBlank String> keyNames,
            @AuthenticationPrincipal(expression = "member") Member member) {

        Map<String, String> urls = s3Service.createPresignedGetUrls(keyNames, member.getId());
        return ResponseEntity.ok(urls);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "keyName에 해당하는 파일 S3에서 삭제")
    public void deleteFile(@RequestParam String keyName,
                           @AuthenticationPrincipal(expression = "member") Member member){
        s3Service.deleteFile(keyName, member.getId());
    }

    @GetMapping("/exist")
    @Operation(summary = "keyName에 해당하는 파일이 S3에 실제 존재하는지 확인")
    public ResponseEntity<Boolean> doesObjectExist(@RequestParam String keyName,
                                                   @AuthenticationPrincipal(expression = "member") Member member){
        return ResponseEntity.ok(s3Service.doesObjectExist(keyName, member.getId()));
    }
}
