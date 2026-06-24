package com.hongxiaoya.api.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongxiaoya.api.common.JwtUtils;
import com.hongxiaoya.api.entity.UserProfile;
import com.hongxiaoya.api.mapper.UserProfileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@ServerEndpoint("/ws/activity/{activityId}")
public class ActivityRoomWebSocket {

    private static UserProfileMapper userProfileMapper;

    @Autowired
    public void setUserProfileMapper(UserProfileMapper mapper) {
        ActivityRoomWebSocket.userProfileMapper = mapper;
    }

    // activityId -> set of sessions
    private static final Map<Long, CopyOnWriteArraySet<Session>> roomSessions = new ConcurrentHashMap<>();
    // session.getId() -> userId
    private static final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("activityId") Long activityId) {
        try {
            String token = getQueryParam(session.getQueryString(), "token");
            if (!StringUtils.hasText(token)) {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Token required"));
                return;
            }
            Long userId = JwtUtils.parseToken(token);
            sessionUserMap.put(session.getId(), userId);
            
            roomSessions.computeIfAbsent(activityId, k -> new CopyOnWriteArraySet<>()).add(session);
            
            UserProfile profile = userProfileMapper.selectById(userId);
            String nickname = profile != null ? profile.getNickname() : "用户" + userId;

            // Broadcast join message
            broadcast(activityId, Map.of(
                    "type", "system",
                    "content", "【" + nickname + "】加入了相亲房间"
            ));

            log.info("User {} joined activity room {}", userId, activityId);
        } catch (Exception e) {
            log.error("Activity Room WS connect failed", e);
            try {
                session.close();
            } catch (Exception ignored) {}
        }
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("activityId") Long activityId) {
        try {
            Long userId = sessionUserMap.get(session.getId());
            if (userId == null) return;
            
            UserProfile profile = userProfileMapper.selectById(userId);
            String nickname = profile != null ? profile.getNickname() : "用户" + userId;

            Map<String, Object> msgData = objectMapper.readValue(message, Map.class);
            String type = (String) msgData.get("type");
            String content = (String) msgData.get("content");

            if ("text".equals(type)) {
                broadcast(activityId, Map.of(
                        "type", "text",
                        "userId", userId,
                        "name", nickname,
                        "content", content
                ));
            } else if ("gift".equals(type)) {
                broadcast(activityId, Map.of(
                        "type", "system",
                        "content", "【" + nickname + "】送出了 " + content + "，气氛瞬间升温！"
                ));
            }

        } catch (Exception e) {
            log.error("Handle message failed", e);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("activityId") Long activityId) {
        Long userId = sessionUserMap.remove(session.getId());
        CopyOnWriteArraySet<Session> sessions = roomSessions.get(activityId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(activityId);
            }
        }
        if (userId != null) {
            UserProfile profile = userProfileMapper.selectById(userId);
            String nickname = profile != null ? profile.getNickname() : "用户" + userId;
            broadcast(activityId, Map.of(
                    "type", "system",
                    "content", "【" + nickname + "】离开了房间"
            ));
            log.info("User {} left activity room {}", userId, activityId);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("activityId") Long activityId) {
        log.error("Activity Room WS error", throwable);
        onClose(session, activityId);
    }

    private void broadcast(Long activityId, Map<String, Object> message) {
        CopyOnWriteArraySet<Session> sessions = roomSessions.get(activityId);
        if (sessions == null) return;

        try {
            String msgJson = objectMapper.writeValueAsString(message);
            for (Session session : sessions) {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(msgJson);
                }
            }
        } catch (Exception e) {
            log.error("Broadcast failed", e);
        }
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
