package befly.community.status;

import befly.common.code.BaseCodeDto;
import befly.common.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FreeErrorStatus implements BaseCodeInterface {
    POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "FREE001", "잘못된 자유함 ID입니다."),
    COMMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "FREE002", "잘못된 자유함 댓글 ID입니다."),
    NO_PERMISSION(HttpStatus.BAD_REQUEST, "FREE003", "권한이 없습니다."),
    ALREADY_EMPATHIZED(HttpStatus.BAD_REQUEST, "FREE004", "이미 공감을 눌렀습니다."),
    EMPATHY_NOT_FOUND(HttpStatus.BAD_REQUEST, "FREE005", "아직 공감을 누르지 않았습니다."),
    NICKNAME_NOT_FOUND(HttpStatus.BAD_REQUEST, "FREE006", "닉네임을 찾지 못했습니다.")
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
