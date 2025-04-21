package pl.lodz.p.zesp.common.util.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final CorsConfig corsConfig;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsConfig.allowedOrigins());
        configuration.setAllowedMethods(corsConfig.allowedMethods());
        configuration.setAllowedHeaders(corsConfig.allowedHeaders());
        configuration.setExposedHeaders(corsConfig.exposedHeaders());
        configuration.setAllowCredentials(corsConfig.allowCredentials());
        configuration.setMaxAge(corsConfig.maxAge());

        registry.addMapping("/**")
                .combine(configuration);
    }
}
