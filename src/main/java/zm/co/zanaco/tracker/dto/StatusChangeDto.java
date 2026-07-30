package zm.co.zanaco.tracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;

public record StatusChangeDto(

        @NotNull
        InitiativeStatus newStatus,

        @Size(max = 100)
        String changedBy,

        String comment
) {}
