package zm.co.zanaco.tracker.dto;

import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;
import zm.co.zanaco.tracker.domain.enums.Priority;

import java.math.BigDecimal;

/**
 * Flat summary of an initiative with aggregated budget and actual cost totals.
 * Used as the constructor target in the InitiativeRepository aggregation query.
 */
public record InitiativeCostSummary(
        Long id,
        String projectCode,
        String title,
        Priority priority,
        InitiativeStatus status,
        Integer year,
        Integer quarter,
        BigDecimal totalBudget,
        BigDecimal totalCost
) {}
