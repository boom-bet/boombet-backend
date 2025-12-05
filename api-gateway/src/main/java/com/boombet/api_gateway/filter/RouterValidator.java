package com.boombet.api_gateway.filter;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouterValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/v1/events/upcoming",
            "/api/v1/events/live",
            "/api/v1/events/status",
            "/api/v1/events/sport",
            "/api/v1/events/filter",
            "/api/v1/events/",
            "/markets"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
