package com.fintrack.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Spring Data'nın {@code Page} arayüzünü doğrudan response olarak dönmek
 * yerine bu sabit şekle çeviriyoruz — Jackson'ın {@code PageImpl}
 * serileştirmesi kararsız/dahili bir yapıdır ve Spring tarafından
 * önerilmez (başlangıçta uyarı loglar).
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}
