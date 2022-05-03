package com.example.gateway;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.RouteMetadataUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration
public class AppConfig {

    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }

    @Bean
    public ErrorProperties errorProperties() {
        return new ErrorProperties();
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {

        return builder.routes()
                // route with fallback controller
                .route("route-with-fallback", predicateSpec -> predicateSpec.path("/withfallback")
                        .filters(gatewayFilterSpec -> gatewayFilterSpec.circuitBreaker(config -> {
                            config.setStatusCodes(Set.of("504", "503", "500", "404"));
                            config.setFallbackUri("/fallback");
                        })).uri("http://someunknownurl1.com"))
                // route with error handler
                .route("route-without-fallback", predicateSpec -> predicateSpec
                        .path("/withoutfallback")
                        .filters(gatewayFilterSpec -> gatewayFilterSpec.circuitBreaker(config -> config.setStatusCodes(Set.of("504", "404"))))
                        .metadata(Map.of(RouteMetadataUtils.CONNECT_TIMEOUT_ATTR, 1000,
                                RouteMetadataUtils.RESPONSE_TIMEOUT_ATTR, 1000))
                        .uri("https://www.google.com"))
                .build();

    }
}
