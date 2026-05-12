package com.gomoku.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriTemplate;

import java.util.Map;

@Component
public class RoomHandshakeInterceptor implements HandshakeInterceptor {

    private static final UriTemplate URI_TEMPLATE = new UriTemplate("/ws/room/{roomId}");

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String path = request.getURI().getPath();
        Map<String, String> variables = URI_TEMPLATE.match(path);
        
        if (variables.containsKey("roomId")) {
            attributes.put("roomId", variables.get("roomId"));
        }
        
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}
