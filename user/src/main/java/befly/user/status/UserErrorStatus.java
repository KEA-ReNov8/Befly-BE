package befly.user.status;

import befly.common.code.BaseCodeDto;
import befly.common.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseCodeInterface {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버내부 오류입니다. 서버 담당자에게 문의하세요."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "MEMBER001", "중복된 닉네임입니다."),
    DUPLICATE_ClIENT_ID(HttpStatus.BAD_REQUEST, "MEMBER002", "이미 가입된 아이디입니다."),
    PWD_INVALID(HttpStatus.BAD_REQUEST, "AUTH004", "비밀번호가 틀립니다."),
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER", "잘못된 ID입니다."),

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
