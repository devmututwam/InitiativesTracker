package zm.co.zanaco.tracker.dto;

import zm.co.zanaco.tracker.domain.enums.CostType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CostEntryResponseDto(
        Long id,
        Long initiativeId,
        CostType costType,
        BigDecimal amount,
        String currency,
        LocalDate recordedDate,
        String notes
) {}
