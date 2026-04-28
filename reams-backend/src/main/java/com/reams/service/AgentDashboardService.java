package com.reams.service;

import com.reams.common.result.Result;

public interface AgentDashboardService {

    Result<?> getDashboardData(Long agentId, Integer salesWindow);
}
