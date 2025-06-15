package com.example.clb.projecttracker.config;

import com.example.clb.projecttracker.security.jwt.AuthEntryPointJwt;
import com.example.clb.projecttracker.security.jwt.AuthTokenFilter;
import com.example.clb.projecttracker.security.oauth2.CustomOAuth2UserService;
import com.example.clb.projecttracker.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.clb.projecttracker.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.example.clb.projecttracker.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.example.clb.projecttracker.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private Environment env;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Enable CORS and disable CSRF
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            
            // Set session management to IF_REQUIRED for OAuth2
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            
            // Set unauthorized requests exception handler
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler)
            )
            
            // Configure OAuth2 Login
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorize")
                    .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/oauth2/callback/*")
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
            )
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/",
                    "/error",
                    "/favicon.ico",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/swagger-config.yaml",
                    "/v3/api-docs/swagger-config",
                    "/actuator/health",
                    "/api/auth/**",
                    "/api/test/public",
                    "/api/test/oauth2/public",
                    "/oauth2/**",
                    "/api/docs/**",
                        "/auth/**"



                ).permitAll()
                
                // OAuth2 login endpoints
                .requestMatchers(
                    "/login/oauth2/**",
                    "/oauth2/authorization/**",
                    "/oauth2/callback/**"
                ).permitAll()
                
                // Role-based access control
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Project endpoints
                .requestMatchers(HttpMethod.GET, "/api/projects/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/projects").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/projects/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/projects/**").hasRole("ADMIN")
                
                // Task endpoints
                .requestMatchers(HttpMethod.GET, "/api/tasks/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/tasks").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/tasks/**").authenticated()
                
                // All other requests need to be authenticated
                .anyRequest().authenticated()
            );

        // Add JWT token filter
        http.authenticationProvider(authenticationProvider())
            .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // React dev server
            "http://localhost:8080",  // Default port
            "http://localhost:8081",  // Alternative port
            "https://editor.swagger.io",  // Swagger Editor
            "http://localhost:8080/swagger-ui/oauth2-redirect.html"  // Swagger UI OAuth2 redirect
        ));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin",
            "Access-Control-Allow-Methods", "Access-Control-Allow-Headers",
            "Access-Control-Allow-Origin", "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin",
            "Access-Control-Allow-Methods", "Access-Control-Allow-Headers",
            "Access-Control-Allow-Origin", "Access-Control-Request-Headers"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
