package com.hongxiaoya.api.service;

import com.hongxiaoya.api.dto.ConversationDto;
import com.hongxiaoya.api.entity.Message;

import java.util.List;

public interface MessageService {
    List<ConversationDto> getConversations(Long userId);
    List<Message> getChatHistory(Long userId, Long targetId);
    Message sendMessage(Long senderId, Long receiverId, Integer msgType, String content);
    void readMessage(Long userId, Long targetId);
    void topConversation(Long userId, Long targetId, boolean isTop);
}
