package com.fintrack.transaction.repository;

import com.fintrack.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
