package zm.co.zanaco.tracker.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zm.co.zanaco.tracker.dto.BudgetVarianceDto;
import zm.co.zanaco.tracker.dto.SummaryReportDto;
import zm.co.zanaco.tracker.service.ReportService;

import java.util.List;

@Tag(name = "Reports", description = "Aggregated period reports (read-only, no DB writes)")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;

    // -------------------------------------------------------------------------
    // Summary
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Period summary",
            description = "Returns totals for the given year/quarter in a single native SQL "
                    + "round-trip: initiative counts by status, total approved budget, total "
                    + "actual cost, total recorded savings, and net budget variance.")
    @ApiResponse(responseCode = "200", description = "Summary returned")
    @GetMapping("/summary")
    public ResponseEntity<SummaryReportDto> summary(
            @Parameter(description = "Calendar year (e.g. 2025). Omit to aggregate all years.")
            @RequestParam(required = false) @Min(2000) Integer year,
            @Parameter(description = "Quarter 1–4. Omit to aggregate all quarters.")
            @RequestParam(required = false) @Min(1) @Max(4) Integer quarter
    ) {
        return ResponseEntity.ok(reportService.getSummary(year, quarter));
    }

    // -------------------------------------------------------------------------
    // Budget variance
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Budget variance per initiative",
            description = "Returns one row per initiative with totalBudget (sum of approved "
                    + "budget records), totalActualCost (sum of all cost entries), variance "
                    + "(budget − cost), and an overBudget flag. Rows are sorted by variance "
                    + "ascending so the most over-budget initiatives appear first.")
    @ApiResponse(responseCode = "200", description = "Variance list returned")
    @GetMapping("/budget-variance")
    public ResponseEntity<List<BudgetVarianceDto>> budgetVariance(
            @Parameter(description = "Calendar year. Omit to include all years.")
            @RequestParam(required = false) @Min(2000) Integer year,
            @Parameter(description = "Quarter 1–4. Omit to include all quarters.")
            @RequestParam(required = false) @Min(1) @Max(4) Integer quarter
    ) {
        return ResponseEntity.ok(reportService.getBudgetVariance(year, quarter));
    }
}
