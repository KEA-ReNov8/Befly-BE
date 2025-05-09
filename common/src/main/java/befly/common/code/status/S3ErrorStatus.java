package befly.common.code.status;

import befly.common.code.BaseCodeDto;
import befly.common.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
    public enum S3ErrorStatus implements BaseCodeInterface {
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "S3400", "잘못된 파일 확장자 입니다."),
    INVALID_S3_METHOD(HttpStatus.BAD_REQUEST, "S3400", "잘못된 요청 메서드 입니다.")
    ;
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

