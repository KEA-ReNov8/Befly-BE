package befly.common.config;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import befly.common.interceptor.AuthInterceptor;
import befly.common.resolver.LoginUserArgumentResolver;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("WebConfig.addInterceptors");
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/oauth2/**",
                        "/auth/signup",
                        "/auth/signin",
                        "/auth/refresh",
                        "/auth/email/duplication",
                        "/user/getNickname/**",
                        "/swagger-ui/**", // Swagger UI 경로 제외
                        "/v3/api-docs/**", // OpenAPI 명세 경로 제외
                        "/api/*/docs/**",  // Gateway를 통해 접근하는 API 명세 경로 제외
                        "/favicon.ico/**",  // Gateway를 통해 접근하는 API 명세 경로 제외
                        "/api/**"
                ); // 로그인/회원가입/닉네임 중복 path 제거
    }
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginUserArgumentResolver());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) { // 추가
        registry.addMapping("*") // User Service API 명세 경로
                .allowedOrigins("http://localhost:8000") // Gateway Origin 허용
                .allowedMethods("*") // 모든 HTTP 메소드 허용
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 필요한 경우
    }
}
