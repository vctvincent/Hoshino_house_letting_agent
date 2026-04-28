package com.reams.service;

import com.reams.common.result.PageResult;
import com.reams.entity.Message;

import java.util.List;
import java.util.Map;

/**
 * 消息通知 Service 接口
 */
public interface MessageService {
    
    /**
     * 获取我的消息列表
     */
    PageResult getMyMessages(Long userId, Integer userType, Integer pageNum, Integer pageSize);
    
    /**
     * 获取消息详情
     */
    Message getMessageDetail(Long id);
    
    /**
     * 发送消息
     */
    void sendMessage(Message message);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(Long id);
    
    /**
     * 删除消息
     */
    void deleteMessage(Long id);
    
    /**
     * 获取未读消息数量
     */
    int getUnreadCount(Long userId, Integer userType);
    
    /**
     * 将用户的所有未读消息标记为已读
     */
    void markAllAsRead(Long userId, Integer userType);
    
    /**
     * 获取对话列表
     */
    List<Map<String, Object>> getConversations(Long userId, Integer userType);
    
    /**
     * 获取与指定用户的对话消息
     */
    PageResult getChatMessages(Long userId, Integer userType, Long targetId, Integer targetType, 
                               Integer pageNum, Integer pageSize);
}