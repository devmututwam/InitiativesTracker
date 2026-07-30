package zm.co.zanaco.tracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;
import zm.co.zanaco.tracker.domain.enums.Priority;

import java.time.LocalDate;

/**
 * All fields are optional — only non-null values are applied during an update.
 */
public record InitiativeUpdateDto(

        @Size(max = 50)
        String projectCode,

        @Size(max = 200)
        String title,

        String description,

        @Size(max = 100)
        String sourceDepartment,

        Priority priority,

        InitiativeStatus status,

        LocalDate startDate,

        LocalDate expectedEndDate,

        LocalDate actualEndDate,

        @Min(2000)
        Integer year,

        @Min(1) @Max(4)
        Integer quarter
) {}
