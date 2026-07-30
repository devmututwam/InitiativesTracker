package zm.co.zanaco.tracker.service;

import zm.co.zanaco.tracker.domain.enums.CostType;
import zm.co.zanaco.tracker.dto.CostEntryDto;
import zm.co.zanaco.tracker.dto.CostEntryResponseDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CostService {

    CostEntryResponseDto addCostEntry(Long initiativeId, CostEntryDto dto);

    CostEntryResponseDto updateCostEntry(Long costEntryId, CostEntryDto dto);

    CostEntryResponseDto getCostEntry(Long costEntryId);

    List<CostEntryResponseDto> getCostEntriesForInitiative(Long initiativeId);

    /** Sum of all cost entries for the given initiative. */
    BigDecimal getTotalCost(Long initiativeId);

    /** Total cost grouped by CostType. */
    Map<CostType, BigDecimal> getCostBreakdown(Long initiativeId);

    void deleteCostEntry(Long costEntryId);
}
