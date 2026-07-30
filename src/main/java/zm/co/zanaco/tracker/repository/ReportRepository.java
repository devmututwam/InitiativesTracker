package zm.co.zanaco.tracker.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import zm.co.zanaco.tracker.domain.Initiative;
import zm.co.zanaco.tracker.dto.BudgetVarianceDto;

import java.util.List;
import java.util.Optional;

/**
 * Read-only reporting repository. Extends the minimal {@link Repository} marker
 * interface so Spring Data manages it, without inheriting CRUD operations.
 *
 * <p>Summary query runs as a single native SQL round-trip using scalar
 * sub-queries (avoids Cartesian products from multi-join aggregation).
 * Variance query reuses JPQL constructor expressions with correlated sub-queries.
 */
public interface ReportRepository extends Repository<Initiative, Long> {

    // -------------------------------------------------------------------------
    // Period summary – single native round-trip
    // -------------------------------------------------------------------------

    /**
     * Returns a one-row projection with all period totals.
     * Both {@code year} and {@code quarter} are optional; pass {@code null} to
     * aggregate across all periods.
     */
    @Query(nativeQuery = true, value = """
            SELECT
                (SELECT COUNT(*)
                 FROM   initiatives
                 WHERE  (:year    IS NULL OR year    = :year)
                   AND  (:quarter IS NULL OR quarter = :quarter))            AS total_initiatives,

                (SELECT COUNT(*)
                 FROM   initiatives
                 WHERE  (:year    IS NULL OR year    = :year)
                   AND  (:quarter IS NULL OR quarter = :quarter)
                   AND  status = 'IN_PROGRESS')                              AS wip_count,

                (SELECT COUNT(*)
                 FROM   initiatives
                 WHERE  (:year    IS NULL OR year    = :year)
                   AND  (:quarter IS NULL OR quarter = :quarter)
                   AND  status = 'UAT')                                      AS uat_count,

                (SELECT COUNT(*)
                 FROM   initiatives
                 WHERE  (:year    IS NULL OR year    = :year)
                   AND  (:quarter IS NULL OR quarter = :quarter)
                   AND  status = 'COMPLETED')                                AS completed_count,

                COALESCE(
                    (SELECT SUM(b.amount)
                     FROM   budgets b
                     JOIN   initiatives i ON b.initiative_id = i.id
                     WHERE  (:year    IS NULL OR i.year    = :year)
                       AND  (:quarter IS NULL OR i.quarter = :quarter)
                    ), 0)                                                    AS total_budget,

                COALESCE(
                    (SELECT SUM(ce.amount)
                     FROM   cost_entries ce
                     JOIN   initiatives i ON ce.initiative_id = i.id
                     WHERE  (:year    IS NULL OR i.year    = :year)
                       AND  (:quarter IS NULL OR i.quarter = :quarter)
                    ), 0)                                                    AS total_actual_cost,

                COALESCE(
                    (SELECT SUM(sr.saving_amount)
                     FROM   savings_records sr
                     JOIN   initiatives i ON sr.initiative_id = i.id
                     WHERE  (:year    IS NULL OR i.year    = :year)
                       AND  (:quarter IS NULL OR i.quarter = :quarter)
                    ), 0)                                                    AS total_savings
            """)
    Optional<SummaryReportProjection> fetchSummary(
            @Param("year") Integer year,
            @Param("quarter") Integer quarter
    );

    // -------------------------------------------------------------------------
    // Budget variance – JPQL constructor expression, correlated sub-queries
    // -------------------------------------------------------------------------

    /**
     * Returns one row per initiative with budget, actual cost, and their
     * difference.  Correlated sub-queries prevent the Cartesian product that
     * would arise from a direct LEFT JOIN on both tables.
     */
    @Query("""
            SELECT new zm.co.zanaco.tracker.dto.BudgetVarianceDto(
                i.id,
                i.projectCode,
                i.title,
                i.status,
                (SELECT COALESCE(SUM(b.amount), 0)  FROM Budget     b  WHERE b.initiative  = i),
                (SELECT COALESCE(SUM(ce.amount), 0) FROM CostEntry   ce WHERE ce.initiative = i),
                (SELECT COALESCE(SUM(b.amount), 0)  FROM Budget     b  WHERE b.initiative  = i)
                    - (SELECT COALESCE(SUM(ce.amount), 0) FROM CostEntry ce WHERE ce.initiative = i),
                CASE WHEN
                    (SELECT COALESCE(SUM(b.amount), 0)  FROM Budget   b  WHERE b.initiative  = i)
                    - (SELECT COALESCE(SUM(ce.amount), 0) FROM CostEntry ce WHERE ce.initiative = i)
                    < 0 THEN true ELSE false END
            )
            FROM Initiative i
            WHERE (:year    IS NULL OR i.year    = :year)
              AND (:quarter IS NULL OR i.quarter = :quarter)
            ORDER BY
                (SELECT COALESCE(SUM(b.amount), 0)  FROM Budget   b  WHERE b.initiative  = i)
                - (SELECT COALESCE(SUM(ce.amount), 0) FROM CostEntry ce WHERE ce.initiative = i) ASC
            """)
    List<BudgetVarianceDto> fetchBudgetVariance(
            @Param("year") Integer year,
            @Param("quarter") Integer quarter
    );
}
