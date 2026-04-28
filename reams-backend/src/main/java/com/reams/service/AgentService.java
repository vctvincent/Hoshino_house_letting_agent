package com.reams.service;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.SysAgent;

import java.util.List;
import java.util.Map;

/**
 * 中介服务接口
 */
public interface AgentService {

    /**
     * 分页查询中介
     */
    PageResult<SysAgent> getAgentPage(Map<String, Object> params);

    /**
     * 获取所有中介
     */
    List<SysAgent> getAllAgents();

    /**
     * 根据 ID 查询中介
     */
    SysAgent getAgentById(Long id);

    /**
     * 新增中介
     */
    Result<?> addAgent(SysAgent agent);

    /**
     * 更新中介
     */
    Result<?> updateAgent(SysAgent agent);

    /**
     * 删除中介
     */
    Result<?> deleteAgent(Long id);

    /**
     * 更新中介状态
     */
    Result<?> updateAgentStatus(Long id, Integer status);
    
    /**
     * 更新中介个人资料
     */
    Result<?> updateAgentProfile(SysAgent agent);
    
    /**
     * 审核通过中介
     */
    Result<?> approveAgent(Long id);
    
    /**
     * 拒绝中介审核
     */
    Result<?> rejectAgent(Long id);
}
