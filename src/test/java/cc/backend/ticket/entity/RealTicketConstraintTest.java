package cc.backend.ticket.entity;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RealTicketConstraintTest {

    @Test
    void realTicketDeclaresUniqueConstraintOnKakaoTid() {
        Table table = RealTicket.class.getAnnotation(Table.class);

        assertTrue(Arrays.stream(table.uniqueConstraints())
                .anyMatch(RealTicketConstraintTest::isKakaoTidUniqueConstraint));
    }

    private static boolean isKakaoTidUniqueConstraint(UniqueConstraint constraint) {
        return "uk_real_ticket_kakao_tid".equals(constraint.name())
                && Arrays.asList(constraint.columnNames()).contains("kakao_tid");
    }
}
