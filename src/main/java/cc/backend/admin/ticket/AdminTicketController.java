package cc.backend.admin.ticket;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "관리자 소극장 티켓 관리")
@RequestMapping("/admin/ticket")
public class AdminTicketController {

    private final AdminTicketService adminTicketService;



}
