package zm.co.zanaco.tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SavingsRecordResponseDto(
        Long id,
        Long initiativeId,
        String projectCode,
        BigDecimal vendorBudget,
        BigDecimal internalCost,
        BigDecimal incrementalExpenses,
        BigDecimal savingAmount,
        /** true when saving >= 0, false when costs exceed vendor budget */
        boolean positiveSaving,
        String calculatedBy,
        LocalDateTime calculatedAt,
        String notes
) {}
