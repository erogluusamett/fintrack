package com.fintrack.budget.service;

import com.fintrack.budget.dto.BudgetStatusResponse;
import com.fintrack.budget.entity.Budget;
import com.fintrack.budget.entity.BudgetPeriod;
import com.fintrack.budget.repository.BudgetRepository;
import com.fintrack.category.service.CategoryAccessService;
import com.fintrack.common.enums.Currency;
import com.fintrack.transaction.repository.TransactionRepository;
import com.fintrack.user.entity.User;
import com.fintrack.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Spec'in açıkça istediği "budget calculations" unit test kapsamı: %80/%100
 * eşiklerinin doğru sınıflandığını doğrular. {@code computeStatus} kasıtlı
 * olarak ownership check yapmıyor (bkz. sınıfın Javadoc'u) — bu da onu
 * SecurityContext kurmadan izole test edilebilir kılıyor.
 */
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private CategoryAccessService categoryAccessService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private com.fintrack.audit.service.AuditService auditService;

    @InjectMocks
    private BudgetService budgetService;

    @ParameterizedTest(name = "spent={0} of limit=1000 -> {1}")
    @CsvSource({
            "0, OK",
            "500, OK",
            "799, OK",
            "800, WARNING",
            "950, WARNING",
            "999, WARNING",
            "1000, EXCEEDED",
            "1500, EXCEEDED"
    })
    void computeStatus_classifiesUsageAgainstTheEightyAndHundredPercentThresholds(String spentAmount, String expectedStatus) {
        Budget budget = budgetWithLimit(new BigDecimal("1000"));
        when(transactionRepository.sumExpenseAmount(any(), eq(Currency.TRY), any(), any(), any()))
                .thenReturn(new BigDecimal(spentAmount));

        BudgetStatusResponse status = budgetService.computeStatus(budget);

        assertThat(status.status()).isEqualTo(BudgetStatusResponse.Status.valueOf(expectedStatus));
    }

    @Test
    void computeStatus_calculatesRemainingAsLimitMinusSpent() {
        Budget budget = budgetWithLimit(new BigDecimal("1000"));
        when(transactionRepository.sumExpenseAmount(any(), eq(Currency.TRY), any(), any(), any()))
                .thenReturn(new BigDecimal("300"));

        BudgetStatusResponse status = budgetService.computeStatus(budget);

        assertThat(status.remaining()).isEqualByComparingTo("700");
    }

    @Test
    void computeStatus_allowsRemainingToGoNegative_whenExceeded() {
        Budget budget = budgetWithLimit(new BigDecimal("1000"));
        when(transactionRepository.sumExpenseAmount(any(), eq(Currency.TRY), any(), any(), any()))
                .thenReturn(new BigDecimal("1300"));

        BudgetStatusResponse status = budgetService.computeStatus(budget);

        assertThat(status.remaining()).isEqualByComparingTo("-300");
        assertThat(status.status()).isEqualTo(BudgetStatusResponse.Status.EXCEEDED);
    }

    private Budget budgetWithLimit(BigDecimal limit) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());

        return Budget.builder()
                .user(user)
                .category(null)
                .period(BudgetPeriod.MONTHLY)
                .amountLimit(limit)
                .currency(Currency.TRY)
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .build();
    }
}
