package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.FinancialRecordRequest;
import com.finance.dashboard.dto.response.FinancialRecordResponse;
import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.entity.TransactionType;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.entity.Role;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.service.impl.FinancialRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialRecordService Unit Tests")
class FinancialRecordServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FinancialRecordServiceImpl recordService;

    private User adminUser;
    private FinancialRecordRequest recordRequest;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L).name("Admin").email("admin@finance.com")
                .role(Role.ADMIN).active(true).build();

        recordRequest = new FinancialRecordRequest();
        recordRequest.setAmount(new BigDecimal("1000.00"));
        recordRequest.setType(TransactionType.INCOME);
        recordRequest.setCategory("Salary");
        recordRequest.setDate(LocalDate.now());
        recordRequest.setNotes("Monthly salary");
    }

    @Test
    @DisplayName("Should create financial record successfully")
    void shouldCreateRecord() {
        when(userRepository.findByEmail("admin@finance.com")).thenReturn(Optional.of(adminUser));

        FinancialRecord saved = FinancialRecord.builder()
                .id(1L).amount(new BigDecimal("1000.00"))
                .type(TransactionType.INCOME).category("Salary")
                .date(LocalDate.now()).notes("Monthly salary")
                .createdBy(adminUser).build();

        when(recordRepository.save(any(FinancialRecord.class))).thenReturn(saved);

        FinancialRecordResponse result = recordService.createRecord(recordRequest, "admin@finance.com");

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(result.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(result.getCategory()).isEqualTo("Salary");
        verify(recordRepository).save(any(FinancialRecord.class));
    }

    @Test
    @DisplayName("Should soft-delete record on deleteRecord call")
    void shouldSoftDeleteRecord() {
        FinancialRecord record = FinancialRecord.builder()
                .id(1L).amount(new BigDecimal("500.00"))
                .type(TransactionType.EXPENSE).category("Rent")
                .date(LocalDate.now()).createdBy(adminUser).deleted(false).build();

        when(recordRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(FinancialRecord.class))).thenReturn(record);

        recordService.deleteRecord(1L);

        assertThat(record.isDeleted()).isTrue();
        verify(recordRepository).save(record);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent record")
    void shouldThrowExceptionForMissingRecord() {
        when(recordRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getRecordById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
