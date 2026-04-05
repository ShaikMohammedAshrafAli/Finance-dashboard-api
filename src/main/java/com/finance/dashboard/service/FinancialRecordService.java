package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.FinancialRecordRequest;
import com.finance.dashboard.dto.response.FinancialRecordResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.entity.TransactionType;

import java.time.LocalDate;

public interface FinancialRecordService {

    FinancialRecordResponse createRecord(FinancialRecordRequest request, String creatorEmail);

    FinancialRecordResponse getRecordById(Long id);

    PagedResponse<FinancialRecordResponse> getAllRecords(
            TransactionType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    );

    FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request);

    void deleteRecord(Long id);
}
