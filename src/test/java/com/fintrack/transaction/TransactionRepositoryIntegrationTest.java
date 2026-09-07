package com.fintrack.transaction;

import com.fintrack.AbstractIntegrationTest;
import com.fintrack.category.entity.Category;
import com.fintrack.category.entity.CategoryType;
import com.fintrack.category.repository.CategoryRepository;
import com.fintrack.common.enums.Currency;
import com.fintrack.transaction.entity.Transaction;
import com.fintrack.transaction.entity.TransactionType;
import com.fintrack.transaction.repository.TransactionRepository;
import com.fintrack.user.entity.User;
import com.fintrack.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec'in "Integration tests: Repository" isteğinin karşılığı. İki şeyi
 * özellikle hedefler: (1) analytics/budget'ın dayandığı aggregate sorgunun
 * tip/para birimi/tarih filtrelerini doğru uyguladığını, (2) soft-delete
 * mekanizmasının ({@code @SQLDelete}/{@code @SQLRestriction}) ve onu
 * kasıtlı olarak atlayan native sorgunun ({@code existsByCategoryIdIncludingDeleted})
 * birlikte doğru çalıştığını — bu ikisi Faz 2 test sürecinde bulunan gerçek
 * bir bug'ın (bkz. git geçmişi) regresyon testidir.
 */
class TransactionRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void sumAmountByTypeAndDateRange_sumsOnlyTransactionsMatchingTypeCurrencyAndDateRange() {
        User user = persistUser();
        Category category = persistCategory();

        saveTransaction(user, category, TransactionType.EXPENSE, "100", Currency.TRY, LocalDate.of(2026, 9, 5));
        saveTransaction(user, category, TransactionType.EXPENSE, "50", Currency.TRY, LocalDate.of(2026, 9, 20));
        saveTransaction(user, category, TransactionType.EXPENSE, "999", Currency.USD, LocalDate.of(2026, 9, 10)); // farklı para birimi
        saveTransaction(user, category, TransactionType.EXPENSE, "999", Currency.TRY, LocalDate.of(2026, 8, 31)); // dönem dışı
        saveTransaction(user, category, TransactionType.INCOME, "999", Currency.TRY, LocalDate.of(2026, 9, 10)); // farklı tip

        BigDecimal total = transactionRepository.sumAmountByTypeAndDateRange(
                user.getId(), TransactionType.EXPENSE, Currency.TRY,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertThat(total).isEqualByComparingTo("150");
    }

    @Test
    void softDeletedTransaction_isInvisibleToSqlRestriction_butStillCountsForCategoryFkSafety() {
        User user = persistUser();
        Category category = persistCategory();
        Transaction transaction = saveTransaction(user, category, TransactionType.EXPENSE, "10", Currency.TRY, LocalDate.of(2026, 9, 1));

        transactionRepository.delete(transaction); // @SQLDelete: fiziksel DELETE değil, UPDATE deleted=true

        assertThat(transactionRepository.findById(transaction.getId())).isEmpty(); // @SQLRestriction gizler
        assertThat(transactionRepository.existsByCategoryIdIncludingDeleted(category.getId())).isTrue(); // native sorgu görmeye devam eder
    }

    private User persistUser() {
        User user = User.builder()
                .email("repo-test-" + UUID.randomUUID() + "@fintrack-test.dev")
                .passwordHash("irrelevant-for-this-test")
                .firstName("Repo")
                .lastName("Test")
                .build();
        return userRepository.save(user);
    }

    private Category persistCategory() {
        Category category = Category.builder()
                .user(null)
                .name("Repo Test Category " + UUID.randomUUID())
                .type(CategoryType.EXPENSE)
                .isDefault(false)
                .build();
        return categoryRepository.save(category);
    }

    private Transaction saveTransaction(User user, Category category, TransactionType type, String amount, Currency currency, LocalDate date) {
        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .type(type)
                .amount(new BigDecimal(amount))
                .currency(currency)
                .transactionDate(date)
                .build();
        return transactionRepository.save(transaction);
    }
}
