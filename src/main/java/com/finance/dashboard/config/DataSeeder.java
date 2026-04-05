package com.finance.dashboard.config;

import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.entity.Role;
import com.finance.dashboard.entity.TransactionType;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedFinancialRecords();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        User admin = User.builder()
                .name("Super Admin")
                .email("admin@finance.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .active(true)
                .build();

        User analyst = User.builder()
                .name("Jane Analyst")
                .email("analyst@finance.com")
                .password(passwordEncoder.encode("analyst123"))
                .role(Role.ANALYST)
                .active(true)
                .build();

        User viewer = User.builder()
                .name("Bob Viewer")
                .email("viewer@finance.com")
                .password(passwordEncoder.encode("viewer123"))
                .role(Role.VIEWER)
                .active(true)
                .build();

        userRepository.save(admin);
        userRepository.save(analyst);
        userRepository.save(viewer);

        log.info("✅ Seeded 3 default users:");
        log.info("   ADMIN   → admin@finance.com   / admin123");
        log.info("   ANALYST → analyst@finance.com / analyst123");
        log.info("   VIEWER  → viewer@finance.com  / viewer123");
    }

    private void seedFinancialRecords() {
        if (recordRepository.count() > 0) return;

        User admin = userRepository.findByEmail("admin@finance.com").orElseThrow();

        LocalDate now = LocalDate.now();

        Object[][] records = {
                {new BigDecimal("50000.00"), TransactionType.INCOME,  "Salary",         now.minusDays(1),  "Monthly salary"},
                {new BigDecimal("1200.00"),  TransactionType.EXPENSE, "Rent",           now.minusDays(2),  "Office rent"},
                {new BigDecimal("350.00"),   TransactionType.EXPENSE, "Utilities",      now.minusDays(3),  "Electricity & water"},
                {new BigDecimal("15000.00"), TransactionType.INCOME,  "Freelance",      now.minusDays(5),  "Consulting project"},
                {new BigDecimal("800.00"),   TransactionType.EXPENSE, "Marketing",      now.minusDays(7),  "Social media ads"},
                {new BigDecimal("250.00"),   TransactionType.EXPENSE, "Software",       now.minusDays(10), "SaaS subscriptions"},
                {new BigDecimal("5000.00"),  TransactionType.INCOME,  "Investment",     now.minusDays(15), "Dividend income"},
                {new BigDecimal("1500.00"),  TransactionType.EXPENSE, "Travel",         now.minusDays(20), "Business trip"},
                {new BigDecimal("300.00"),   TransactionType.EXPENSE, "Office Supplies",now.minusDays(22), "Stationery"},
                {new BigDecimal("48000.00"), TransactionType.INCOME,  "Salary",         now.minusMonths(1),"Last month salary"},
                {new BigDecimal("1200.00"),  TransactionType.EXPENSE, "Rent",           now.minusMonths(1).minusDays(2),"Last month rent"},
                {new BigDecimal("400.00"),   TransactionType.EXPENSE, "Utilities",      now.minusMonths(1).minusDays(5),"Last month utilities"},
        };

        for (Object[] r : records) {
            recordRepository.save(FinancialRecord.builder()
                    .amount((BigDecimal) r[0])
                    .type((TransactionType) r[1])
                    .category((String) r[2])
                    .date((LocalDate) r[3])
                    .notes((String) r[4])
                    .createdBy(admin)
                    .build());
        }

        log.info("✅ Seeded {} sample financial records", records.length);
    }
}
