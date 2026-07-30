package zm.co.zanaco.tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetResponseDto(
        Long id,
        Long initiativeId,
        BigDecimal amount,
        String currency,
        LocalDate approvedDate,
        String budgetSource
) {}
