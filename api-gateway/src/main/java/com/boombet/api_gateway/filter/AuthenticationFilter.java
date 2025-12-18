package com.boombet.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import com.boombet.api_gateway.util.JwtUtil;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    private RouterValidator validator;
    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (validator.isSecured.test(request)) {
                log.debug("Secured endpoint detected: {}", request.getURI().getPath());

                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    log.warn("Missing authorization header for secured endpoint: {}", request.getURI().getPath());
                    throw new RuntimeException("Missing authorization header");
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.warn("Invalid authorization header format");
                    throw new RuntimeException("Missing or invalid Bearer token");
                }

                String token = authHeader.substring(7);
                
                try {
                    if (!jwtUtil.isTokenValid(token)) {
                        log.warn("Token validation failed");
                        throw new RuntimeException("Invalid or expired token");
                    }
                    
                    String username = jwtUtil.extractUsername(token);
                    log.debug("Token validated for user: {}", username);
                    
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-Authenticated-User-Email", username)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (Exception e) {
                    log.error("Token validation error: {}", e.getMessage());
                    throw new RuntimeException("Unauthorized access to application", e);
                }
            }
            
            log.debug("Public endpoint: {}", request.getURI().getPath());
            return chain.filter(exchange);
        });
    }

    public static class Config {
    }
}
