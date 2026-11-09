package kz.bitlab.mainservice.config;

import kz.bitlab.mainservice.security.KeycloakJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error"
    };

    // Reading courses, chapters, lessons and downloading attachments
    private static final String[] CONTENT_URLS = {
            "/api/v1/courses/**",
            "/api/v1/chapters/**",
            "/api/v1/lessons/**",
            "/attachment/download/**"
    };

    // Creating, updating and deleting courses and chapters (LT15, LT16)
    private static final String[] ADMIN_URLS = {
            "/course/**",
            "/chapter/**"
    };

    private static final String ADMIN = "ADMIN";
    private static final String TEACHER = "TEACHER";
    private static final String USER = "USER";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationEntryPoint authenticationEntryPoint,
                                                   AccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                // Stateless REST API with bearer tokens: no session, no CSRF
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(ADMIN_URLS).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, CONTENT_URLS).hasAnyRole(ADMIN, TEACHER, USER)
                        .requestMatchers(HttpMethod.POST, "/attachment/upload").hasAnyRole(ADMIN, TEACHER)
                        .requestMatchers("/api/v1/lessons/**").hasAnyRole(ADMIN, TEACHER)
                        // Finer rules for /api/v1/users are set with @PreAuthorize in UserController
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()))
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }

    /**
     * 401 and 403 from the security filters are passed to GlobalExceptionHandler,
     * so they get the same ErrorResponse body as all other errors.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        return (request, response, exception) -> resolver.resolveException(request, response, null, exception);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        return (request, response, exception) -> resolver.resolveException(request, response, null, exception);
    }
}
