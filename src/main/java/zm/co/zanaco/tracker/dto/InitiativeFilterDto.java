package zm.co.zanaco.tracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;
import zm.co.zanaco.tracker.domain.enums.Priority;

/**
 * All fields are optional; null means "no filter on this dimension".
 */
public record InitiativeFilterDto(
        @Min(2000) Integer year,
        @Min(1) @Max(4) Integer quarter,
        InitiativeStatus status,
        Priority priority,
        String sourceDepartment
) {
    public static InitiativeFilterDto empty() {
        return new InitiativeFilterDto(null, null, null, null, null);
    }
}
