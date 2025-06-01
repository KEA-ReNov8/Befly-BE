package befly.community.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;

@OpenAPIDefinition(
        info = @Info(title = "Befly community-service API 명세서",
                version = "v1"))
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityRequirement accessTokenSecurityRequirement = new SecurityRequirement().addList("accessToken");
        SecurityRequirement refreshTokenSecurityRequirement = new SecurityRequirement().addList("refreshToken");

        // accessToken 쿠키 SecurityScheme 정의
        SecurityScheme accessTokenCookie = new SecurityScheme()
                .type(Type.APIKEY)
                .in(In.COOKIE)  // 헤더에서 쿠키로 변경
                .name("accessToken") // 쿠키 이름
                .description("액세스 토큰 (쿠키)");

        // refreshToken 쿠키 SecurityScheme 정의
        SecurityScheme refreshTokenCookie = new SecurityScheme()
                .type(Type.APIKEY)
                .in(In.COOKIE)  // 헤더에서 쿠키로 변경
                .name("refreshToken") // 쿠키 이름
                .description("리프레시 토큰 (쿠키)");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("accessToken", accessTokenCookie)
                        .addSecuritySchemes("refreshToken", refreshTokenCookie))
                .addSecurityItem(accessTokenSecurityRequirement)
                .addSecurityItem(refreshTokenSecurityRequirement)
                .addServersItem(new Server().url("/"));
    }
}