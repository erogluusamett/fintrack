package com.fintrack.budget.service;

import com.fintrack.budget.dto.BudgetResponse;
import com.fintrack.budget.dto.BudgetStatusResponse;
import com.fintrack.budget.dto.CreateBudgetRequest;
import com.fintrack.budget.entity.Budget;
import com.fintrack.budget.entity.BudgetPeriod;
import com.fintrack.budget.repository.BudgetRepository;
import com.fintrack.category.entity.Category;
import com.fintrack.category.service.CategoryAccessService;
import com.fintrack.common.exception.ForbiddenException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.security.CurrentUserProvider;
import com.fintrack.transaction.repository.TransactionRepository;
import com.fintrack.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private static final BigDecimal WARNING_THRESHOLD = BigDecimal.valueOf(80);
    private static final BigDecimal EXCEEDED_THRESHOLD = BigDecimal.valueOf(100);

    private final BudgetRepository budgetRepository;
    private final CategoryAccessService categoryAccessService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<BudgetResponse> list() {
        UUID userId = CurrentUserProvider.getUserId();
        return budgetRepository.findByUserIdOrderByStartDateDesc(userId).stream()
                .map(BudgetResponse::from)
                .toList();
    }

    @Transactional
    public BudgetResponse create(CreateBudgetRequest request) {
        UUID userId = CurrentUserProvider.getUserId();
        Category category = categoryAccessService.resolveOwnedOrSystemOrNull(request.categoryId(), userId);

        Budget budget = Budget.builder()
                .user(userRepository.getReferenceById(userId))
                .category(category)
                .period(request.period())
                .amountLimit(request.amountLimit())
                .currency(request.currency())
                .startDate(request.startDate())
                .endDate(computeEndDate(request.startDate(), request.period()))
                .build();
        budgetRepository.save(budget);
        return BudgetResponse.from(budget);
    }

    @Transactional
    public BudgetResponse update(UUID id, CreateBudgetRequest request) {
        Budget budget = findOwned(id);
        Category category = categoryAccessService.resolveOwnedOrSystemOrNull(request.categoryId(), budget.getUser().getId());

        budget.setCategory(category);
        budget.setPeriod(request.period());
        budget.setAmountLimit(request.amountLimit());
        budget.setCurrency(request.currency());
        budget.setStartDate(request.startDate());
        budget.setEndDate(computeEndDate(request.startDate(), request.period()));

        return BudgetResponse.from(budget);
    }

    @Transactional
    public void delete(UUID id) {
        budgetRepository.delete(findOwned(id));
    }

    @Transactional(readOnly = true)
    public BudgetStatusResponse status(UUID id) {
        return computeStatus(findOwned(id));
    }

    /**
     * Ownership kontrolü YAPMAZ — entity zaten elde bulunduğunda kullanılır.
     * {@link com.fintrack.budget.listener.BudgetEventListener} bunu event
     * işlerken çağırır; o context'te HTTP isteği/{@code CurrentUserProvider}
     * yoktur, bu yüzden {@link #status(UUID)}'ün ownership-check'li yolunu
     * kullanamaz.
     */
    @Transactional(readOnly = true)
    public BudgetStatusResponse computeStatus(Budget budget) {
        BigDecimal spent = calculateSpent(budget);
        BigDecimal remaining = budget.getAmountLimit().subtract(spent);
        BigDecimal usagePercentage = spent
                .divide(budget.getAmountLimit(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        BudgetStatusResponse.Status status = BudgetStatusResponse.Status.OK;
        if (usagePercentage.compareTo(EXCEEDED_THRESHOLD) >= 0) {
            status = BudgetStatusResponse.Status.EXCEEDED;
        } else if (usagePercentage.compareTo(WARNING_THRESHOLD) >= 0) {
            status = BudgetStatusResponse.Status.WARNING;
        }

        return new BudgetStatusResponse(budget.getId(), budget.getAmountLimit(), spent, remaining, usagePercentage, status);
    }

    /**
     * Yalnızca aynı para biriminden EXPENSE transaction'ları toplar — bkz.
     * Budget entity Javadoc'undaki multi-currency notu.
     */
    private BigDecimal calculateSpent(Budget budget) {
        UUID categoryId = budget.getCategory() != null ? budget.getCategory().getId() : null;
        return transactionRepository.sumExpenseAmount(
                budget.getUser().getId(), budget.getCurrency(), budget.getStartDate(), budget.getEndDate(), categoryId);
    }

    private LocalDate computeEndDate(LocalDate startDate, BudgetPeriod period) {
        return switch (period) {
            case WEEKLY -> startDate.plusWeeks(1).minusDays(1);
            case MONTHLY -> startDate.plusMonths(1).minusDays(1);
            case YEARLY -> startDate.plusYears(1).minusDays(1);
        };
    }

    private Budget findOwned(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Budget", id));
        if (!budget.getUser().getId().equals(CurrentUserProvider.getUserId())) {
            throw new ForbiddenException("Bu bütçeye erişim yetkin yok");
        }
        return budget;
    }
}
