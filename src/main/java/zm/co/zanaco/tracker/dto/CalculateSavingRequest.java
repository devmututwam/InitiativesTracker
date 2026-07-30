package zm.co.zanaco.tracker.dto;

import jakarta.validation.constraints.Size;

/**
 * Optional request body for POST /api/initiatives/{id}/calculate-saving.
 * Both fields are nullable; omitting the body entirely is also accepted.
 */
public record CalculateSavingRequest(
        @Size(max = 100) String calculatedBy,
        String notes
) {
    public static CalculateSavingRequest empty() {
        return new CalculateSavingRequest(null, null);
    }
}
