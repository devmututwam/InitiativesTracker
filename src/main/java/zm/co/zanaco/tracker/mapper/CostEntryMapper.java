package zm.co.zanaco.tracker.mapper;

import org.springframework.stereotype.Component;
import zm.co.zanaco.tracker.domain.CostEntry;
import zm.co.zanaco.tracker.domain.Initiative;
import zm.co.zanaco.tracker.dto.CostEntryDto;
import zm.co.zanaco.tracker.dto.CostEntryResponseDto;

@Component
public class CostEntryMapper {

    public CostEntry toEntity(Initiative initiative, CostEntryDto dto) {
        CostEntry entry = new CostEntry();
        entry.setInitiative(initiative);
        entry.setCostType(dto.costType());
        entry.setAmount(dto.amount());
        entry.setCurrency(dto.currency());
        entry.setRecordedDate(dto.recordedDate());
        entry.setNotes(dto.notes());
        return entry;
    }

    public void updateEntity(CostEntry entry, CostEntryDto dto) {
        if (dto.costType() != null)     entry.setCostType(dto.costType());
        if (dto.amount() != null)       entry.setAmount(dto.amount());
        if (dto.currency() != null)     entry.setCurrency(dto.currency());
        if (dto.recordedDate() != null) entry.setRecordedDate(dto.recordedDate());
        if (dto.notes() != null)        entry.setNotes(dto.notes());
    }

    public CostEntryResponseDto toResponseDto(CostEntry entry) {
        return new CostEntryResponseDto(
                entry.getId(),
                entry.getInitiative().getId(),
                entry.getCostType(),
                entry.getAmount(),
                entry.getCurrency(),
                entry.getRecordedDate(),
                entry.getNotes()
        );
    }
}
