package com.reams.service;

import com.reams.common.result.Result;

public interface AdminDashboardService {

    Result<?> getDashboardData(Integer salesWindow);
}
