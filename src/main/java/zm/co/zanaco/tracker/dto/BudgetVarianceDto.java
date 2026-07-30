package zm.co.zanaco.tracker.dto;

import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;

import java.math.BigDecimal;

/**
 * Per-initiative budget vs actual cost row for the variance report.
 * {@code variance = totalBudget - totalActualCost}; negative means over-spend.
 */
public record BudgetVarianceDto(
        Long initiativeId,
        String projectCode,
        String title,
        InitiativeStatus status,
        BigDecimal totalBudget,
        BigDecimal totalActualCost,
        BigDecimal variance,
        boolean overBudget
) {}
