package com.reams.service.impl;

import com.github.pagehelper.PageHelper;
import com.reams.common.constant.MessageScenes;
import com.reams.common.result.PageResult;
import com.reams.entity.Message;
import com.reams.mapper.MessageMapper;
import com.reams.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 消息通知 Service 实现类
 */
@Service
public class MessageServiceImpl implements MessageService {
    
    @Autowired
    private MessageMapper messageMapper;
    
    @Override
    public PageResult getMyMessages(Long userId, Integer userType, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Message> messages = messageMapper.selectMyMessages(userId, userType);
        int total = messageMapper.countMyMessages(userId, userType);
        return new PageResult((long) total, messages);
    }
    
    @Override
    public Message getMessageDetail(Long id) {
        return messageMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public void sendMessage(Message message) {
        message.setIsRead(0); // 默认未读
        if (message.getContentType() == null || message.getContentType().trim().isEmpty()) {
            message.setContentType("TEXT");
        }
        if (message.getMessageScene() == null || message.getMessageScene().trim().isEmpty()) {
            message.setMessageScene(MessageScenes.CHAT);
        }
        message.setCreateTime(new Date());
        messageMapper.insert(message);
    }
    
    @Override
    @Transactional
    public void markAsRead(Long id) {
        Message message = messageMapper.selectById(id);
        if (message != null && message.getIsRead() == 0) {
            message.setIsRead(1);
            message.setReadTime(new Date());
            messageMapper.update(message);
        }
    }
    
    @Override
    @Transactional
    public void deleteMessage(Long id) {
        messageMapper.deleteById(id);
    }
    
    @Override
    public int getUnreadCount(Long userId, Integer userType) {
        return messageMapper.countUnreadMessages(userId, userType);
    }
    
    @Override
    public void markAllAsRead(Long userId, Integer userType) {
        messageMapper.markAllAsRead(userId, userType);
    }
    
    @Override
    public java.util.List<Map<String, Object>> getConversations(Long userId, Integer userType) {
        return messageMapper.selectConversations(userId, userType);
    }
    
    @Override
    public PageResult getChatMessages(Long userId, Integer userType, Long targetId, Integer targetType,
                                      Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Message> messages = messageMapper.selectChatMessages(userId, userType, targetId, targetType);
        int total = messageMapper.countChatMessages(userId, userType, targetId, targetType);
        return new PageResult((long) total, messages);
    }
}
