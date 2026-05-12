package com.gomoku.config;

import com.gomoku.websocket.RoomHandshakeInterceptor;
import com.gomoku.websocket.RoomWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RoomWebSocketHandler roomWebSocketHandler;
    private final RoomHandshakeInterceptor roomHandshakeInterceptor;

    public WebSocketConfig(RoomWebSocketHandler roomWebSocketHandler, RoomHandshakeInterceptor roomHandshakeInterceptor) {
        this.roomWebSocketHandler = roomWebSocketHandler;
        this.roomHandshakeInterceptor = roomHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(roomWebSocketHandler, "/ws/room/{roomId}")
                .addInterceptors(roomHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
