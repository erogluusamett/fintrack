package com.fintrack.analytics.service;

import com.fintrack.budget.repository.BudgetRepository;
import com.fintrack.budget.service.BudgetService;
import com.fintrack.subscription.repository.SubscriptionRepository;
import com.fintrack.transaction.repository.TransactionRepository;
import com.fintrack.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code percentChange}/{@code percentOf} sıfıra bölme gibi kenar
 * durumları barındırdığı için ayrı test edilmeye değer — bir dashboard'da
 * bu hesaplar yanlış olursa fark edilmesi zor olur.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void percentChange_computesStandardIncrease() {
        BigDecimal result = analyticsService.percentChange(new BigDecimal("100"), new BigDecimal("150"));

        assertThat(result).isEqualByComparingTo("50.00");
    }

    @Test
    void percentChange_computesStandardDecrease() {
        BigDecimal result = analyticsService.percentChange(new BigDecimal("200"), new BigDecimal("150"));

        assertThat(result).isEqualByComparingTo("-25.00");
    }

    @Test
    void percentChange_returnsZero_whenBothPreviousAndCurrentAreZero() {
        BigDecimal result = analyticsService.percentChange(BigDecimal.ZERO, BigDecimal.ZERO);

        assertThat(result).isEqualByComparingTo("0");
    }

    @Test
    void percentChange_returnsHundred_whenPreviousIsZeroAndCurrentIsPositive() {
        BigDecimal result = analyticsService.percentChange(BigDecimal.ZERO, new BigDecimal("500"));

        assertThat(result).isEqualByComparingTo("100");
    }

    /**
     * Regresyon testi: savings 0'dan negatife düştüğünde (gelir yokken
     * gider oluştuğunda) dashboard'da "+%100" gibi yanıltıcı bir "arttı"
     * sinyali üretilmemeli — bu gerçek bir manuel test sırasında bulunan
     * bug'dı (bkz. git geçmişi).
     */
    @Test
    void percentChange_returnsNegativeHundred_whenPreviousIsZeroAndCurrentIsNegative() {
        BigDecimal result = analyticsService.percentChange(BigDecimal.ZERO, new BigDecimal("-245.50"));

        assertThat(result).isEqualByComparingTo("-100");
    }

    @Test
    void percentOf_returnsZero_whenWholeIsZero_insteadOfThrowing() {
        BigDecimal result = analyticsService.percentOf(new BigDecimal("50"), BigDecimal.ZERO);

        assertThat(result).isEqualByComparingTo("0");
    }

    @Test
    void percentOf_computesShareOfWhole() {
        BigDecimal result = analyticsService.percentOf(new BigDecimal("250"), new BigDecimal("1000"));

        assertThat(result).isEqualByComparingTo("25.00");
    }
}
