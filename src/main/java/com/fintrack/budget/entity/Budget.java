package com.fintrack.budget.entity;

import com.fintrack.category.entity.Category;
import com.fintrack.common.entity.BaseEntity;
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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * {@code category == null} tüm kategorileri kapsayan genel bir bütçe demektir.
 * {@code endDate}, oluşturma anında {@code startDate + period}'den hesaplanıp
 * saklanır (bkz. BudgetService) — böylece "bu bütçe şu an aktif mi" sorgusu
 * basit bir tarih aralığı karşılaştırmasına indirgenir.
 * <p>
 * {@code currency} kasıtlı olarak eklendi: harcama toplamı yalnızca aynı para
 * biriminden transaction'ları toplar (bkz. BudgetService#calculateSpent).
 * Farklı para birimlerini dönüştürmek Faz 11 (Multi-Currency/exchange rate)
 * kapsamına bırakıldı.
 */
@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Budget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BudgetPeriod period;

    @Column(name = "amount_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
}
