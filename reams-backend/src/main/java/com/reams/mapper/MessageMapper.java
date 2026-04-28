package com.reams.mapper;

import com.reams.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 消息通知 Mapper 接口
 */
@Mapper
public interface MessageMapper {
    
    /**
     * 根据 ID 查询消息
     */
    Message selectById(@Param("id") Long id);
    
    /**
     * 查询我的消息列表（分页）
     */
    List<Message> selectMyMessages(@Param("userId") Long userId, 
                                   @Param("userType") Integer userType);
    
    /**
     * 查询我的消息总数
     */
    int countMyMessages(@Param("userId") Long userId, @Param("userType") Integer userType);
    
    /**
     * 查询未读消息数量
     */
    int countUnreadMessages(@Param("userId") Long userId, @Param("userType") Integer userType);
    
    /**
     * 将用户的所有未读消息标记为已读
     */
    void markAllAsRead(@Param("userId") Long userId, @Param("userType") Integer userType);
    
    /**
     * 插入消息
     */
    int insert(Message message);
    
    /**
     * 更新消息（标记已读等）
     */
    int update(Message message);
    
    /**
     * 删除消息
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 获取对话列表
     */
    List<Map<String, Object>> selectConversations(@Param("userId") Long userId, 
                                                  @Param("userType") Integer userType);
    
    /**
     * 获取与指定用户的对话消息
     */
    List<Message> selectChatMessages(@Param("userId") Long userId, 
                                     @Param("userType") Integer userType,
                                     @Param("targetId") Long targetId,
                                     @Param("targetType") Integer targetType);
    
    /**
     * 统计与指定用户的对话消息数量
     */
    int countChatMessages(@Param("userId") Long userId,
                          @Param("userType") Integer userType,
                          @Param("targetId") Long targetId,
                          @Param("targetType") Integer targetType);
}