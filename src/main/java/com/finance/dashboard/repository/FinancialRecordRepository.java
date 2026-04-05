package com.finance.dashboard.repository;

import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // Find active (non-deleted) records with filters
    @Query("""
            SELECT r FROM FinancialRecord r
            WHERE r.deleted = false
              AND (:type IS NULL OR r.type = :type)
              AND (:category IS NULL OR LOWER(r.category) LIKE LOWER(CONCAT('%', :category, '%')))
              AND (:startDate IS NULL OR r.date >= :startDate)
              AND (:endDate IS NULL OR r.date <= :endDate)
            ORDER BY r.date DESC, r.createdAt DESC
            """)
    Page<FinancialRecord> findAllWithFilters(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);

    // Total by type
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.deleted = false AND r.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);

    // Total by type within date range
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.deleted = false AND r.type = :type AND r.date BETWEEN :startDate AND :endDate")
    BigDecimal sumByTypeAndDateRange(
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Category-wise totals
    @Query("SELECT r.category, r.type, SUM(r.amount) FROM FinancialRecord r WHERE r.deleted = false GROUP BY r.category, r.type ORDER BY SUM(r.amount) DESC")
    List<Object[]> getCategoryWiseTotals();

    // Monthly totals
    @Query("""
            SELECT FUNCTION('YEAR', r.date), FUNCTION('MONTH', r.date), r.type, SUM(r.amount)
            FROM FinancialRecord r
            WHERE r.deleted = false
              AND r.date >= :startDate
            GROUP BY FUNCTION('YEAR', r.date), FUNCTION('MONTH', r.date), r.type
            ORDER BY FUNCTION('YEAR', r.date) ASC, FUNCTION('MONTH', r.date) ASC
            """)
    List<Object[]> getMonthlyTotals(@Param("startDate") LocalDate startDate);

    // Recent records
    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false ORDER BY r.createdAt DESC")
    List<FinancialRecord> findRecentRecords(Pageable pageable);

    // Count active records
    long countByDeletedFalse();
}
