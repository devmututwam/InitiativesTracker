package zm.co.zanaco.tracker.dto;

import java.math.BigDecimal;

/**
 * Aggregated totals for a given year / quarter (both optional).
 * Produced by a single native SQL query for performance.
 */
public record SummaryReportDto(
        Integer year,
        Integer quarter,
        long totalInitiatives,
        long wipCount,
        long uatCount,
        long completedCount,
        BigDecimal totalBudget,
        BigDecimal totalActualCost,
        BigDecimal totalSavings,
        /** totalBudget - totalActualCost; negative means over-spend. */
        BigDecimal netVariance
) {}
