package zm.co.zanaco.tracker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.co.zanaco.tracker.domain.CostEntry;
import zm.co.zanaco.tracker.domain.Initiative;
import zm.co.zanaco.tracker.domain.enums.CostType;
import zm.co.zanaco.tracker.dto.CostEntryDto;
import zm.co.zanaco.tracker.dto.CostEntryResponseDto;
import zm.co.zanaco.tracker.exception.ResourceNotFoundException;
import zm.co.zanaco.tracker.mapper.CostEntryMapper;
import zm.co.zanaco.tracker.repository.CostEntryRepository;
import zm.co.zanaco.tracker.repository.InitiativeRepository;
import zm.co.zanaco.tracker.service.CostService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostServiceImpl implements CostService {

    private final CostEntryRepository costEntryRepository;
    private final InitiativeRepository initiativeRepository;
    private final CostEntryMapper costEntryMapper;

    @Override
    @Transactional
    public CostEntryResponseDto addCostEntry(Long initiativeId, CostEntryDto dto) {
        Initiative initiative = findInitiativeOrThrow(initiativeId);
        CostEntry entry = costEntryMapper.toEntity(initiative, dto);
        return costEntryMapper.toResponseDto(costEntryRepository.save(entry));
    }

    @Override
    @Transactional
    public CostEntryResponseDto updateCostEntry(Long costEntryId, CostEntryDto dto) {
        CostEntry entry = findCostEntryOrThrow(costEntryId);
        costEntryMapper.updateEntity(entry, dto);
        return costEntryMapper.toResponseDto(costEntryRepository.save(entry));
    }

    @Override
    public CostEntryResponseDto getCostEntry(Long costEntryId) {
        return costEntryMapper.toResponseDto(findCostEntryOrThrow(costEntryId));
    }

    @Override
    public List<CostEntryResponseDto> getCostEntriesForInitiative(Long initiativeId) {
        findInitiativeOrThrow(initiativeId);
        return costEntryRepository.findAllByInitiativeId(initiativeId)
                .stream()
                .map(costEntryMapper::toResponseDto)
                .toList();
    }

    @Override
    public BigDecimal getTotalCost(Long initiativeId) {
        findInitiativeOrThrow(initiativeId);
        BigDecimal total = costEntryRepository.sumAmountByInitiativeId(initiativeId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Map<CostType, BigDecimal> getCostBreakdown(Long initiativeId) {
        findInitiativeOrThrow(initiativeId);

        Map<CostType, BigDecimal> breakdown = new EnumMap<>(CostType.class);

        // Seed every known type with ZERO so callers always get a complete map
        Arrays.stream(CostType.values()).forEach(t -> breakdown.put(t, BigDecimal.ZERO));

        costEntryRepository.sumAmountByCostTypeForInitiative(initiativeId)
                .forEach(row -> {
                    CostType type = (CostType) row[0];
                    Object raw = row[1];
                    BigDecimal amount = raw instanceof BigDecimal bd
                            ? bd
                            : new BigDecimal(raw.toString());
                    breakdown.put(type, amount);
                });

        return breakdown;
    }

    @Override
    @Transactional
    public void deleteCostEntry(Long costEntryId) {
        CostEntry entry = findCostEntryOrThrow(costEntryId);
        costEntryRepository.delete(entry);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Initiative findInitiativeOrThrow(Long id) {
        return initiativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", id));
    }

    private CostEntry findCostEntryOrThrow(Long id) {
        return costEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostEntry", id));
    }
}
