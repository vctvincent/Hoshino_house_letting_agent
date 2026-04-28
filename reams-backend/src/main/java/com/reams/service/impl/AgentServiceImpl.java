package com.reams.service.impl;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.SysAgent;
import com.reams.mapper.SysAgentMapper;
import com.reams.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AgentServiceImpl implements AgentService {

    @Autowired
    private SysAgentMapper agentMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public PageResult<SysAgent> getAgentPage(Map<String, Object> params) {
        Integer pageNum = (Integer) params.get("pageNum");
        Integer pageSize = (Integer) params.get("pageSize");
        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }

        String keyword = (String) params.get("keyword");
        Integer status = (Integer) params.get("status");
        Integer offset = (pageNum - 1) * pageSize;

        List<SysAgent> list;
        long total;
        if ((keyword != null && !keyword.trim().isEmpty()) || status != null) {
            list = agentMapper.selectPageWithSearch(offset, pageSize, keyword, status);
            total = agentMapper.countWithSearch(keyword, status);
        } else {
            list = agentMapper.selectPage(offset, pageSize);
            total = agentMapper.count();
        }

        return PageResult.of(total, pageNum, pageSize, list);
    }

    @Override
    public List<SysAgent> getAllAgents() {
        return agentMapper.selectAll();
    }

    @Override
    public SysAgent getAgentById(Long id) {
        return agentMapper.selectById(id);
    }

    @Override
    @Transactional
    public Result<?> addAgent(SysAgent agent) {
        if (agentMapper.selectByPhone(agent.getPhone()) != null) {
            return Result.error("手机号已注册");
        }

        if (agent.getName() == null || agent.getName().trim().isEmpty()) {
            agent.setName(buildDefaultAgentName(agent.getPhone()));
        }

        if (agent.getPassword() != null && !agent.getPassword().isEmpty()) {
            agent.setPassword(passwordEncoder.encode(agent.getPassword()));
        }
        agent.setStatus(1);
        agentMapper.insert(agent);
        return Result.success("中介添加成功");
    }

    @Override
    @Transactional
    public Result<?> updateAgent(SysAgent agent) {
        SysAgent existingAgent = agentMapper.selectById(agent.getId());
        if (existingAgent == null) {
            return Result.error("中介不存在");
        }

        if (agent.getPhone() != null && !agent.getPhone().equals(existingAgent.getPhone())) {
            SysAgent phoneExists = agentMapper.selectByPhone(agent.getPhone());
            if (phoneExists != null && !phoneExists.getId().equals(agent.getId())) {
                return Result.error("手机号已被使用");
            }
        }

        if (agent.getPassword() != null && !agent.getPassword().isEmpty()) {
            agent.setPassword(passwordEncoder.encode(agent.getPassword()));
        } else {
            agent.setPassword(null);
        }

        agentMapper.update(agent);
        return Result.success("中介信息更新成功");
    }

    @Override
    @Transactional
    public Result<?> deleteAgent(Long id) {
        SysAgent agent = agentMapper.selectById(id);
        if (agent == null) {
            return Result.error("中介不存在");
        }
        agentMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    @Transactional
    public Result<?> updateAgentStatus(Long id, Integer status) {
        SysAgent agent = agentMapper.selectById(id);
        if (agent == null) {
            return Result.error("中介不存在");
        }

        SysAgent updateAgent = new SysAgent();
        updateAgent.setId(id);
        updateAgent.setStatus(status);
        agentMapper.update(updateAgent);
        return Result.success("状态更新成功");
    }

    @Override
    @Transactional
    public Result<?> updateAgentProfile(SysAgent agent) {
        SysAgent existingAgent = agentMapper.selectById(agent.getId());
        if (existingAgent == null) {
            return Result.error("中介不存在");
        }

        if (agent.getPhone() != null && !agent.getPhone().equals(existingAgent.getPhone())) {
            SysAgent phoneExists = agentMapper.selectByPhone(agent.getPhone());
            if (phoneExists != null && !phoneExists.getId().equals(agent.getId())) {
                return Result.error("手机号已被使用");
            }
        }

        agentMapper.update(agent);
        return Result.success("个人资料更新成功");
    }

    @Override
    @Transactional
    public Result<?> approveAgent(Long id) {
        SysAgent agent = agentMapper.selectById(id);
        if (agent == null) {
            return Result.error("中介不存在");
        }
        
        if (agent.getStatus() != 2) {
            return Result.error("该中介不是待审核状态");
        }
        
        SysAgent updateAgent = new SysAgent();
        updateAgent.setId(id);
        updateAgent.setStatus(1);  // 1表示正常状态
        agentMapper.update(updateAgent);
        return Result.success("审核通过");
    }

    @Override
    @Transactional
    public Result<?> rejectAgent(Long id) {
        SysAgent agent = agentMapper.selectById(id);
        if (agent == null) {
            return Result.error("中介不存在");
        }
        
        if (agent.getStatus() != 2) {
            return Result.error("该中介不是待审核状态");
        }
        
        SysAgent updateAgent = new SysAgent();
        updateAgent.setId(id);
        updateAgent.setStatus(0);  // 0表示禁用状态
        agentMapper.update(updateAgent);
        return Result.success("已拒绝");
    }

    private String buildDefaultAgentName(String phone) {
        if (phone != null && phone.length() >= 4) {
            return "中介" + phone.substring(phone.length() - 4);
        }
        return "中介";
    }
}
