package com.fintrack.transaction.repository;

import com.fintrack.transaction.entity.Transaction;
import com.fintrack.transaction.entity.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Dinamik filtreleme (tarih/kategori/tip kombinasyonları isteğe bağlı
 * olduğu için) için Specification pattern — her kombinasyon için ayrı bir
 * repository metodu yazmak yerine tek bir sorgu, isteğe göre birleştirilen
 * predicate'lerden kurulur. {@code deleted = false} filtresi burada tekrar
 * yazılmaz; entity üzerindeki {@code @SQLRestriction} zaten her sorguya
 * otomatik uygular.
 */
public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> belongsToUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> hasCategory(UUID categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> dateFrom(LocalDate from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("transactionDate"), from);
    }

    public static Specification<Transaction> dateTo(LocalDate to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("transactionDate"), to);
    }
}
