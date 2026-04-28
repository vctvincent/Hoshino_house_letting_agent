package com.reams.controller;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.Viewing;
import com.reams.service.ViewingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// 带看控制器
@RestController
@RequestMapping("/api/viewing")
public class ViewingController {

    @Autowired
    private ViewingService viewingService;

    // 分页查询带看记录
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<PageResult<Viewing>> getViewingList(
            Authentication authentication,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) Long houseId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Map<String, Object> params = new HashMap<>();

        // 根据角色自动过滤数据
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        Long userId = (Long) authentication.getPrincipal();

        if ("ROLE_CUSTOMER".equals(role)) {
            params.put("customerId", userId);
        } else if ("ROLE_AGENT".equals(role)) {
            params.put("agentId", userId);
        }
        // ADMIN 可以查看所有

        params.put("houseId", houseId);
        params.put("status", status);
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);

        return Result.success(viewingService.getViewingPage(params));
    }

    // 获取带看详情
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<?> getViewingDetail(@PathVariable Long id) {
        return viewingService.getViewingDetail(id);
    }

    // 客户预约看房
    @PostMapping("/book")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<?> bookViewing(Authentication authentication, @RequestBody Viewing viewing) {
        Long userId = (Long) authentication.getPrincipal();
        viewing.setCustomerId(userId);
        return viewingService.bookViewing(viewing);
    }

    // 中介确认带看
    @PostMapping("/confirm/{id}")
    @PreAuthorize("hasAnyRole('AGENT')")
    public Result<?> confirmViewing(@PathVariable Long id) {
        return viewingService.confirmViewing(id);
    }

    // 标记带看完成
    @PostMapping("/complete/{id}")
    @PreAuthorize("hasAnyRole('AGENT')")
    public Result<?> completeViewing(@PathVariable Long id, @RequestParam(required = false) String feedback) {
        return viewingService.completeViewing(id, feedback);
    }

    // 取消带看
    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'CUSTOMER')")
    public Result<?> cancelViewing(@PathVariable Long id,
                                   @RequestParam(required = false) String reason) {
        return viewingService.cancelViewing(id, reason);
    }

    // 获取我的预约 (客户)
    @GetMapping("/my/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<?> getMyViewingsAsCustomer(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return viewingService.getCustomerViewings(userId);
    }

    // 获取我的带看 (中介)
    @GetMapping("/my/agent")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> getMyViewingsAsAgent(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return viewingService.getAgentViewings(userId);
    }

    // 获取中介带看过的客户列表（去重）
    @GetMapping("/my/customers")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> getMyCustomersAsAgent(Authentication authentication) {
        Long agentId = (Long) authentication.getPrincipal();
        return viewingService.getAgentCustomers(agentId);
    }

    // 中介发起带看申请
    @PostMapping("/agent/book")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> agentBookViewing(Authentication authentication, @RequestBody Viewing viewing) {
        Long userId = (Long) authentication.getPrincipal();
        viewing.setAgentId(userId);
        return viewingService.agentBookViewing(viewing);
    }

    // 客户确认中介发起的带看
    @PostMapping("/customer/confirm/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<?> customerConfirmViewing(@PathVariable Long id) {
        return viewingService.customerConfirmViewing(id);
    }
}
