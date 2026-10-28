package kz.bitlab.mainservice.config;

import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applies the real {@link SecurityConfig} to a {@code @WebMvcTest} slice.
 * JwtDecoder is mocked, so tests never call Keycloak: authenticate requests with
 * {@code @WithMockUser} or {@code SecurityMockMvcRequestPostProcessors.user(...)} instead.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SecurityConfig.class)
@MockitoBean(types = JwtDecoder.class)
public @interface WithSecurityConfig {
}
