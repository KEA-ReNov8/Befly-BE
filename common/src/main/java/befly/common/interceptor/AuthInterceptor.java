package befly.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/*
    Gateway에서 인증되어 들어온 요청에는 X-USER-ID가 있음
    그것을 ArgumentResolver에서 사용할 수 있도록 추출

    로그인이 필요한 서비스에 한에 적용 되어야함.
 */
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        log.info("AuthInterceptor preHandle");
        try {
            if ("GET".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/community/") && (!request.getRequestURI().startsWith("/community/notification") && !request.getRequestURI().contains("/check"))) {
                return true;
            }
            String userId = request.getHeader("X-USER-ID");
            request.setAttribute("userId", Long.parseLong(userId)); // ArgumentResolver에서 꺼내 쓸 수 있게 저장
            log.info("인터셉터 X-USER-ID: {} 처리 완료", request.getHeader("X-USER-ID"));
        } catch (NumberFormatException e) {
            log.error("Invalid or missing X-USER-ID header");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing X-USER-ID header");
//            에러 페이지 RETURN 어떻게 할 것인지 -> 추후 확인좀
            return false;
        }
        return true;
    }
}