package zm.co.zanaco.tracker.dto;

import java.math.BigDecimal;

/**
 * Result of the savings calculation for an initiative.
 * <p>
 * saving = vendorBudget - internalCost - incrementalExpenses
 */
public record SavingsDto(
        Long initiativeId,
        String projectCode,
        BigDecimal vendorBudget,
        BigDecimal internalCost,
        BigDecimal incrementalExpenses,
        BigDecimal saving
) {}
