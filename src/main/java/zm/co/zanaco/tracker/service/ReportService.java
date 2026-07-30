package zm.co.zanaco.tracker.service;

import zm.co.zanaco.tracker.dto.BudgetVarianceDto;
import zm.co.zanaco.tracker.dto.SummaryReportDto;

import java.util.List;

public interface ReportService {

    /**
     * Returns aggregated totals for the given period.
     * Pass {@code null} for {@code year} or {@code quarter} to include all periods.
     */
    SummaryReportDto getSummary(Integer year, Integer quarter);

    /**
     * Returns one row per initiative showing approved budget, actual cost,
     * and the variance (budget − cost). Rows are sorted by variance ascending
     * so the most over-budget initiatives appear first.
     */
    List<BudgetVarianceDto> getBudgetVariance(Integer year, Integer quarter);
}
