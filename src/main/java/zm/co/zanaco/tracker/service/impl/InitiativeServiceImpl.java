package zm.co.zanaco.tracker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.co.zanaco.tracker.domain.Budget;
import zm.co.zanaco.tracker.domain.Initiative;
import zm.co.zanaco.tracker.domain.SavingsRecord;
import zm.co.zanaco.tracker.domain.StatusHistory;
import zm.co.zanaco.tracker.domain.enums.CostType;
import zm.co.zanaco.tracker.dto.CalculateSavingRequest;
import zm.co.zanaco.tracker.dto.InitiativeFilterDto;
import zm.co.zanaco.tracker.dto.InitiativeCreateDto;
import zm.co.zanaco.tracker.dto.InitiativeCostSummary;
import zm.co.zanaco.tracker.dto.InitiativeResponseDto;
import zm.co.zanaco.tracker.dto.InitiativeUpdateDto;
import zm.co.zanaco.tracker.dto.SavingsRecordResponseDto;
import zm.co.zanaco.tracker.dto.StatusChangeDto;
import zm.co.zanaco.tracker.exception.ResourceNotFoundException;
import zm.co.zanaco.tracker.mapper.InitiativeMapper;
import zm.co.zanaco.tracker.mapper.SavingsRecordMapper;
import zm.co.zanaco.tracker.repository.BudgetRepository;
import zm.co.zanaco.tracker.repository.CostEntryRepository;
import zm.co.zanaco.tracker.repository.InitiativeRepository;
import zm.co.zanaco.tracker.repository.InitiativeSpecification;
import zm.co.zanaco.tracker.repository.SavingsRecordRepository;
import zm.co.zanaco.tracker.repository.StatusHistoryRepository;
import zm.co.zanaco.tracker.service.InitiativeService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InitiativeServiceImpl implements InitiativeService {

    /** Cost types that represent internal delivery effort and are deducted from the vendor quote. */
    static final Set<CostType> INTERNAL_COST_TYPES = Set.of(
            CostType.INTERNAL_HOURS,
            CostType.INFRA,
            CostType.LICENSE
    );

    private final InitiativeRepository initiativeRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final BudgetRepository budgetRepository;
    private final CostEntryRepository costEntryRepository;
    private final SavingsRecordRepository savingsRecordRepository;
    private final InitiativeMapper initiativeMapper;
    private final SavingsRecordMapper savingsRecordMapper;

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public InitiativeResponseDto createInitiative(InitiativeCreateDto dto) {
        if (initiativeRepository.existsByProjectCode(dto.projectCode())) {
            throw new IllegalArgumentException(
                    "An initiative with project code '%s' already exists".formatted(dto.projectCode()));
        }
        return initiativeMapper.toResponseDto(
                initiativeRepository.save(initiativeMapper.toEntity(dto)));
    }

    @Override
    public InitiativeResponseDto getInitiative(Long id) {
        return initiativeMapper.toResponseDto(findOrThrow(id));
    }

    @Override
    @Transactional
    public InitiativeResponseDto updateInitiative(Long id, InitiativeUpdateDto dto) {
        Initiative initiative = findOrThrow(id);
        if (dto.projectCode() != null
                && !dto.projectCode().equals(initiative.getProjectCode())
                && initiativeRepository.existsByProjectCode(dto.projectCode())) {
            throw new IllegalArgumentException(
                    "An initiative with project code '%s' already exists".formatted(dto.projectCode()));
        }
        initiativeMapper.updateEntity(initiative, dto);
        return initiativeMapper.toResponseDto(initiativeRepository.save(initiative));
    }

    // -------------------------------------------------------------------------
    // Status change
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void changeStatus(Long id, StatusChangeDto dto) {
        Initiative initiative = findOrThrow(id);

        StatusHistory history = new StatusHistory();
        history.setInitiative(initiative);
        history.setOldStatus(initiative.getStatus());
        history.setNewStatus(dto.newStatus());
        history.setChangedBy(dto.changedBy());
        history.setChangedAt(LocalDateTime.now());
        history.setComment(dto.comment());

        initiative.setStatus(dto.newStatus());
        statusHistoryRepository.save(history);
    }

    // -------------------------------------------------------------------------
    // Savings calculation
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public SavingsRecordResponseDto calculateSavings(Long id, CalculateSavingRequest request) {
        Initiative initiative = findOrThrow(id);

        // Require an explicit vendor budget record to have a meaningful baseline
        Budget vendorBudgetRecord = budgetRepository
                .findFirstByInitiativeIdAndBudgetSourceIgnoreCase(id, "Vendor")
                .orElseThrow(() -> new IllegalArgumentException(
                        ("No vendor budget found for initiative '%s'. "
                        + "Attach a Budget with budgetSource='Vendor' before calculating savings.")
                                .formatted(initiative.getProjectCode())));

        BigDecimal vendorBudget = vendorBudgetRecord.getAmount();

        BigDecimal internalCost = nullSafe(
                costEntryRepository.sumByCostTypesInAndInitiativeId(id, INTERNAL_COST_TYPES));

        BigDecimal incrementalExpenses = nullSafe(
                costEntryRepository.sumByCostTypesNotInAndInitiativeId(id, INTERNAL_COST_TYPES));

        BigDecimal savingAmount = vendorBudget
                .subtract(internalCost)
                .subtract(incrementalExpenses);

        SavingsRecord record = new SavingsRecord();
        record.setInitiative(initiative);
        record.setVendorBudget(vendorBudget);
        record.setInternalCost(internalCost);
        record.setIncrementalExpenses(incrementalExpenses);
        record.setSavingAmount(savingAmount);
        record.setCalculatedBy(request != null ? request.calculatedBy() : null);
        record.setCalculatedAt(LocalDateTime.now());
        record.setNotes(request != null ? request.notes() : null);

        return savingsRecordMapper.toResponseDto(savingsRecordRepository.save(record));
    }

    // -------------------------------------------------------------------------
    // Listing
    // -------------------------------------------------------------------------

    @Override
    public Page<InitiativeResponseDto> listInitiatives(InitiativeFilterDto filter, Pageable pageable) {
        return initiativeRepository
                .findAll(InitiativeSpecification.withFilter(filter), pageable)
                .map(initiativeMapper::toResponseDto);
    }

    @Override
    public List<InitiativeCostSummary> listCostSummaries(InitiativeFilterDto filter) {
        return initiativeRepository.findCostSummaries(
                filter == null ? null : filter.year(),
                filter == null ? null : filter.quarter(),
                filter == null ? null : filter.status()
        );
    }

    @Override
    public List<InitiativeResponseDto> findRecent() {
        return initiativeRepository.findTop10ByOrderByStartDateDesc()
                .stream()
                .map(initiativeMapper::toResponseDto)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Initiative findOrThrow(Long id) {
        return initiativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", id));
    }

    private static BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
