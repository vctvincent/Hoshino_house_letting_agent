package com.reams.controller;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.SysAgent;
import com.reams.service.AgentDashboardService;
import com.reams.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/agent")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentDashboardService agentDashboardService;

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public Result<PageResult<SysAgent>> getAgentList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("status", status);
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        return Result.success(agentService.getAgentPage(params));
    }

    @GetMapping("/all")
    public Result<List<SysAgent>> getAllAgents() {
        return Result.success(agentService.getAllAgents());
    }

    @GetMapping("/{id}")
    public Result<SysAgent> getAgentById(@PathVariable Long id) {
        return Result.success(agentService.getAgentById(id));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> getDashboard(Authentication authentication,
                                  @RequestParam(defaultValue = "6") Integer months) {
        Long agentId = (Long) authentication.getPrincipal();
        return agentDashboardService.getDashboardData(agentId, months);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteAgent(@PathVariable Long id) {
        return agentService.deleteAgent(id);
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateAgentStatus(@PathVariable Long id, @RequestParam Integer status) {
        return agentService.updateAgentStatus(id, status);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> updateAgentProfile(Authentication authentication, @RequestBody SysAgent agent) {
        agent.setId((Long) authentication.getPrincipal());
        return agentService.updateAgentProfile(agent);
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> approveAgent(@PathVariable Long id) {
        return agentService.approveAgent(id);
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> rejectAgent(@PathVariable Long id) {
        return agentService.rejectAgent(id);
    }
}
