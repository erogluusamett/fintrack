package com.fintrack.transaction.entity;

import com.fintrack.category.entity.Category;
import com.fintrack.common.entity.SoftDeleteEntity;
import com.fintrack.common.enums.Currency;
import com.fintrack.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tutar her zaman pozitiftir; yön (para giriyor mu çıkıyor mu) {@code type}
 * ile ifade edilir — negatif/pozitif işaret kullanmak (örn. gider için -50)
 * toplama/analiz kodlarında işaret hatalarına çok açık bir tasarımdır.
 * <p>
 * {@code category} kasıtlı olarak nullable: INCOME/EXPENSE işlemler için
 * zorunludur ama sistemde ayrı bir "hesap" kavramı olmadığından TRANSFER
 * işlemler kategorisiz kalabilir (bkz. TransactionService doğrulaması).
 * <p>
 * Soft delete: {@code @SQLDelete} fiziksel DELETE'i UPDATE'e çevirir,
 * {@code @SQLRestriction} silinen kayıtları tüm sorgulardan (Specification'lar
 * dahil) otomatik filtreler — servis katmanının bunu unutması mümkün değil.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE transactions SET deleted = true, deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted = false")
public class Transaction extends SoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(length = 500)
    private String description;
}
