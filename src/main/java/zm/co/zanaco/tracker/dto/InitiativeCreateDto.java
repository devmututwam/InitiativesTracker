package zm.co.zanaco.tracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;
import zm.co.zanaco.tracker.domain.enums.Priority;

import java.time.LocalDate;

public record InitiativeCreateDto(

        @NotBlank @Size(max = 50)
        String projectCode,

        @NotBlank @Size(max = 200)
        String title,

        String description,

        @Size(max = 100)
        String sourceDepartment,

        @NotNull
        Priority priority,

        @NotNull
        InitiativeStatus status,

        LocalDate startDate,

        LocalDate expectedEndDate,

        @NotNull @Min(2000)
        Integer year,

        @Min(1) @Max(4)
        Integer quarter
) {}
