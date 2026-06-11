package cc.backend.amateurShow.service;

import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.enums.ApprovalStatus;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.amateurShow.service.amateurShowService.AmateurServiceImpl;
import cc.backend.config.s3.S3Service;
import cc.backend.image.FilePath;
import cc.backend.image.entity.Image;
import cc.backend.image.repository.ImageRepository;
import cc.backend.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmateurServiceImplTest {

    @Mock
    private AmateurShowRepository amateurShowRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private AmateurServiceImpl amateurService;

    @Test
    void getAmateurShowReturnsPresignedPosterUrl() {
        AmateurShow show = approvedShow();
        Image poster = posterImage();
        String presignedUrl = "https://signed.example/poster";

        when(amateurShowRepository.findById(19L)).thenReturn(Optional.of(show));
        when(imageRepository.findByFilePathAndContentId(FilePath.amateurShow, 19L))
                .thenReturn(poster);
        when(s3Service.createPresignedGetUrl(poster.getKeyName())).thenReturn(presignedUrl);

        AmateurShowResponseDTO.AmateurShowResult result = amateurService.getAmateurShow(19L);

        assertEquals(presignedUrl, result.getPosterImageUrl());
        verify(s3Service).createPresignedGetUrl("amateurShow/poster.jpeg");
    }

    @Test
    void getCreatedShowReturnsPresignedPosterUrl() {
        AmateurShow show = approvedShow();
        Image poster = posterImage();

        when(amateurShowRepository.findByIdAndMemberId(19L, 32L)).thenReturn(Optional.of(show));
        when(imageRepository.findByFilePathAndContentId(FilePath.amateurShow, 19L))
                .thenReturn(poster);
        when(s3Service.createPresignedGetUrl(poster.getKeyName()))
                .thenReturn("https://signed.example/owner-poster");

        AmateurShowResponseDTO.AmateurShowResult result = amateurService.getCreatedShow(32L, 19L);

        assertEquals("https://signed.example/owner-poster", result.getPosterImageUrl());
    }

    @Test
    void getAmateurShowKeepsStoredUrlWhenPosterImageRecordIsMissing() {
        AmateurShow show = approvedShow();

        when(amateurShowRepository.findById(19L)).thenReturn(Optional.of(show));
        when(imageRepository.findByFilePathAndContentId(FilePath.amateurShow, 19L))
                .thenReturn(null);

        AmateurShowResponseDTO.AmateurShowResult result = amateurService.getAmateurShow(19L);

        assertEquals("https://static.example/poster.jpeg", result.getPosterImageUrl());
    }

    @Test
    void getAmateurShowKeepsStoredUrlWhenPosterImageKeyNameIsNull() {
        AmateurShow show = approvedShow();
        Image poster = posterImage(null);

        when(amateurShowRepository.findById(19L)).thenReturn(Optional.of(show));
        when(imageRepository.findByFilePathAndContentId(FilePath.amateurShow, 19L))
                .thenReturn(poster);

        AmateurShowResponseDTO.AmateurShowResult result = amateurService.getAmateurShow(19L);

        assertEquals("https://static.example/poster.jpeg", result.getPosterImageUrl());
        verifyNoInteractions(s3Service);
    }

    @Test
    void getAmateurShowKeepsStoredUrlWhenPosterImageKeyNameIsBlank() {
        AmateurShow show = approvedShow();
        Image poster = posterImage("   ");

        when(amateurShowRepository.findById(19L)).thenReturn(Optional.of(show));
        when(imageRepository.findByFilePathAndContentId(FilePath.amateurShow, 19L))
                .thenReturn(poster);

        AmateurShowResponseDTO.AmateurShowResult result = amateurService.getAmateurShow(19L);

        assertEquals("https://static.example/poster.jpeg", result.getPosterImageUrl());
        verifyNoInteractions(s3Service);
    }

    @Test
    void getAmateurShowKeepsStoredUrlWhenPresignedUrlCreationFails() {
        AmateurShow show = approvedShow();
        Image poster = posterImage();

        when(amateurShowRepository.findById(19L)).thenReturn(Optional.of(show));
        when(imageRepository.findByFilePathAndContentId(FilePath.amateurShow, 19L))
                .thenReturn(poster);
        when(s3Service.createPresignedGetUrl(poster.getKeyName()))
                .thenThrow(new IllegalStateException("S3 unavailable"));

        AmateurShowResponseDTO.AmateurShowResult result = amateurService.getAmateurShow(19L);

        assertEquals("https://static.example/poster.jpeg", result.getPosterImageUrl());
    }

    private AmateurShow approvedShow() {
        Member member = Member.builder().email("performer@example.test").build();
        ReflectionTestUtils.setField(member, "id", 32L);
        return AmateurShow.builder()
                .id(19L)
                .member(member)
                .name("Test Show")
                .start(LocalDate.of(2026, 6, 13))
                .end(LocalDate.of(2026, 6, 14))
                .runtime(90)
                .posterImageUrl("https://static.example/poster.jpeg")
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();
    }

    private Image posterImage() {
        return posterImage("amateurShow/poster.jpeg");
    }

    private Image posterImage(String keyName) {
        return Image.builder()
                .id(55L)
                .keyName(keyName)
                .filePath(FilePath.amateurShow)
                .contentId(19L)
                .memberId(32L)
                .build();
    }
}
