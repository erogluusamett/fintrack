package com.fintrack.category.service;

import com.fintrack.category.entity.Category;
import com.fintrack.category.repository.CategoryRepository;
import com.fintrack.common.exception.BusinessException;
import com.fintrack.common.exception.ForbiddenException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.transaction.entity.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Transaction ve RecurringTransaction'ın ikisi de aynı kuralı paylaşır:
 * INCOME/EXPENSE için kategori zorunlu, TRANSFER için opsiyonel; seçilen
 * kategori sistem varsayılanı ya da isteği yapan kullanıcıya ait olmalı.
 * Bu mantığın tek yerde durması için ayrı bir servise çıkarıldı.
 */
@Service
@RequiredArgsConstructor
public class CategoryAccessService {

    private final CategoryRepository categoryRepository;

    public Category resolveOptional(UUID categoryId, TransactionType type, UUID userId) {
        if (categoryId == null) {
            if (type != TransactionType.TRANSFER) {
                throw new BusinessException("INCOME/EXPENSE işlemler için kategori zorunludur");
            }
            return null;
        }

        return resolveOwnedOrSystem(categoryId, userId);
    }

    /**
     * Budget/Subscription için: kategori tamamen opsiyoneldir (null = "tüm
     * kategoriler"/"kategorisiz"), TRANSFER gibi bir tip kısıtı yoktur.
     */
    public Category resolveOwnedOrSystemOrNull(UUID categoryId, UUID userId) {
        return categoryId == null ? null : resolveOwnedOrSystem(categoryId, userId);
    }

    private Category resolveOwnedOrSystem(UUID categoryId, UUID userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", categoryId));
        if (!category.isSystemDefault() && !category.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Bu kategoriye erişim yetkin yok");
        }
        return category;
    }
}
