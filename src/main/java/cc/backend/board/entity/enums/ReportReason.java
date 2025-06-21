package cc.backend.board.entity.enums;

public enum ReportReason {

    INAPPROPRIATE("게시판 성격에 부적절함"),
    ABUSE("욕설/비하"),
    FRAUD("사칭/사기"),
    OBSCENE("음란물/불건전한 행위");

    private final String description;

    ReportReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
