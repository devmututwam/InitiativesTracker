package zm.co.zanaco.tracker.mapper;

import org.springframework.stereotype.Component;
import zm.co.zanaco.tracker.domain.Budget;
import zm.co.zanaco.tracker.domain.Initiative;
import zm.co.zanaco.tracker.dto.BudgetDto;
import zm.co.zanaco.tracker.dto.BudgetResponseDto;

@Component
public class BudgetMapper {

    public Budget toEntity(Initiative initiative, BudgetDto dto) {
        Budget budget = new Budget();
        budget.setInitiative(initiative);
        budget.setAmount(dto.amount());
        budget.setCurrency(dto.currency());
        budget.setApprovedDate(dto.approvedDate());
        budget.setBudgetSource(dto.budgetSource());
        return budget;
    }

    public void updateEntity(Budget budget, BudgetDto dto) {
        if (dto.amount() != null)       budget.setAmount(dto.amount());
        if (dto.currency() != null)     budget.setCurrency(dto.currency());
        if (dto.approvedDate() != null) budget.setApprovedDate(dto.approvedDate());
        if (dto.budgetSource() != null) budget.setBudgetSource(dto.budgetSource());
    }

    public BudgetResponseDto toResponseDto(Budget budget) {
        return new BudgetResponseDto(
                budget.getId(),
                budget.getInitiative().getId(),
                budget.getAmount(),
                budget.getCurrency(),
                budget.getApprovedDate(),
                budget.getBudgetSource()
        );
    }
}
