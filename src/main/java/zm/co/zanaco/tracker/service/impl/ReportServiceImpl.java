package zm.co.zanaco.tracker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.co.zanaco.tracker.dto.BudgetVarianceDto;
import zm.co.zanaco.tracker.dto.SummaryReportDto;
import zm.co.zanaco.tracker.repository.ReportRepository;
import zm.co.zanaco.tracker.repository.SummaryReportProjection;
import zm.co.zanaco.tracker.service.ReportService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    @Override
    public SummaryReportDto getSummary(Integer year, Integer quarter) {
        /*
         * Single native SQL round-trip — all seven aggregates computed together.
         * The projection is always present (scalar sub-queries guarantee one row)
         * but we guard with orElseGet for safety.
         */
        SummaryReportProjection p = reportRepository.fetchSummary(year, quarter)
                .orElseGet(EmptySummary::new);

        BigDecimal totalBudget     = nullSafe(p.getTotalBudget());
        BigDecimal totalActualCost = nullSafe(p.getTotalActualCost());

        return new SummaryReportDto(
                year,
                quarter,
                nullSafeLong(p.getTotalInitiatives()),
                nullSafeLong(p.getWipCount()),
                nullSafeLong(p.getUatCount()),
                nullSafeLong(p.getCompletedCount()),
                totalBudget,
                totalActualCost,
                nullSafe(p.getTotalSavings()),
                totalBudget.subtract(totalActualCost)
        );
    }

    @Override
    public List<BudgetVarianceDto> getBudgetVariance(Integer year, Integer quarter) {
        // JPQL constructor-expression query with correlated sub-queries
        return reportRepository.fetchBudgetVariance(year, quarter);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static BigDecimal nullSafe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static long nullSafeLong(Long v) {
        return v != null ? v : 0L;
    }

    /** Fallback for the (unlikely) case that the native query returns empty. */
    private static final class EmptySummary implements SummaryReportProjection {
        @Override public Long   getTotalInitiatives() { return 0L; }
        @Override public Long   getWipCount()         { return 0L; }
        @Override public Long   getUatCount()         { return 0L; }
        @Override public Long   getCompletedCount()   { return 0L; }
        @Override public BigDecimal getTotalBudget()      { return BigDecimal.ZERO; }
        @Override public BigDecimal getTotalActualCost()  { return BigDecimal.ZERO; }
        @Override public BigDecimal getTotalSavings()     { return BigDecimal.ZERO; }
    }
}
