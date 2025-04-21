package pl.lodz.p.zesp.common.util.config;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Arrays;

import static pl.lodz.p.zesp.common.util.Constants.*;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    public SecurityConfig(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    public static final AntPathRequestMatcher[] WHITELIST_URLS = {
            // Swagger/OpenAPI
            new AntPathRequestMatcher("/v3/api-docs/**"),
            new AntPathRequestMatcher("/api-docs"),
            new AntPathRequestMatcher("/api-docs/**"),
            new AntPathRequestMatcher("/swagger-resources"),
            new AntPathRequestMatcher("/swagger-resources/**"),
            new AntPathRequestMatcher("/configuration/ui"),
            new AntPathRequestMatcher("/configuration/security"),
            new AntPathRequestMatcher("/swagger-ui.html"),
            new AntPathRequestMatcher("/webjars/**"),
            new AntPathRequestMatcher("/swagger-ui/**")
    };

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.cors(Customizer.withDefaults());
//
//        http.csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(authz -> authz.
//                        requestMatchers(URI_VERSION_V1 + URI_USERS + URI_REGISTER)
//                        .permitAll()
//                        .requestMatchers(WHITELIST_URLS).permitAll()
//                        .anyRequest().authenticated()
//                )
//                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                ).exceptionHandling(exceptionHandling -> exceptionHandling
//                        .authenticationEntryPoint((request, response, ex) ->
//                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage())
//                        ))
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));
        return http.build();
    }
}