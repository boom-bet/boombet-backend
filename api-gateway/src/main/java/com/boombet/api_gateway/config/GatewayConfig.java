package com.boombet.api_gateway.config;

import com.boombet.api_gateway.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter filter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service-route", r -> r.path("/api/auth/**")
                        .uri("http://auth-service:8081"))
                
                .route("core-service-route", r -> r.path("/api/v1/**")
                        .filters(f -> f.filter(filter.apply(new AuthenticationFilter.Config())))
                        .uri("http://core-service:8082"))

                .route("realtime-service-route", r -> r.path("/ws/updates/**")
                        .uri("ws://realtime-service:8083"))
                
                .build();
    }
}
