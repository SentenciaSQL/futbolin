package com.futbolin.core.config;

import com.futbolin.api.ws.MatchWebSocketHandler;
import com.futbolin.api.ws.WsHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MatchWebSocketHandler handler;
    private final WsHandshakeInterceptor interceptor;

    public WebSocketConfig(MatchWebSocketHandler handler, WsHandshakeInterceptor interceptor) {
        this.handler = handler;
        this.interceptor = interceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/match")
                .addInterceptors(interceptor)
                .setAllowedOrigins("*");
    }
}
