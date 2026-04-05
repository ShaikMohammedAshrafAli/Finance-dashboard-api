package com.finance.dashboard.service.impl;

import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.dto.response.FinancialRecordResponse;
import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.entity.TransactionType;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final FinancialRecordRepository recordRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        BigDecimal totalIncome = recordRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);
        long totalRecords = recordRepository.countByDeletedFalse();

        List<DashboardSummaryResponse.CategorySummary> categoryBreakdown = buildCategoryBreakdown();
        List<DashboardSummaryResponse.MonthlyTrend> monthlyTrends = buildMonthlyTrends(
                LocalDate.now().minusMonths(11).withDayOfMonth(1));
        List<FinancialRecordResponse> recentActivity = buildRecentActivity();

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .totalRecords(totalRecords)
                .categoryBreakdown(categoryBreakdown)
                .monthlyTrends(monthlyTrends)
                .recentActivity(recentActivity)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummaryByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        BigDecimal totalIncome = recordRepository.sumByTypeAndDateRange(TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpenses = recordRepository.sumByTypeAndDateRange(TransactionType.EXPENSE, startDate, endDate);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        List<DashboardSummaryResponse.MonthlyTrend> monthlyTrends = buildMonthlyTrends(startDate);

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .categoryBreakdown(buildCategoryBreakdown())
                .monthlyTrends(monthlyTrends)
                .recentActivity(buildRecentActivity())
                .build();
    }

    private List<DashboardSummaryResponse.CategorySummary> buildCategoryBreakdown() {
        return recordRepository.getCategoryWiseTotals().stream()
                .map(row -> DashboardSummaryResponse.CategorySummary.builder()
                        .category((String) row[0])
                        .type(row[1].toString())
                        .total((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }

    private List<DashboardSummaryResponse.MonthlyTrend> buildMonthlyTrends(LocalDate startDate) {
        List<Object[]> rawData = recordRepository.getMonthlyTotals(startDate);

        // Group by year+month
        Map<String, DashboardSummaryResponse.MonthlyTrend> trendMap = new LinkedHashMap<>();

        for (Object[] row : rawData) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            String typeStr = row[2].toString();
            BigDecimal amount = (BigDecimal) row[3];
            String key = year + "-" + String.format("%02d", month);

            DashboardSummaryResponse.MonthlyTrend trend = trendMap.computeIfAbsent(key, k ->
                    DashboardSummaryResponse.MonthlyTrend.builder()
                            .year(year)
                            .month(month)
                            .monthName(Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + year)
                            .income(BigDecimal.ZERO)
                            .expenses(BigDecimal.ZERO)
                            .net(BigDecimal.ZERO)
                            .build()
            );

            if (TransactionType.INCOME.name().equals(typeStr)) {
                trend.setIncome(amount);
            } else {
                trend.setExpenses(amount);
            }
            trend.setNet(trend.getIncome().subtract(trend.getExpenses()));
        }

        return new ArrayList<>(trendMap.values());
    }

    private List<FinancialRecordResponse> buildRecentActivity() {
        return recordRepository.findRecentRecords(PageRequest.of(0, 10))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private FinancialRecordResponse toResponse(FinancialRecord r) {
        return FinancialRecordResponse.builder()
                .id(r.getId())
                .amount(r.getAmount())
                .type(r.getType())
                .category(r.getCategory())
                .date(r.getDate())
                .notes(r.getNotes())
                .createdByName(r.getCreatedBy().getName())
                .createdById(r.getCreatedBy().getId())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
