package com.hongxiaoya.api.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongxiaoya.api.common.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/chat")
public class ChatWebSocket {

    private static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        try {
            String token = getQueryParam(session.getQueryString(), "token");
            if (!StringUtils.hasText(token)) {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Token required"));
                return;
            }
            Long userId = JwtUtils.parseToken(token);
            onlineUsers.put(userId, session);
            log.info("User {} connected to WebSocket", userId);
        } catch (Exception e) {
            log.error("WebSocket connect failed", e);
            try {
                session.close();
            } catch (Exception ignored) {}
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // Here we can handle ping/pong or other control messages if needed
        // Actual chat messages are usually sent via HTTP REST API and pushed via WebSocket
        log.info("Received message: {}", message);
    }

    @OnClose
    public void onClose(Session session) {
        Long userId = findUserId(session);
        if (userId != null) {
            onlineUsers.remove(userId);
            log.info("User {} disconnected", userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("WebSocket error", throwable);
        Long userId = findUserId(session);
        if (userId != null) {
            onlineUsers.remove(userId);
        }
    }

    /**
     * Send message to a specific user
     */
    public static void sendMessageToUser(Long userId, Object messageObj) {
        Session session = onlineUsers.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String msgJson = objectMapper.writeValueAsString(messageObj);
                session.getAsyncRemote().sendText(msgJson);
            } catch (Exception e) {
                log.error("Send message to user {} failed", userId, e);
            }
        }
    }

    private Long findUserId(Session session) {
        for (Map.Entry<Long, Session> entry : onlineUsers.entrySet()) {
            if (entry.getValue().equals(session)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String getQueryParam(String query, String key) {
        if (query == null) return null;
        String[] params = query.split("&");
        for (String param : params) {
            String[] kv = param.split("=");
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }
        return null;
    }
}
