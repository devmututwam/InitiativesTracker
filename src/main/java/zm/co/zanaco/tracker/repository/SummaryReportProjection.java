package zm.co.zanaco.tracker.repository;

import java.math.BigDecimal;

/**
 * Spring Data native-query projection for the single-row period summary.
 * Column aliases in the SQL must match these getter names exactly
 * (Spring Data strips "get" and lower-cases the first letter, then maps
 * snake_case aliases via underscore-to-camel conversion).
 */
public interface SummaryReportProjection {
    Long   getTotalInitiatives();
    Long   getWipCount();
    Long   getUatCount();
    Long   getCompletedCount();
    BigDecimal getTotalBudget();
    BigDecimal getTotalActualCost();
    BigDecimal getTotalSavings();
}
