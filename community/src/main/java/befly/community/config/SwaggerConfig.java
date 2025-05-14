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
        SecurityRequirement refreshTokenSecurityRequirement = new SecurityRequirement().addList("X-Refresh-Token");
        SecurityRequirement accesssTokenSecurityRequirement = new SecurityRequirement().addList("Authorization");

        // accessToken 헤더 SecurityScheme 정의
        SecurityScheme accessTokenHeader = new SecurityScheme()
                .type(Type.APIKEY)
                .in(In.HEADER)
                .scheme("bearer")
                .name("Authorization") // 일반적인 Authorization 헤더 사용
                .description("액세스 토큰 (Bearer 형식)");

        // refreshToken 헤더 SecurityScheme 정의
        SecurityScheme refreshTokenHeader = new SecurityScheme()
                .type(Type.APIKEY)
                .in(In.HEADER)
                .scheme("bearer")
                .name("X-Refresh-Token") // 사용자 정의 헤더 사용
                .description("리프레시 토큰 (Bearer 형식)");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Authorization", accessTokenHeader)
                        .addSecuritySchemes("X-Refresh-Token", refreshTokenHeader))
                .addSecurityItem(accesssTokenSecurityRequirement)
                .addSecurityItem(refreshTokenSecurityRequirement)
                .addServersItem(new Server().url("/"));
    }
}