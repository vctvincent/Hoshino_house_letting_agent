package com.reams.service;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.Viewing;

import java.util.Map;

/**
 * 带看服务接口
 */
public interface ViewingService {

    /**
     * 分页查询带看记录
     */
    PageResult<Viewing> getViewingPage(Map<String, Object> params);

    /**
     * 获取带看详情
     */
    Result<?> getViewingDetail(Long id);

    /**
     * 客户预约看房
     */
    Result<?> bookViewing(Viewing viewing);

    /**
     * 中介确认带看
     */
    Result<?> confirmViewing(Long id);

    /**
     * 标记带看完成
     */
    Result<?> completeViewing(Long id, String feedback);

    /**
     * 取消带看
     */
    Result<?> cancelViewing(Long id, String reason);

    /**
     * 获取客户的带看记录
     */
    Result<?> getCustomerViewings(Long customerId);

    /**
     * 获取中介的带看记录
     */
    Result<?> getAgentViewings(Long agentId);

    /**
     * 获取中介带看过的客户列表（去重）
     */
    Result<?> getAgentCustomers(Long agentId);
    
    /**
     * 中介发起带看申请
     */
    Result<?> agentBookViewing(Viewing viewing);
    
    /**
     * 客户确认中介发起的带看
     */
    Result<?> customerConfirmViewing(Long id);
}
