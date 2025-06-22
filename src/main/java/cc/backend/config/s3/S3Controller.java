package cc.backend.config.s3;

import cc.backend.image.FilePath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@Tag(name = "S3 접근 Presigned Url 요청 api")
@RestController
@RequiredArgsConstructor
@RequestMapping("/upload/s3")
public class S3Controller {
    private final S3Service s3Service;

    /**
     * presigned URL을 생성하여 반환
     * @param imageExtension 확장자 (예: jpg, png 등)
     * @return 업로드 URL과 public URL
     */
    @Operation(summary = "Image 한 개 업로드용 url 요청 API", description = "image 하나 = url 하나 필요")
    @GetMapping("/presignedUrl")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@Parameter(description = "업로드할 이미지 파일의 확장자(jpg 또는 jpeg)", required = true) String imageExtension ,
                                                               @Parameter(description = "이미지 업로드하는 기능(board, photoAlbum)", required = true)FilePath filePath) {
        Map<String, String> urlMap = s3Service.createPresignedUrl(imageExtension, filePath);
        return ResponseEntity.ok(urlMap);
    }

    /**
     * 다중 presigned URL 생성 요청
     * @param extensions 이미지 확장자 리스트 (예: ["png", "jpg"])
     * @return List<Map<String, String>> 각 객체는 keyName, uploadUrl, publicUrl 포함
     */
    @PostMapping("/presignedUrls")
    @Operation(summary = "Image 여러 개 업로드용 url 요청 API", description = "업로드할 image 개수 만큼 url 필요")
    public ResponseEntity<List<Map<String, String>>> getPresignedUrls (@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "각 이미지의 확장자 jpg,jpeg,jpg ...를 따옴표 안에 나열", required = true)
                                                                           @RequestBody List<@NotBlank String> extensions,
                                                                       @Parameter(description = "이미지 업로드하는 기능(board, photoAlbum)", required = true)FilePath filePath) {

        if (extensions == null || extensions.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Map<String, String>> urls = s3Service.createPresignedUrls(extensions, filePath);
        return ResponseEntity.ok(urls);
    }
}
