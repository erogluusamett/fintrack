package com.fintrack.recurring.service;

import com.fintrack.category.entity.Category;
import com.fintrack.category.service.CategoryAccessService;
import com.fintrack.common.exception.ForbiddenException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.recurring.dto.CreateRecurringTransactionRequest;
import com.fintrack.recurring.dto.RecurringTransactionResponse;
import com.fintrack.recurring.entity.RecurringTransaction;
import com.fintrack.recurring.repository.RecurringTransactionRepository;
import com.fintrack.security.CurrentUserProvider;
import com.fintrack.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final CategoryAccessService categoryAccessService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> list() {
        UUID userId = CurrentUserProvider.getUserId();
        return recurringTransactionRepository.findByUserIdOrderByNextExecutionDateAsc(userId).stream()
                .map(RecurringTransactionResponse::from)
                .toList();
    }

    @Transactional
    public RecurringTransactionResponse create(CreateRecurringTransactionRequest request) {
        UUID userId = CurrentUserProvider.getUserId();
        Category category = categoryAccessService.resolveOptional(request.categoryId(), request.type(), userId);

        RecurringTransaction recurring = RecurringTransaction.builder()
                .user(userRepository.getReferenceById(userId))
                .category(category)
                .type(request.type())
                .amount(request.amount())
                .currency(request.currency())
                .frequency(request.frequency())
                .nextExecutionDate(request.nextExecutionDate())
                .description(request.description())
                .active(true)
                .build();
        recurringTransactionRepository.save(recurring);
        return RecurringTransactionResponse.from(recurring);
    }

    @Transactional
    public RecurringTransactionResponse update(UUID id, CreateRecurringTransactionRequest request) {
        RecurringTransaction recurring = findOwned(id);
        Category category = categoryAccessService.resolveOptional(request.categoryId(), request.type(), recurring.getUser().getId());

        recurring.setCategory(category);
        recurring.setType(request.type());
        recurring.setAmount(request.amount());
        recurring.setCurrency(request.currency());
        recurring.setFrequency(request.frequency());
        recurring.setNextExecutionDate(request.nextExecutionDate());
        recurring.setDescription(request.description());

        return RecurringTransactionResponse.from(recurring);
    }

    @Transactional
    public void delete(UUID id) {
        recurringTransactionRepository.delete(findOwned(id));
    }

    private RecurringTransaction findOwned(UUID id) {
        RecurringTransaction recurring = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("RecurringTransaction", id));
        if (!recurring.getUser().getId().equals(CurrentUserProvider.getUserId())) {
            throw new ForbiddenException("Bu tekrarlayan işleme erişim yetkin yok");
        }
        return recurring;
    }
}
