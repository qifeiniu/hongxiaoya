package com.hongxiaoya.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongxiaoya.api.dto.ConversationDto;
import com.hongxiaoya.api.entity.Conversation;
import com.hongxiaoya.api.entity.Message;
import com.hongxiaoya.api.entity.UserProfile;
import com.hongxiaoya.api.mapper.ConversationMapper;
import com.hongxiaoya.api.mapper.MessageMapper;
import com.hongxiaoya.api.mapper.UserProfileMapper;
import com.hongxiaoya.api.service.MessageService;
import com.hongxiaoya.api.websocket.ChatWebSocket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final ConversationMapper conversationMapper;
    private final UserProfileMapper userProfileMapper;

    @Override
    public List<ConversationDto> getConversations(Long userId) {
        List<Conversation> list = conversationMapper.selectList(new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUserId, userId)
                .orderByDesc(Conversation::getIsTop)
                .orderByDesc(Conversation::getUpdatedAt));

        if (list.isEmpty()) return new ArrayList<>();

        List<Long> targetIds = list.stream().map(Conversation::getTargetId).collect(Collectors.toList());
        Map<Long, UserProfile> profileMap = userProfileMapper.selectBatchIds(targetIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, p -> p));

        List<Long> msgIds = list.stream().map(Conversation::getLastMsgId).filter(id -> id != null).collect(Collectors.toList());
        Map<Long, Message> msgMap = msgIds.isEmpty() ? Map.of() : messageMapper.selectBatchIds(msgIds).stream()
                .collect(Collectors.toMap(Message::getId, m -> m));

        return list.stream().map(c -> {
            ConversationDto dto = new ConversationDto();
            dto.setId(c.getId());
            dto.setTargetId(c.getTargetId());
            dto.setTargetProfile(profileMap.get(c.getTargetId()));
            dto.setUnreadCount(c.getUnreadCount());
            dto.setIsTop(c.getIsTop());
            if (c.getLastMsgId() != null && msgMap.containsKey(c.getLastMsgId())) {
                Message m = msgMap.get(c.getLastMsgId());
                dto.setLastMsgContent(m.getMsgType() == 1 ? m.getContent() : "[其他消息]");
                dto.setLastMsgTime(m.getCreatedAt());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Message> getChatHistory(Long userId, Long targetId) {
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getSenderId, userId).eq(Message::getReceiverId, targetId)
                        .or().eq(Message::getSenderId, targetId).eq(Message::getReceiverId, userId))
                .orderByAsc(Message::getCreatedAt));
    }

    @Override
    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, Integer msgType, String content) {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setMsgType(msgType);
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setIsRecalled(0);
        msg.setCreatedAt(new Date());
        messageMapper.insert(msg);

        updateConversation(senderId, receiverId, msg.getId(), 0);
        updateConversation(receiverId, senderId, msg.getId(), 1);

        // Push message via WebSocket
        ChatWebSocket.sendMessageToUser(receiverId, msg);

        return msg;
    }

    private void updateConversation(Long userId, Long targetId, Long lastMsgId, int addUnread) {
        Conversation conv = conversationMapper.selectOne(new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUserId, userId).eq(Conversation::getTargetId, targetId));
        if (conv == null) {
            conv = new Conversation();
            conv.setUserId(userId);
            conv.setTargetId(targetId);
            conv.setLastMsgId(lastMsgId);
            conv.setUnreadCount(addUnread);
            conv.setIsTop(0);
            conv.setUpdatedAt(new Date());
            conversationMapper.insert(conv);
        } else {
            conv.setLastMsgId(lastMsgId);
            conv.setUnreadCount(conv.getUnreadCount() + addUnread);
            conv.setUpdatedAt(new Date());
            conversationMapper.updateById(conv);
        }
    }

    @Override
    @Transactional
    public void readMessage(Long userId, Long targetId) {
        messageMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Message>()
                .set(Message::getIsRead, 1)
                .eq(Message::getSenderId, targetId)
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0));

        conversationMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Conversation>()
                .set(Conversation::getUnreadCount, 0)
                .eq(Conversation::getUserId, userId)
                .eq(Conversation::getTargetId, targetId));
    }

    @Override
    public void topConversation(Long userId, Long targetId, boolean isTop) {
        conversationMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Conversation>()
                .set(Conversation::getIsTop, isTop ? 1 : 0)
                .eq(Conversation::getUserId, userId)
                .eq(Conversation::getTargetId, targetId));
    }
}
