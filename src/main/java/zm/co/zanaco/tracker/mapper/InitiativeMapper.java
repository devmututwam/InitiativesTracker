package zm.co.zanaco.tracker.mapper;

import org.springframework.stereotype.Component;
import zm.co.zanaco.tracker.domain.Initiative;
import zm.co.zanaco.tracker.dto.InitiativeCreateDto;
import zm.co.zanaco.tracker.dto.InitiativeResponseDto;
import zm.co.zanaco.tracker.dto.InitiativeUpdateDto;

@Component
public class InitiativeMapper {

    public Initiative toEntity(InitiativeCreateDto dto) {
        Initiative initiative = new Initiative();
        initiative.setProjectCode(dto.projectCode());
        initiative.setTitle(dto.title());
        initiative.setDescription(dto.description());
        initiative.setSourceDepartment(dto.sourceDepartment());
        initiative.setPriority(dto.priority());
        initiative.setStatus(dto.status());
        initiative.setStartDate(dto.startDate());
        initiative.setExpectedEndDate(dto.expectedEndDate());
        initiative.setYear(dto.year());
        initiative.setQuarter(dto.quarter());
        return initiative;
    }

    /** Applies only non-null fields from the DTO (partial / patch semantics). */
    public void updateEntity(Initiative initiative, InitiativeUpdateDto dto) {
        if (dto.projectCode() != null)    initiative.setProjectCode(dto.projectCode());
        if (dto.title() != null)          initiative.setTitle(dto.title());
        if (dto.description() != null)    initiative.setDescription(dto.description());
        if (dto.sourceDepartment() != null) initiative.setSourceDepartment(dto.sourceDepartment());
        if (dto.priority() != null)       initiative.setPriority(dto.priority());
        if (dto.status() != null)         initiative.setStatus(dto.status());
        if (dto.startDate() != null)      initiative.setStartDate(dto.startDate());
        if (dto.expectedEndDate() != null) initiative.setExpectedEndDate(dto.expectedEndDate());
        if (dto.actualEndDate() != null)  initiative.setActualEndDate(dto.actualEndDate());
        if (dto.year() != null)           initiative.setYear(dto.year());
        if (dto.quarter() != null)        initiative.setQuarter(dto.quarter());
    }

    public InitiativeResponseDto toResponseDto(Initiative initiative) {
        return new InitiativeResponseDto(
                initiative.getId(),
                initiative.getProjectCode(),
                initiative.getTitle(),
                initiative.getDescription(),
                initiative.getSourceDepartment(),
                initiative.getPriority(),
                initiative.getStatus(),
                initiative.getStartDate(),
                initiative.getExpectedEndDate(),
                initiative.getActualEndDate(),
                initiative.getYear(),
                initiative.getQuarter(),
                initiative.getCreatedAt(),
                initiative.getUpdatedAt()
        );
    }
}
