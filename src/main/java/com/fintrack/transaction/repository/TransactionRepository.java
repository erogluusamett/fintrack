package com.fintrack.transaction.repository;

import com.fintrack.common.enums.Currency;
import com.fintrack.transaction.entity.Transaction;
import com.fintrack.transaction.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    /**
     * Kasıtlı olarak native sorgu: {@code Transaction} entity'sindeki
     * {@code @SQLRestriction("deleted = false")} her JPQL sorgusuna da
     * uygulanır, ama burada soft-delete edilmiş kayıtları da görmemiz
     * gerekiyor — onlar fiziksel olarak hâlâ DB'de ve FK'yi tutuyorlar
     * (soft-delete edilmiş bir işlem bile kategori referansını fiziksel
     * silmeye karşı korur; bkz. CategoryService).
     */
    @Query(value = "SELECT EXISTS(SELECT 1 FROM transactions WHERE category_id = :categoryId)", nativeQuery = true)
    boolean existsByCategoryIdIncludingDeleted(@Param("categoryId") UUID categoryId);

    /** Budget durum hesaplaması için: bkz. BudgetService#calculateSpent. */
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.user.id = :userId
              AND t.type = com.fintrack.transaction.entity.TransactionType.EXPENSE
              AND t.currency = :currency
              AND t.transactionDate BETWEEN :startDate AND :endDate
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
            """)
    BigDecimal sumExpenseAmount(@Param("userId") UUID userId,
                                 @Param("currency") Currency currency,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 @Param("categoryId") UUID categoryId);

    /** AnalyticsService için genel amaçlı toplam — Budget'a özel olmayan (INCOME dahil) sürüm. */
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.user.id = :userId AND t.type = :type AND t.currency = :currency
              AND t.transactionDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumAmountByTypeAndDateRange(@Param("userId") UUID userId,
                                            @Param("type") TransactionType type,
                                            @Param("currency") Currency currency,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    Optional<Transaction> findFirstByUserIdAndTypeAndCurrencyAndTransactionDateBetweenOrderByAmountDesc(
            UUID userId, TransactionType type, Currency currency, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT t.category.id AS categoryId, t.category.name AS categoryName, SUM(t.amount) AS total
            FROM Transaction t
            WHERE t.user.id = :userId AND t.type = :type AND t.currency = :currency
              AND t.transactionDate BETWEEN :startDate AND :endDate AND t.category IS NOT NULL
            GROUP BY t.category.id, t.category.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<CategoryAmountProjection> sumByCategoryForDateRange(@Param("userId") UUID userId,
                                                              @Param("type") TransactionType type,
                                                              @Param("currency") Currency currency,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);

    interface CategoryAmountProjection {
        UUID getCategoryId();
        String getCategoryName();
        BigDecimal getTotal();
    }
}
