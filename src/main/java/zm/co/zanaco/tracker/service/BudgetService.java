package zm.co.zanaco.tracker.service;

import zm.co.zanaco.tracker.dto.BudgetDto;
import zm.co.zanaco.tracker.dto.BudgetResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface BudgetService {

    BudgetResponseDto attachBudget(Long initiativeId, BudgetDto dto);

    BudgetResponseDto updateBudget(Long budgetId, BudgetDto dto);

    BudgetResponseDto getBudget(Long budgetId);

    List<BudgetResponseDto> getBudgetsForInitiative(Long initiativeId);

    BigDecimal getTotalBudget(Long initiativeId);

    void deleteBudget(Long budgetId);
}
