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
import zm.co.zanaco.tracker.dto.BudgetDto;
import zm.co.zanaco.tracker.dto.BudgetResponseDto;
import zm.co.zanaco.tracker.service.BudgetService;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Budgets", description = "Attach and manage budgets for an initiative")
@RestController
@RequiredArgsConstructor
@Validated
public class BudgetController {

    private final BudgetService budgetService;

    // -------------------------------------------------------------------------
    // Nested under /api/initiatives/{id}/budgets
    // -------------------------------------------------------------------------

    @Operation(summary = "Attach a new budget record to an initiative")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Budget attached"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Initiative not found")
    })
    @PostMapping("/api/initiatives/{initiativeId}/budgets")
    public ResponseEntity<BudgetResponseDto> attach(
            @Parameter(description = "Initiative ID") @PathVariable Long initiativeId,
            @Valid @RequestBody BudgetDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.attachBudget(initiativeId, dto));
    }

    @Operation(summary = "List all budget records for an initiative")
    @ApiResponse(responseCode = "200", description = "List returned")
    @GetMapping("/api/initiatives/{initiativeId}/budgets")
    public ResponseEntity<List<BudgetResponseDto>> list(
            @Parameter(description = "Initiative ID") @PathVariable Long initiativeId) {
        return ResponseEntity.ok(budgetService.getBudgetsForInitiative(initiativeId));
    }

    @Operation(summary = "Get total approved budget for an initiative")
    @GetMapping("/api/initiatives/{initiativeId}/budgets/total")
    public ResponseEntity<BigDecimal> total(
            @Parameter(description = "Initiative ID") @PathVariable Long initiativeId) {
        return ResponseEntity.ok(budgetService.getTotalBudget(initiativeId));
    }

    // -------------------------------------------------------------------------
    // Direct /api/budgets/{budgetId}
    // -------------------------------------------------------------------------

    @Operation(summary = "Get a single budget record by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Budget found"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @GetMapping("/api/budgets/{budgetId}")
    public ResponseEntity<BudgetResponseDto> get(
            @Parameter(description = "Budget ID") @PathVariable Long budgetId) {
        return ResponseEntity.ok(budgetService.getBudget(budgetId));
    }

    @Operation(summary = "Update a budget record (only non-null fields applied)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Budget updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @PatchMapping("/api/budgets/{budgetId}")
    public ResponseEntity<BudgetResponseDto> update(
            @Parameter(description = "Budget ID") @PathVariable Long budgetId,
            @Valid @RequestBody BudgetDto dto) {
        return ResponseEntity.ok(budgetService.updateBudget(budgetId, dto));
    }

    @Operation(summary = "Delete a budget record")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Budget deleted"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @DeleteMapping("/api/budgets/{budgetId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Budget ID") @PathVariable Long budgetId) {
        budgetService.deleteBudget(budgetId);
        return ResponseEntity.noContent().build();
    }
}
