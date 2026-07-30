package zm.co.zanaco.tracker.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import zm.co.zanaco.tracker.domain.enums.CostType;
import zm.co.zanaco.tracker.dto.CostEntryDto;
import zm.co.zanaco.tracker.dto.CostEntryResponseDto;
import zm.co.zanaco.tracker.service.CostService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "Costs", description = "Record and track cost entries for an initiative")
@RestController
@RequiredArgsConstructor
@Validated
public class CostController {

    private final CostService costService;

    // -------------------------------------------------------------------------
    // Nested under /api/initiatives/{id}/costs
    // -------------------------------------------------------------------------

    @Operation(summary = "Add a new cost entry to an initiative")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cost entry created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Initiative not found")
    })
    @PostMapping("/api/initiatives/{initiativeId}/costs")
    public ResponseEntity<CostEntryResponseDto> add(
            @Parameter(description = "Initiative ID") @PathVariable Long initiativeId,
            @Valid @RequestBody CostEntryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(costService.addCostEntry(initiativeId, dto));
    }

    @Operation(summary = "List all cost entries for an initiative")
    @ApiResponse(responseCode = "200", description = "List returned")
    @GetMapping("/api/initiatives/{initiativeId}/costs")
    public ResponseEntity<List<CostEntryResponseDto>> list(
            @Parameter(description = "Initiative ID") @PathVariable Long initiativeId) {
        return ResponseEntity.ok(costService.getCostEntriesForInitiative(initiativeId));
    }

    @Operation(summary = "Get total cost for an initiative")
    @GetMapping("/api/initiatives/{initiativeId}/costs/total")
    public ResponseEntity<BigDecimal> total(
            @Parameter(description = "Initiative ID") @PathVariable Long initiativeId) {
        return ResponseEntity.ok(costService.getTotalCost(initiativeId));
    }

    @Operation(summary = "Get cost breakdown grouped by cost type for an initiative")
    @GetMapping("/api/initiatives/{initiativeId}/costs/breakdown")
    public ResponseEntity<Map<CostType, BigDecimal>> breakdown(
            @Parameter(description = "Initiative ID") @PathVariable Long initiativeId) {
        return ResponseEntity.ok(costService.getCostBreakdown(initiativeId));
    }

    // -------------------------------------------------------------------------
    // Direct /api/costs/{costEntryId}
    // -------------------------------------------------------------------------

    @Operation(summary = "Get a single cost entry by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cost entry found"),
            @ApiResponse(responseCode = "404", description = "Cost entry not found")
    })
    @GetMapping("/api/costs/{costEntryId}")
    public ResponseEntity<CostEntryResponseDto> get(
            @Parameter(description = "Cost entry ID") @PathVariable Long costEntryId) {
        return ResponseEntity.ok(costService.getCostEntry(costEntryId));
    }

    @Operation(summary = "Update a cost entry (only non-null fields applied)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cost entry updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Cost entry not found")
    })
    @PatchMapping("/api/costs/{costEntryId}")
    public ResponseEntity<CostEntryResponseDto> update(
            @Parameter(description = "Cost entry ID") @PathVariable Long costEntryId,
            @Valid @RequestBody CostEntryDto dto) {
        return ResponseEntity.ok(costService.updateCostEntry(costEntryId, dto));
    }

    @Operation(summary = "Delete a cost entry")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cost entry deleted"),
            @ApiResponse(responseCode = "404", description = "Cost entry not found")
    })
    @DeleteMapping("/api/costs/{costEntryId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Cost entry ID") @PathVariable Long costEntryId) {
        costService.deleteCostEntry(costEntryId);
        return ResponseEntity.noContent().build();
    }
}
