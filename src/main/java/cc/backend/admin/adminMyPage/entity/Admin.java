//package cc.backend.admin.adminMyPage.entity;
//
//import cc.backend.domain.common.BaseEntity;
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Getter
//@Builder
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor(access = AccessLevel.PROTECTED)
//public class Admin extends BaseEntity {
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(unique = true, nullable = false)
//    private String adminId;
//
//    @Column(nullable = false)
//    private String password;
//
//    private String accountNumber;
//
//    // 비번 변경 메서드
//    public void updatePassword(String newHashedPassword) {
//        this.password = newHashedPassword;
//    }
//}
