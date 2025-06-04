package befly.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NickNameResponse {

    private LocalDateTime timestamp;
    private String code;
    private int status;
    private String message;
    private String error;
    private String result;
    private String path;

}

/*
"timestamp": "2025-06-04T14:47:51.591717",
    "code": "COMMON200",
    "message": "성공입니다.",
    "result": "테스터"
 */

/*
{
    "timestamp": "2025-06-04T05:48:08.220+00:00",
    "status": 500,
    "error": "Internal Server Error",
    "path": "/user/getNickname/134"
}
 */