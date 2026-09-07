package com.fintrack.category.service;

import com.fintrack.category.dto.CategoryResponse;
import com.fintrack.category.dto.CreateCategoryRequest;
import com.fintrack.category.dto.UpdateCategoryRequest;
import com.fintrack.category.entity.Category;
import com.fintrack.category.entity.CategoryType;
import com.fintrack.category.repository.CategoryRepository;
import com.fintrack.common.exception.BusinessException;
import com.fintrack.common.exception.DuplicateResourceException;
import com.fintrack.common.exception.ForbiddenException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.security.CurrentUserProvider;
import com.fintrack.transaction.repository.TransactionRepository;
import com.fintrack.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        UUID userId = CurrentUserProvider.getUserId();
        return categoryRepository.findByUserIdIsNullOrUserIdOrderByNameAsc(userId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        UUID userId = CurrentUserProvider.getUserId();
        ensureNameAvailable(userId, request.name(), request.type(), null);

        Category category = Category.builder()
                .user(userRepository.getReferenceById(userId))
                .name(request.name())
                .type(request.type())
                .isDefault(false)
                .build();
        categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse update(UUID id, UpdateCategoryRequest request) {
        Category category = findOwnedEditable(id);
        ensureNameAvailable(category.getUser().getId(), request.name(), category.getType(), id);
        category.setName(request.name());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(UUID id) {
        Category category = findOwnedEditable(id);
        if (transactionRepository.existsByCategoryIdIncludingDeleted(id)) {
            throw new BusinessException("Bu kategori işlemler tarafından kullanılıyor, silinemez");
        }
        categoryRepository.delete(category);
    }

    /** Sistem varsayılanı değilse ve istek sahibine aitse döner; aksi halde 403. */
    private Category findOwnedEditable(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", id));
        if (category.isSystemDefault()) {
            throw new ForbiddenException("Sistem kategorileri düzenlenemez veya silinemez");
        }
        if (!category.getUser().getId().equals(CurrentUserProvider.getUserId())) {
            throw new ForbiddenException("Bu kategoriye erişim yetkin yok");
        }
        return category;
    }

    private void ensureNameAvailable(UUID userId, String name, CategoryType type, UUID excludingId) {
        boolean duplicate = categoryRepository.findByUserIdIsNullOrUserIdOrderByNameAsc(userId).stream()
                .anyMatch(c -> c.getType() == type
                        && c.getName().equalsIgnoreCase(name)
                        && !c.getId().equals(excludingId));
        if (duplicate) {
            throw new DuplicateResourceException("Bu isim ve tipte bir kategori zaten var: " + name);
        }
    }
}
