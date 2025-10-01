package com.boombet.api_gateway.filter;

import com.boombet.api_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.http.server.reactive.ServerHttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                log.info(">>>> [FILTER] Secured endpoint detected: {}", request.getURI());

                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    log.error(">>>> [FILTER] Authorization header is missing!");
                    throw new RuntimeException("Missing authorization header");
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.error(">>>> [FILTER] Authorization header is invalid!");
                    throw new RuntimeException("Missing or invalid Bearer token");
                }

                String token = authHeader.substring(7);
                
                try {
                    String username = jwtUtil.extractUsername(token);
                    log.info(">>>> [FILTER] Token is valid. Extracted username: {}", username);
                    
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-Authenticated-User-Email", username)
                            .build();
                    
                    log.info(">>>> [FILTER] Forwarding request to {} with new header X-Authenticated-User-Email", exchange.getRequest().getURI());

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (Exception e) {
                    log.error(">>>> [FILTER] Invalid token!", e);
                    throw new RuntimeException("Unauthorized access to application", e);
                }
            }
            
            log.info(">>>> [FILTER] Open endpoint detected: {}. Forwarding without changes.", request.getURI());
            return chain.filter(exchange);
        });
    }

    public static class Config {
    }
}
