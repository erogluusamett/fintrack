package com.fintrack.transaction.service;

import com.fintrack.category.entity.Category;
import com.fintrack.category.repository.CategoryRepository;
import com.fintrack.common.exception.BusinessException;
import com.fintrack.common.exception.ForbiddenException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.security.CurrentUserProvider;
import com.fintrack.transaction.dto.CreateTransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import com.fintrack.transaction.dto.UpdateTransactionRequest;
import com.fintrack.transaction.entity.Transaction;
import com.fintrack.transaction.entity.TransactionType;
import com.fintrack.transaction.event.TransactionCreatedEvent;
import com.fintrack.transaction.repository.TransactionRepository;
import com.fintrack.transaction.repository.TransactionSpecifications;
import com.fintrack.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        UUID userId = CurrentUserProvider.getUserId();
        Category category = resolveCategory(request.categoryId(), request.type(), userId);

        Transaction transaction = Transaction.builder()
                .user(userRepository.getReferenceById(userId))
                .category(category)
                .type(request.type())
                .amount(request.amount())
                .currency(request.currency())
                .transactionDate(request.transactionDate())
                .description(request.description())
                .build();
        transactionRepository.save(transaction);

        eventPublisher.publishEvent(new TransactionCreatedEvent(
                transaction.getId(), userId, category != null ? category.getId() : null,
                transaction.getType(), transaction.getAmount()));

        return TransactionResponse.from(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(UUID id) {
        return TransactionResponse.from(findOwned(id));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> list(TransactionType type, UUID categoryId, LocalDate from, LocalDate to, Pageable pageable) {
        UUID userId = CurrentUserProvider.getUserId();
        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.belongsToUser(userId))
                .and(TransactionSpecifications.hasType(type))
                .and(TransactionSpecifications.hasCategory(categoryId))
                .and(TransactionSpecifications.dateFrom(from))
                .and(TransactionSpecifications.dateTo(to));

        return transactionRepository.findAll(spec, pageable).map(TransactionResponse::from);
    }

    @Transactional
    public TransactionResponse update(UUID id, UpdateTransactionRequest request) {
        Transaction transaction = findOwned(id);
        Category category = resolveCategory(request.categoryId(), request.type(), transaction.getUser().getId());

        transaction.setCategory(category);
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setDescription(request.description());

        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(UUID id) {
        transactionRepository.delete(findOwned(id));
    }

    private Transaction findOwned(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Transaction", id));
        if (!transaction.getUser().getId().equals(CurrentUserProvider.getUserId())) {
            throw new ForbiddenException("Bu işleme erişim yetkin yok");
        }
        return transaction;
    }

    /**
     * TRANSFER işlemler kategorisiz kalabilir (sistemde ayrı bir "hesap"
     * kavramı olmadığı için anlamlı bir kategori seçimi yok); INCOME/EXPENSE
     * için kategori zorunludur. Seçilen kategori sistem varsayılanı ya da
     * isteği yapan kullanıcıya ait olmalıdır.
     */
    private Category resolveCategory(UUID categoryId, TransactionType type, UUID userId) {
        if (categoryId == null) {
            if (type != TransactionType.TRANSFER) {
                throw new BusinessException("INCOME/EXPENSE işlemler için kategori zorunludur");
            }
            return null;
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", categoryId));
        if (!category.isSystemDefault() && !category.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Bu kategoriye erişim yetkin yok");
        }
        return category;
    }
}
