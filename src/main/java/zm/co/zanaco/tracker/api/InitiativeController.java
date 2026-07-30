package zm.co.zanaco.tracker.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;
import zm.co.zanaco.tracker.domain.enums.Priority;
import zm.co.zanaco.tracker.dto.CalculateSavingRequest;
import zm.co.zanaco.tracker.dto.InitiativeCostSummary;
import zm.co.zanaco.tracker.dto.InitiativeCreateDto;
import zm.co.zanaco.tracker.dto.InitiativeFilterDto;
import zm.co.zanaco.tracker.dto.InitiativeResponseDto;
import zm.co.zanaco.tracker.dto.InitiativeUpdateDto;
import zm.co.zanaco.tracker.dto.SavingsRecordResponseDto;
import zm.co.zanaco.tracker.dto.StatusChangeDto;
import zm.co.zanaco.tracker.service.InitiativeService;

import java.util.List;

@Tag(name = "Initiatives", description = "Create and manage strategic initiatives")
@RestController
@RequestMapping("/api/initiatives")
@RequiredArgsConstructor
@Validated
public class InitiativeController {

    private final InitiativeService initiativeService;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Operation(summary = "Create a new initiative")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Initiative created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Duplicate project code")
    })
    @PostMapping
    public ResponseEntity<InitiativeResponseDto> create(@Valid @RequestBody InitiativeCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(initiativeService.createInitiative(dto));
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Operation(summary = "List initiatives with optional filters and pagination")
    @ApiResponse(responseCode = "200", description = "Paginated list returned")
    @GetMapping
    public ResponseEntity<Page<InitiativeResponseDto>> list(
            @Parameter(description = "Filter by calendar year (e.g. 2025)")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Filter by quarter (1–4)")
            @RequestParam(required = false) Integer quarter,
            @Parameter(description = "Filter by initiative status")
            @RequestParam(required = false) InitiativeStatus status,
            @Parameter(description = "Filter by priority")
            @RequestParam(required = false) Priority priority,
            @Parameter(description = "Filter by source department (partial match)")
            @RequestParam(required = false) String sourceDepartment,
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        InitiativeFilterDto filter = new InitiativeFilterDto(year, quarter, status, priority, sourceDepartment);
        return ResponseEntity.ok(initiativeService.listInitiatives(filter, pageable));
    }

    @Operation(summary = "Get a single initiative by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Initiative found"),
            @ApiResponse(responseCode = "404", description = "Initiative not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<InitiativeResponseDto> getById(
            @Parameter(description = "Initiative ID") @PathVariable Long id) {
        return ResponseEntity.ok(initiativeService.getInitiative(id));
    }

    @Operation(summary = "List recently started initiatives (top 10)")
    @GetMapping("/recent")
    public ResponseEntity<List<InitiativeResponseDto>> recent() {
        return ResponseEntity.ok(initiativeService.findRecent());
    }

    @Operation(summary = "Aggregated cost + budget summaries with optional filters")
    @GetMapping("/cost-summaries")
    public ResponseEntity<List<InitiativeCostSummary>> costSummaries(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) InitiativeStatus status
    ) {
        InitiativeFilterDto filter = new InitiativeFilterDto(year, quarter, status, null, null);
        return ResponseEntity.ok(initiativeService.listCostSummaries(filter));
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Operation(summary = "Partially update an initiative (only non-null fields applied)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Initiative updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Initiative not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate project code")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<InitiativeResponseDto> update(
            @Parameter(description = "Initiative ID") @PathVariable Long id,
            @Valid @RequestBody InitiativeUpdateDto dto) {
        return ResponseEntity.ok(initiativeService.updateInitiative(id, dto));
    }

    // -------------------------------------------------------------------------
    // Status change
    // -------------------------------------------------------------------------

    @Operation(summary = "Change the status of an initiative and record history")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Status changed"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Initiative not found")
    })
    @PostMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @Parameter(description = "Initiative ID") @PathVariable Long id,
            @Valid @RequestBody StatusChangeDto dto) {
        initiativeService.changeStatus(id, dto);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Savings calculation
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Calculate savings for an initiative",
            description = "Finds the 'Vendor' budget record, subtracts internal costs "
                    + "(INTERNAL_HOURS + INFRA + LICENSE) and incremental expenses, "
                    + "persists the result as a SavingsRecord, and returns it. "
                    + "savingAmount may be negative when costs exceed the vendor quote.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Savings calculated and persisted"),
            @ApiResponse(responseCode = "404", description = "Initiative not found"),
            @ApiResponse(responseCode = "409", description = "No vendor budget attached to this initiative")
    })
    @PostMapping("/{id}/calculate-saving")
    public ResponseEntity<SavingsRecordResponseDto> calculateSaving(
            @Parameter(description = "Initiative ID") @PathVariable Long id,
            @Valid @RequestBody(required = false) CalculateSavingRequest request) {
        return ResponseEntity.ok(
                initiativeService.calculateSavings(id, request != null ? request : CalculateSavingRequest.empty()));
    }
}
