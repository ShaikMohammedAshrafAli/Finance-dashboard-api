package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.DashboardSummaryResponse;

public interface DashboardService {
    DashboardSummaryResponse getSummary();
    DashboardSummaryResponse getSummaryByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate);
}
