package com.reams.controller;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.Message;
import com.reams.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知 Controller
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    /**
     * 获取我的消息列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<PageResult> getMyMessages(
            @RequestParam Long userId,
            @RequestParam Integer userType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult result = messageService.getMyMessages(userId, userType, pageNum, pageSize);
        return Result.success(result);
    }
    
    /**
     * 获取消息详情
     */
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<Message> getMessageDetail(@PathVariable Long id) {
        Message message = messageService.getMessageDetail(id);
        if (message == null) {
            return Result.error("消息不存在");
        }
        return Result.success(message);
    }
    
    /**
     * 发送消息
     */
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<String> sendMessage(@RequestBody Message message) {
        messageService.sendMessage(message);
        return Result.success("发送成功");
    }
    
    /**
     * 标记消息为已读
     */
    @PutMapping("/read/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<String> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success("已标记为已读");
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<String> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return Result.success("删除成功");
    }
    
    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<Map<String, Object>> getUnreadCount(
            @RequestParam Long userId,
            @RequestParam Integer userType) {
        
        System.out.println("🔍 [未读消息] 查询未读消息数量 - userId: " + userId + ", userType: " + userType);
        
        int count = messageService.getUnreadCount(userId, userType);
        
        System.out.println("📊 [未读消息] 查询结果: " + count);
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }
    
    /**
     * 将所有消息标记为已读
     */
    @PostMapping("/read/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<?> markAllAsRead(
            @RequestParam Long userId,
            @RequestParam Integer userType) {
        messageService.markAllAsRead(userId, userType);
        return Result.success("已将所有消息标记为已读");
    }
    
    /**
     * 获取对话列表
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<List<Map<String, Object>>> getConversations(
            @RequestParam Long userId,
            @RequestParam Integer userType) {
        List<Map<String, Object>> conversations = messageService.getConversations(userId, userType);
        return Result.success(conversations);
    }
    
    /**
     * 获取与指定用户的对话消息
     */
    @GetMapping("/chat")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<PageResult> getChatMessages(
            @RequestParam Long userId,
            @RequestParam Integer userType,
            @RequestParam Long targetId,
            @RequestParam Integer targetType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        PageResult result = messageService.getChatMessages(userId, userType, targetId, targetType, pageNum, pageSize);
        return Result.success(result);
    }
}