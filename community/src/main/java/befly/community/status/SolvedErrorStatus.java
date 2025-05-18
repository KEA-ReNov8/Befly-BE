package befly.community.status;

import befly.common.code.BaseCodeDto;
import befly.common.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SolvedErrorStatus implements BaseCodeInterface {
    POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "SOLVED001", "잘못된 해결함/공유함 ID입니다."),
    COMMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "SOLVED002", "잘못된 해결함/공유함 댓글 ID입니다."),
    NO_PERMISSION(HttpStatus.BAD_REQUEST, "SOLVED003", "권한이 없습니다."),
    ALREADY_EMPATHIZED(HttpStatus.BAD_REQUEST, "SOLVED004", "이미 해결함/공유함 공감을 눌렀습니다."),
    EMPATHY_NOT_FOUND(HttpStatus.BAD_REQUEST, "SOLVED005", "아직 해결함/공유함 공감을 누르지 않았습니다.")
    ;

    private final HttpStatus httpStatus;
    private final boolean isSuccess = false;
    private final String code;
    private final String message;

    @Override
    public BaseCodeDto getCode() {
        return BaseCodeDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(isSuccess)
                .code(code)
                .message(message)
                .build();
    }
}
