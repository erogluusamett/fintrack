package com.fintrack.transaction.service;

import com.fintrack.category.entity.Category;
import com.fintrack.category.service.CategoryAccessService;
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
    private final CategoryAccessService categoryAccessService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        return createForUser(CurrentUserProvider.getUserId(), request);
    }

    /**
     * {@code CurrentUserProvider} yerine açık bir {@code userId} alır —
     * {@code RecurringTransactionScheduler} gibi HTTP isteği/SecurityContext
     * olmayan bir arka plan işinden çağrılabilmesi için. Aynı
     * {@link TransactionCreatedEvent} yayınlanır, böylece otomatik oluşan
     * işlemler de budget kontrolünden geçer.
     */
    @Transactional
    public TransactionResponse createForUser(UUID userId, CreateTransactionRequest request) {
        Category category = categoryAccessService.resolveOptional(request.categoryId(), request.type(), userId);

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
                transaction.getType(), transaction.getAmount(), transaction.getCurrency(), transaction.getTransactionDate()));

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
        Category category = categoryAccessService.resolveOptional(request.categoryId(), request.type(), transaction.getUser().getId());

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
}
