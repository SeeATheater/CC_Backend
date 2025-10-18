package cc.backend.member.entity;

import cc.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Account extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String bankName;

    @Column(nullable = false, length = 50)
    private String accountNumber;

    @Column(nullable = false, length = 50)
    private String accountOwner;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    public void update(String bankName, String accountNumber, String accountOwner) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountOwner = accountOwner;
    }
}

