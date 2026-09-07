package com.fintrack.category.repository;

import com.fintrack.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /** Sistem varsayılanları (user_id NULL) + bu kullanıcının kendi kategorileri. */
    List<Category> findByUserIdIsNullOrUserIdOrderByNameAsc(UUID userId);
}
