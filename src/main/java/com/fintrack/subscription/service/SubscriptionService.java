package com.fintrack.subscription.service;

import com.fintrack.category.entity.Category;
import com.fintrack.category.service.CategoryAccessService;
import com.fintrack.common.exception.ForbiddenException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.security.CurrentUserProvider;
import com.fintrack.subscription.dto.CreateSubscriptionRequest;
import com.fintrack.subscription.dto.SubscriptionResponse;
import com.fintrack.subscription.entity.Subscription;
import com.fintrack.subscription.repository.SubscriptionRepository;
import com.fintrack.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CategoryAccessService categoryAccessService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> list() {
        UUID userId = CurrentUserProvider.getUserId();
        return subscriptionRepository.findByUserIdOrderByNextBillingDateAsc(userId).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest request) {
        UUID userId = CurrentUserProvider.getUserId();
        Category category = categoryAccessService.resolveOwnedOrSystemOrNull(request.categoryId(), userId);

        Subscription subscription = Subscription.builder()
                .user(userRepository.getReferenceById(userId))
                .category(category)
                .name(request.name())
                .amount(request.amount())
                .currency(request.currency())
                .billingCycle(request.billingCycle())
                .nextBillingDate(request.nextBillingDate())
                .active(true)
                .build();
        subscriptionRepository.save(subscription);
        return SubscriptionResponse.from(subscription);
    }

    @Transactional
    public SubscriptionResponse update(UUID id, CreateSubscriptionRequest request) {
        Subscription subscription = findOwned(id);
        Category category = categoryAccessService.resolveOwnedOrSystemOrNull(request.categoryId(), subscription.getUser().getId());

        subscription.setCategory(category);
        subscription.setName(request.name());
        subscription.setAmount(request.amount());
        subscription.setCurrency(request.currency());
        subscription.setBillingCycle(request.billingCycle());
        subscription.setNextBillingDate(request.nextBillingDate());

        return SubscriptionResponse.from(subscription);
    }

    @Transactional
    public SubscriptionResponse cancel(UUID id) {
        Subscription subscription = findOwned(id);
        subscription.setActive(false);
        return SubscriptionResponse.from(subscription);
    }

    @Transactional
    public void delete(UUID id) {
        subscriptionRepository.delete(findOwned(id));
    }

    private Subscription findOwned(UUID id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Subscription", id));
        if (!subscription.getUser().getId().equals(CurrentUserProvider.getUserId())) {
            throw new ForbiddenException("Bu aboneliğe erişim yetkin yok");
        }
        return subscription;
    }
}
