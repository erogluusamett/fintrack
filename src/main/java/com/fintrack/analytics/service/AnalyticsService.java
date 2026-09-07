package com.fintrack.analytics.service;

import com.fintrack.analytics.dto.CategoryAmount;
import com.fintrack.analytics.dto.CategoryDistributionItem;
import com.fintrack.analytics.dto.ComparisonGranularity;
import com.fintrack.analytics.dto.ComparisonResponse;
import com.fintrack.analytics.dto.DashboardResponse;
import com.fintrack.analytics.dto.Insight;
import com.fintrack.analytics.dto.InsightType;
import com.fintrack.analytics.dto.TrendPoint;
import com.fintrack.budget.entity.Budget;
import com.fintrack.budget.repository.BudgetRepository;
import com.fintrack.budget.service.BudgetService;
import com.fintrack.common.enums.Currency;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.security.CurrentUserProvider;
import com.fintrack.subscription.entity.Subscription;
import com.fintrack.subscription.repository.SubscriptionRepository;
import com.fintrack.transaction.entity.Transaction;
import com.fintrack.transaction.entity.TransactionType;
import com.fintrack.transaction.repository.TransactionRepository;
import com.fintrack.user.entity.User;
import com.fintrack.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Tüm hesaplamalar tek bir para birimi kapsamında yapılır (varsayılan:
 * kullanıcının {@code defaultCurrency}'si, {@code currency} parametresiyle
 * override edilebilir) — Budget'taki aynı gerekçe: farklı para birimlerini
 * dönüştürmeden toplamak yanıltıcı olurdu (bkz. Faz 11 Multi-Currency notu).
 * Insight'lar kalıcı bir tabloya yazılmaz, her çağrıda güncel veriden
 * hesaplanır — bilinçli bir sadeleştirme (bkz. sohbet geçmişi).
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal SPENDING_CHANGE_THRESHOLD = BigDecimal.valueOf(10);
    private static final BigDecimal CATEGORY_CHANGE_THRESHOLD = BigDecimal.valueOf(20);

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final BudgetService budgetService;

    @Transactional(readOnly = true)
    public DashboardResponse dashboard(Integer year, Integer month, Currency currencyParam) {
        UUID userId = CurrentUserProvider.getUserId();
        Currency currency = resolveCurrency(currencyParam, userId);
        YearMonth period = resolvePeriod(year, month);
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();

        BigDecimal income = transactionRepository.sumAmountByTypeAndDateRange(userId, TransactionType.INCOME, currency, start, end);
        BigDecimal expense = transactionRepository.sumAmountByTypeAndDateRange(userId, TransactionType.EXPENSE, currency, start, end);
        BigDecimal savings = income.subtract(expense);
        BigDecimal savingsRate = percentOf(savings, income);

        BigDecimal subscriptionCost = subscriptionRepository.findByUserIdOrderByNextBillingDateAsc(userId).stream()
                .filter(Subscription::isActive)
                .filter(s -> s.getCurrency() == currency)
                .map(AnalyticsService::normalizeToMonthly)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TransactionRepository.CategoryAmountProjection> categoryTotals =
                transactionRepository.sumByCategoryForDateRange(userId, TransactionType.EXPENSE, currency, start, end);
        CategoryAmount topSpendingCategory = categoryTotals.isEmpty() ? null
                : new CategoryAmount(categoryTotals.get(0).getCategoryId(), categoryTotals.get(0).getCategoryName(), categoryTotals.get(0).getTotal());

        Optional<Transaction> highest = transactionRepository
                .findFirstByUserIdAndTypeAndCurrencyAndTransactionDateBetweenOrderByAmountDesc(userId, TransactionType.EXPENSE, currency, start, end);
        DashboardResponse.HighestExpense highestExpense = highest.map(t -> new DashboardResponse.HighestExpense(
                t.getId(), t.getCategory() != null ? t.getCategory().getName() : null, t.getAmount(), t.getTransactionDate(), t.getDescription()
        )).orElse(null);

        List<DashboardResponse.BudgetUsage> budgetUsage = budgetRepository.findByUserIdOrderByStartDateDesc(userId).stream()
                .filter(b -> overlaps(b, start, end))
                .map(b -> {
                    var status = budgetService.computeStatus(b);
                    String scope = b.getCategory() != null ? b.getCategory().getName() : "Genel";
                    return new DashboardResponse.BudgetUsage(b.getId(), scope, status.usagePercentage(), status.status());
                })
                .toList();

        return new DashboardResponse(period.toString(), currency, income, expense, savings, savingsRate,
                subscriptionCost, topSpendingCategory, highestExpense, budgetUsage);
    }

    @Transactional(readOnly = true)
    public List<CategoryDistributionItem> categoryDistribution(Integer year, Integer month, Currency currencyParam) {
        UUID userId = CurrentUserProvider.getUserId();
        Currency currency = resolveCurrency(currencyParam, userId);
        YearMonth period = resolvePeriod(year, month);

        List<TransactionRepository.CategoryAmountProjection> rows = transactionRepository.sumByCategoryForDateRange(
                userId, TransactionType.EXPENSE, currency, period.atDay(1), period.atEndOfMonth());

        BigDecimal total = rows.stream().map(TransactionRepository.CategoryAmountProjection::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream()
                .map(r -> new CategoryDistributionItem(r.getCategoryId(), r.getCategoryName(), r.getTotal(), percentOf(r.getTotal(), total)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrendPoint> trends(int months, Currency currencyParam) {
        UUID userId = CurrentUserProvider.getUserId();
        Currency currency = resolveCurrency(currencyParam, userId);

        List<TrendPoint> points = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth period = current.minusMonths(i);
            BigDecimal income = transactionRepository.sumAmountByTypeAndDateRange(
                    userId, TransactionType.INCOME, currency, period.atDay(1), period.atEndOfMonth());
            BigDecimal expense = transactionRepository.sumAmountByTypeAndDateRange(
                    userId, TransactionType.EXPENSE, currency, period.atDay(1), period.atEndOfMonth());
            points.add(new TrendPoint(period.toString(), income, expense, income.subtract(expense)));
        }
        return points;
    }

    @Transactional(readOnly = true)
    public ComparisonResponse comparison(ComparisonGranularity granularity, Currency currencyParam) {
        UUID userId = CurrentUserProvider.getUserId();
        Currency currency = resolveCurrency(currencyParam, userId);

        LocalDate[] currentRange;
        LocalDate[] previousRange;
        String currentLabel;
        String previousLabel;

        if (granularity == ComparisonGranularity.YEAR) {
            int year = LocalDate.now().getYear();
            currentRange = new LocalDate[]{LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)};
            previousRange = new LocalDate[]{LocalDate.of(year - 1, 1, 1), LocalDate.of(year - 1, 12, 31)};
            currentLabel = String.valueOf(year);
            previousLabel = String.valueOf(year - 1);
        } else {
            YearMonth current = YearMonth.now();
            YearMonth previous = current.minusMonths(1);
            currentRange = new LocalDate[]{current.atDay(1), current.atEndOfMonth()};
            previousRange = new LocalDate[]{previous.atDay(1), previous.atEndOfMonth()};
            currentLabel = current.toString();
            previousLabel = previous.toString();
        }

        var currentSummary = periodSummary(userId, currency, currentLabel, currentRange[0], currentRange[1]);
        var previousSummary = periodSummary(userId, currency, previousLabel, previousRange[0], previousRange[1]);

        return new ComparisonResponse(
                currentSummary, previousSummary,
                percentChange(previousSummary.income(), currentSummary.income()),
                percentChange(previousSummary.expense(), currentSummary.expense()),
                percentChange(previousSummary.savings(), currentSummary.savings())
        );
    }

    @Transactional(readOnly = true)
    public List<Insight> insights(Currency currencyParam) {
        UUID userId = CurrentUserProvider.getUserId();
        Currency currency = resolveCurrency(currencyParam, userId);

        YearMonth current = YearMonth.now();
        YearMonth previous = current.minusMonths(1);

        BigDecimal currentExpense = transactionRepository.sumAmountByTypeAndDateRange(userId, TransactionType.EXPENSE, currency, current.atDay(1), current.atEndOfMonth());
        BigDecimal previousExpense = transactionRepository.sumAmountByTypeAndDateRange(userId, TransactionType.EXPENSE, currency, previous.atDay(1), previous.atEndOfMonth());
        BigDecimal currentIncome = transactionRepository.sumAmountByTypeAndDateRange(userId, TransactionType.INCOME, currency, current.atDay(1), current.atEndOfMonth());
        BigDecimal previousIncome = transactionRepository.sumAmountByTypeAndDateRange(userId, TransactionType.INCOME, currency, previous.atDay(1), previous.atEndOfMonth());

        List<Insight> insights = new ArrayList<>();
        addSpendingChangeInsight(insights, previousExpense, currentExpense, currency);
        addIncomeExpenseRatioInsight(insights, currentIncome, currentExpense, currency);
        addSubscriptionAnnualCostInsight(insights, userId, currency);
        addSavingsTrendInsight(insights, previousIncome.subtract(previousExpense), currentIncome.subtract(currentExpense), currency);
        addCategorySpendingChangeInsight(insights, userId, currency, previous, current);
        return insights;
    }

    private void addSpendingChangeInsight(List<Insight> insights, BigDecimal previousExpense, BigDecimal currentExpense, Currency currency) {
        if (previousExpense.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        BigDecimal change = percentChange(previousExpense, currentExpense);
        if (change.abs().compareTo(SPENDING_CHANGE_THRESHOLD) < 0) {
            return;
        }
        boolean increased = change.signum() > 0;
        insights.add(new Insight(
                InsightType.SPENDING_CHANGE,
                increased ? "Harcamalar Arttı" : "Harcamalar Azaldı",
                "Bu ay harcamaların geçen aya göre %%%s %s (%s %s)".formatted(
                        change.abs(), increased ? "arttı" : "azaldı", currentExpense, currency)
        ));
    }

    private void addIncomeExpenseRatioInsight(List<Insight> insights, BigDecimal income, BigDecimal expense, Currency currency) {
        if (expense.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        BigDecimal ratio = income.divide(expense, 2, RoundingMode.HALF_UP);
        String verdict = ratio.compareTo(BigDecimal.ONE) < 0
                ? "Giderlerin gelirini aşıyor, dikkat!"
                : ratio.compareTo(BigDecimal.valueOf(1.2)) < 0 ? "Dengeli ama sınırda" : "Sağlıklı";
        insights.add(new Insight(
                InsightType.INCOME_EXPENSE_RATIO,
                "Gelir/Gider Oranı",
                "Bu ay gelir/gider oranın %s (%s). %s".formatted(ratio, currency, verdict)
        ));
    }

    private void addSubscriptionAnnualCostInsight(List<Insight> insights, UUID userId, Currency currency) {
        List<Subscription> active = subscriptionRepository.findByUserIdOrderByNextBillingDateAsc(userId).stream()
                .filter(Subscription::isActive)
                .filter(s -> s.getCurrency() == currency)
                .toList();
        if (active.isEmpty()) {
            return;
        }
        BigDecimal annualCost = active.stream().map(AnalyticsService::normalizeToAnnual).reduce(BigDecimal.ZERO, BigDecimal::add);
        insights.add(new Insight(
                InsightType.SUBSCRIPTION_ANNUAL_COST,
                "Yıllık Abonelik Maliyeti",
                "%d aktif aboneliğin toplam yıllık maliyeti: %s %s".formatted(active.size(), annualCost, currency)
        ));
    }

    private void addSavingsTrendInsight(List<Insight> insights, BigDecimal previousSavings, BigDecimal currentSavings, Currency currency) {
        BigDecimal diff = currentSavings.subtract(previousSavings);
        if (diff.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        boolean improved = diff.signum() > 0;
        insights.add(new Insight(
                InsightType.SAVINGS_TREND,
                improved ? "Tasarruf Trendin Yükselişte" : "Tasarruf Trendin Düşüşte",
                "Bu ay net tasarrufun geçen aya göre %s %s %s".formatted(
                        diff.abs(), currency, improved ? "arttı" : "azaldı")
        ));
    }

    private void addCategorySpendingChangeInsight(List<Insight> insights, UUID userId, Currency currency, YearMonth previous, YearMonth current) {
        var currentRows = transactionRepository.sumByCategoryForDateRange(userId, TransactionType.EXPENSE, currency, current.atDay(1), current.atEndOfMonth());
        var previousRows = transactionRepository.sumByCategoryForDateRange(userId, TransactionType.EXPENSE, currency, previous.atDay(1), previous.atEndOfMonth());

        BigDecimal biggestIncrease = CATEGORY_CHANGE_THRESHOLD;
        String biggestCategory = null;
        BigDecimal biggestCategoryAmount = null;

        for (var currentRow : currentRows) {
            BigDecimal previousAmount = previousRows.stream()
                    .filter(p -> p.getCategoryId().equals(currentRow.getCategoryId()))
                    .map(TransactionRepository.CategoryAmountProjection::getTotal)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
            if (previousAmount.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            BigDecimal change = percentChange(previousAmount, currentRow.getTotal());
            if (change.compareTo(biggestIncrease) > 0) {
                biggestIncrease = change;
                biggestCategory = currentRow.getCategoryName();
                biggestCategoryAmount = currentRow.getTotal();
            }
        }

        if (biggestCategory != null) {
            insights.add(new Insight(
                    InsightType.CATEGORY_SPENDING_CHANGE,
                    "%s Harcaman Yükseldi".formatted(biggestCategory),
                    "%s kategorisindeki harcaman geçen aya göre %%%s arttı (%s %s)".formatted(
                            biggestCategory, biggestIncrease, biggestCategoryAmount, currency)
            ));
        }
    }

    private ComparisonResponse.PeriodSummary periodSummary(UUID userId, Currency currency, String label, LocalDate start, LocalDate end) {
        BigDecimal income = transactionRepository.sumAmountByTypeAndDateRange(userId, TransactionType.INCOME, currency, start, end);
        BigDecimal expense = transactionRepository.sumAmountByTypeAndDateRange(userId, TransactionType.EXPENSE, currency, start, end);
        return new ComparisonResponse.PeriodSummary(label, income, expense, income.subtract(expense));
    }

    private boolean overlaps(Budget budget, LocalDate start, LocalDate end) {
        return !budget.getStartDate().isAfter(end) && !budget.getEndDate().isBefore(start);
    }

    private Currency resolveCurrency(Currency currencyParam, UUID userId) {
        if (currencyParam != null) {
            return currencyParam;
        }
        User user = userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        return user.getDefaultCurrency();
    }

    private YearMonth resolvePeriod(Integer year, Integer month) {
        return (year != null && month != null) ? YearMonth.of(year, month) : YearMonth.now();
    }

    /** Package-private (private değil): AnalyticsServiceTest saf matematiği izole test edebilsin diye. */
    BigDecimal percentOf(BigDecimal part, BigDecimal whole) {
        if (whole.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return part.divide(whole, 4, RoundingMode.HALF_UP).multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }

    /** {@code previous} sıfırsa: değişim ancak {@code current} da sıfırsa 0, aksi halde tam artış (%100) kabul edilir. */
    BigDecimal percentChange(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : HUNDRED;
        }
        return current.subtract(previous).divide(previous.abs(), 4, RoundingMode.HALF_UP).multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal normalizeToMonthly(Subscription subscription) {
        return switch (subscription.getBillingCycle()) {
            case WEEKLY -> subscription.getAmount().multiply(BigDecimal.valueOf(52)).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            case MONTHLY -> subscription.getAmount();
            case YEARLY -> subscription.getAmount().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        };
    }

    private static BigDecimal normalizeToAnnual(Subscription subscription) {
        return switch (subscription.getBillingCycle()) {
            case WEEKLY -> subscription.getAmount().multiply(BigDecimal.valueOf(52));
            case MONTHLY -> subscription.getAmount().multiply(BigDecimal.valueOf(12));
            case YEARLY -> subscription.getAmount();
        };
    }
}
