package com.finance.dashboard.entity;

public enum Role {
    VIEWER,   // Read-only: view dashboard and records
    ANALYST,  // Read records + access analytics/summaries
    ADMIN     // Full access: manage records and users
}
