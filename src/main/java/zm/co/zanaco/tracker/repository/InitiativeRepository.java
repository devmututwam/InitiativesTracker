package zm.co.zanaco.tracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zm.co.zanaco.tracker.domain.Initiative;
import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;
import zm.co.zanaco.tracker.dto.InitiativeCostSummary;

import java.util.List;
import java.util.Optional;

public interface InitiativeRepository extends JpaRepository<Initiative, Long>, JpaSpecificationExecutor<Initiative> {

    // Paginated filter by year, quarter, and status – all three must match
    Page<Initiative> findAllByYearAndQuarterAndStatus(
            Integer year,
            Integer quarter,
            InitiativeStatus status,
            Pageable pageable
    );

    // Paginated filter by year and status when quarter is not relevant
    Page<Initiative> findAllByYearAndStatus(
            Integer year,
            InitiativeStatus status,
            Pageable pageable
    );

    // Most recently started initiatives (dashboard widget)
    List<Initiative> findTop10ByOrderByStartDateDesc();

    Optional<Initiative> findByProjectCode(String projectCode);

    boolean existsByProjectCode(String projectCode);

    /**
     * Returns one row per initiative with correlated subqueries for total approved
     * budget and total recorded cost.  Using correlated subqueries avoids the
     * Cartesian product that a direct LEFT JOIN on both collections would produce.
     */
    @Query("""
            SELECT new zm.co.zanaco.tracker.dto.InitiativeCostSummary(
                i.id,
                i.projectCode,
                i.title,
                i.priority,
                i.status,
                i.year,
                i.quarter,
                (SELECT COALESCE(SUM(b.amount), 0) FROM Budget b WHERE b.initiative = i),
                (SELECT COALESCE(SUM(ce.amount), 0) FROM CostEntry ce WHERE ce.initiative = i)
            )
            FROM Initiative i
            WHERE (:year IS NULL OR i.year = :year)
              AND (:quarter IS NULL OR i.quarter = :quarter)
              AND (:status IS NULL OR i.status = :status)
            ORDER BY i.startDate DESC
            """)
    List<InitiativeCostSummary> findCostSummaries(
            @Param("year") Integer year,
            @Param("quarter") Integer quarter,
            @Param("status") InitiativeStatus status
    );

    /**
     * Convenience overload that returns cost summaries for every initiative.
     */
    @Query("""
            SELECT new zm.co.zanaco.tracker.dto.InitiativeCostSummary(
                i.id,
                i.projectCode,
                i.title,
                i.priority,
                i.status,
                i.year,
                i.quarter,
                (SELECT COALESCE(SUM(b.amount), 0) FROM Budget b WHERE b.initiative = i),
                (SELECT COALESCE(SUM(ce.amount), 0) FROM CostEntry ce WHERE ce.initiative = i)
            )
            FROM Initiative i
            ORDER BY i.startDate DESC
            """)
    List<InitiativeCostSummary> findAllCostSummaries();
}
