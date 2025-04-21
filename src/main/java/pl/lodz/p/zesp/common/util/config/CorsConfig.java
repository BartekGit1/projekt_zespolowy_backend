package pl.lodz.p.zesp.common.util.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "web.cors")
public record CorsConfig(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        Long maxAge,
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        Boolean allowCredentials
) {
}
