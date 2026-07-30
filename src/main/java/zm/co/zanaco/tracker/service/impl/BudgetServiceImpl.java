package zm.co.zanaco.tracker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.co.zanaco.tracker.domain.Budget;
import zm.co.zanaco.tracker.domain.Initiative;
import zm.co.zanaco.tracker.dto.BudgetDto;
import zm.co.zanaco.tracker.dto.BudgetResponseDto;
import zm.co.zanaco.tracker.exception.ResourceNotFoundException;
import zm.co.zanaco.tracker.mapper.BudgetMapper;
import zm.co.zanaco.tracker.repository.BudgetRepository;
import zm.co.zanaco.tracker.repository.InitiativeRepository;
import zm.co.zanaco.tracker.service.BudgetService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final InitiativeRepository initiativeRepository;
    private final BudgetMapper budgetMapper;

    @Override
    @Transactional
    public BudgetResponseDto attachBudget(Long initiativeId, BudgetDto dto) {
        Initiative initiative = findInitiativeOrThrow(initiativeId);
        Budget budget = budgetMapper.toEntity(initiative, dto);
        return budgetMapper.toResponseDto(budgetRepository.save(budget));
    }

    @Override
    @Transactional
    public BudgetResponseDto updateBudget(Long budgetId, BudgetDto dto) {
        Budget budget = findBudgetOrThrow(budgetId);
        budgetMapper.updateEntity(budget, dto);
        return budgetMapper.toResponseDto(budgetRepository.save(budget));
    }

    @Override
    public BudgetResponseDto getBudget(Long budgetId) {
        return budgetMapper.toResponseDto(findBudgetOrThrow(budgetId));
    }

    @Override
    public List<BudgetResponseDto> getBudgetsForInitiative(Long initiativeId) {
        findInitiativeOrThrow(initiativeId);
        return budgetRepository.findAllByInitiativeId(initiativeId)
                .stream()
                .map(budgetMapper::toResponseDto)
                .toList();
    }

    @Override
    public BigDecimal getTotalBudget(Long initiativeId) {
        findInitiativeOrThrow(initiativeId);
        BigDecimal total = budgetRepository.sumAmountByInitiativeId(initiativeId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public void deleteBudget(Long budgetId) {
        Budget budget = findBudgetOrThrow(budgetId);
        budgetRepository.delete(budget);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Initiative findInitiativeOrThrow(Long id) {
        return initiativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", id));
    }

    private Budget findBudgetOrThrow(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
    }
}
