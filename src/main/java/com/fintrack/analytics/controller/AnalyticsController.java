package com.fintrack.analytics.controller;

import com.fintrack.analytics.dto.CategoryDistributionItem;
import com.fintrack.analytics.dto.ComparisonGranularity;
import com.fintrack.analytics.dto.ComparisonResponse;
import com.fintrack.analytics.dto.DashboardResponse;
import com.fintrack.analytics.dto.Insight;
import com.fintrack.analytics.dto.TrendPoint;
import com.fintrack.analytics.service.AnalyticsService;
import com.fintrack.common.dto.ApiResponse;
import com.fintrack.common.enums.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> dashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Currency currency
    ) {
        return ApiResponse.of(analyticsService.dashboard(year, month, currency));
    }

    @GetMapping("/category-distribution")
    public ApiResponse<List<CategoryDistributionItem>> categoryDistribution(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Currency currency
    ) {
        return ApiResponse.of(analyticsService.categoryDistribution(year, month, currency));
    }

    @GetMapping("/trends")
    public ApiResponse<List<TrendPoint>> trends(
            @RequestParam(defaultValue = "6") int months,
            @RequestParam(required = false) Currency currency
    ) {
        return ApiResponse.of(analyticsService.trends(months, currency));
    }

    @GetMapping("/comparison")
    public ApiResponse<ComparisonResponse> comparison(
            @RequestParam(defaultValue = "MONTH") ComparisonGranularity granularity,
            @RequestParam(required = false) Currency currency
    ) {
        return ApiResponse.of(analyticsService.comparison(granularity, currency));
    }

    @GetMapping("/insights")
    public ApiResponse<List<Insight>> insights(@RequestParam(required = false) Currency currency) {
        return ApiResponse.of(analyticsService.insights(currency));
    }
}
