package com.example.clb.projecttracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class OpenAPIConfig {

    private static final String OAUTH2_SCHEME_NAME = "oauth2";
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String SCHEME = "bearer";
    private static final String BEARER_FORMAT = "JWT";

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-id:}")
    private String githubClientId;

    @Bean
    public OpenAPI customOpenAPI() {
        // Define the OAuth2 scheme
        OAuthFlow oAuthFlow = new OAuthFlow();
        oAuthFlow.authorizationUrl("http://localhost:" + serverPort + "/oauth2/authorize/google");
        oAuthFlow.tokenUrl("http://localhost:" + serverPort + "/login/oauth2/code/google");
        
        Scopes scopes = new Scopes()
            .addString("openid", "OpenID Connect")
            .addString("profile", "User profile")
            .addString("email", "User email");
        oAuthFlow.scopes(scopes);

        OAuthFlows oAuthFlows = new OAuthFlows()
            .authorizationCode(oAuthFlow);

        SecurityScheme oauth2Scheme = new SecurityScheme()
            .type(SecurityScheme.Type.OAUTH2)
            .flows(oAuthFlows);

        SecurityScheme bearerScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");

        return new OpenAPI()
            .servers(List.of(
                new Server().url("http://localhost:" + serverPort).description("Development Server")
            ))
            .info(new Info()
                .title("Project Tracker API")
                .version("1.0.0")
                .description("Project Tracker Application API Documentation")
                .contact(new Contact()
                    .name("API Support")
                    .email("support@example.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .components(new Components()
                .addSecuritySchemes("oauth2", oauth2Scheme)
                .addSecuritySchemes("bearerAuth", bearerScheme)
            )
            .addSecurityItem(new SecurityRequirement()
                .addList("oauth2", Arrays.asList("openid", "profile", "email"))
            )
            .addSecurityItem(new SecurityRequirement()
                .addList("bearerAuth", Collections.emptyList())
            );
    }
}
