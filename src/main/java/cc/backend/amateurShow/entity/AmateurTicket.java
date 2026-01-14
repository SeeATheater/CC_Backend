package cc.backend.amateurShow.entity;

import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import cc.backend.domain.common.BaseEntity;
import cc.backend.ticket.entity.TempTicket;
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
    @Builder.Default
    private List<TempTicket> tempTicketList = new ArrayList<>();

    public void update(AmateurUpdateRequestDTO.UpdateTickets dto) {
        if (dto.getDiscountName() != null) this.discountName = dto.getDiscountName();
        if (dto.getPrice() != null) this.price = dto.getPrice();
    }
}
