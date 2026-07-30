package zm.co.zanaco.tracker.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zm.co.zanaco.tracker.dto.InitiativeFilterDto;
import zm.co.zanaco.tracker.dto.InitiativeCreateDto;
import zm.co.zanaco.tracker.dto.InitiativeCostSummary;
import zm.co.zanaco.tracker.dto.InitiativeResponseDto;
import zm.co.zanaco.tracker.dto.InitiativeUpdateDto;
import zm.co.zanaco.tracker.dto.CalculateSavingRequest;
import zm.co.zanaco.tracker.dto.SavingsRecordResponseDto;
import zm.co.zanaco.tracker.dto.StatusChangeDto;

import java.util.List;

public interface InitiativeService {

    InitiativeResponseDto createInitiative(InitiativeCreateDto dto);

    InitiativeResponseDto getInitiative(Long id);

    InitiativeResponseDto updateInitiative(Long id, InitiativeUpdateDto dto);

    void changeStatus(Long id, StatusChangeDto dto);

    /**
     * Calculates initiative savings against a vendor budget.
     * Persists a {@code SavingsRecord} and returns the saved record.
     * <ul>
     *   <li>internalCost  — sum of INTERNAL_HOURS + INFRA + LICENSE entries</li>
     *   <li>incrementalExpenses — sum of all other cost types</li>
     *   <li>saving = vendorBudget - internalCost - incrementalExpenses</li>
     * </ul>
     * Throws {@link IllegalArgumentException} when no vendor budget exists.
     */
    SavingsRecordResponseDto calculateSavings(Long id, CalculateSavingRequest request);

    Page<InitiativeResponseDto> listInitiatives(InitiativeFilterDto filter, Pageable pageable);

    List<InitiativeCostSummary> listCostSummaries(InitiativeFilterDto filter);

    List<InitiativeResponseDto> findRecent();
}
