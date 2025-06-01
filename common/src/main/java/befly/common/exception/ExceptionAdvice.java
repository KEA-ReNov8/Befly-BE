package befly.common.exception;

import befly.common.apiPayload.ApiResponse;
import befly.common.code.BaseCodeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static befly.common.code.status.GlobalErrorStatus.INTERNAL_SERVER_ERROR;


@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    //RestApi 에러
    @ExceptionHandler(value = RestApiException.class)
    public ResponseEntity<ApiResponse<String>> handleRestApiException(RestApiException e) {
        BaseCodeDto errorCode = e.getErrorCode();
        return handleExceptionInternal(errorCode);
    }

    //500에러
    /*@ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<String>> handleInternalException(Exception e) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.onFailure(INTERNAL_SERVER_ERROR.getCode().code(),
                        INTERNAL_SERVER_ERROR.getMessage(), null));
    }*/

    private ResponseEntity<ApiResponse<String>> handleExceptionInternal(BaseCodeDto errorCode) {
        return ResponseEntity
                .status(errorCode.httpStatus().value())
                .body(ApiResponse.onFailure(errorCode.code(), errorCode.message(), null));
    }
}
