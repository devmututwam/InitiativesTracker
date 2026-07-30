package zm.co.zanaco.tracker.dto;

import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;
import zm.co.zanaco.tracker.domain.enums.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InitiativeResponseDto(
        Long id,
        String projectCode,
        String title,
        String description,
        String sourceDepartment,
        Priority priority,
        InitiativeStatus status,
        LocalDate startDate,
        LocalDate expectedEndDate,
        LocalDate actualEndDate,
        Integer year,
        Integer quarter,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
