package zm.co.zanaco.tracker.mapper;

import org.springframework.stereotype.Component;
import zm.co.zanaco.tracker.domain.SavingsRecord;
import zm.co.zanaco.tracker.dto.SavingsRecordResponseDto;

import java.math.BigDecimal;

@Component
public class SavingsRecordMapper {

    public SavingsRecordResponseDto toResponseDto(SavingsRecord record) {
        return new SavingsRecordResponseDto(
                record.getId(),
                record.getInitiative().getId(),
                record.getInitiative().getProjectCode(),
                record.getVendorBudget(),
                record.getInternalCost(),
                record.getIncrementalExpenses(),
                record.getSavingAmount(),
                record.getSavingAmount().compareTo(BigDecimal.ZERO) >= 0,
                record.getCalculatedBy(),
                record.getCalculatedAt(),
                record.getNotes()
        );
    }
}
