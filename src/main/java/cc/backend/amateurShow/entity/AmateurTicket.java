package cc.backend.amateurShow.entity;

import cc.backend.domain.common.BaseEntity;
import cc.backend.member.entity.MemberTicket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmateurTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String discountName;

    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "amateur_show_id")
    private AmateurShow amateurShow;

    @OneToMany(mappedBy = "amateurTicket", cascade = CascadeType.ALL)
    private List<MemberTicket> memberTicketList = new ArrayList<>();

}
