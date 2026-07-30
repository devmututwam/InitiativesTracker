package zm.co.zanaco.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zm.co.zanaco.tracker.domain.Budget;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findAllByInitiativeId(Long initiativeId);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Budget b WHERE b.initiative.id = :initiativeId")
    BigDecimal sumAmountByInitiativeId(@Param("initiativeId") Long initiativeId);

    Optional<Budget> findFirstByInitiativeIdOrderByApprovedDateDesc(Long initiativeId);

    /** Finds the most recent budget record whose budgetSource matches the given value (case-insensitive). */
    Optional<Budget> findFirstByInitiativeIdAndBudgetSourceIgnoreCase(Long initiativeId, String budgetSource);

    @Query("""
            SELECT COALESCE(SUM(b.amount), 0)
            FROM Budget b
            WHERE (:year IS NULL OR b.initiative.year = :year)
              AND (:quarter IS NULL OR b.initiative.quarter = :quarter)
            """)
    BigDecimal sumByYearAndQuarter(@Param("year") Integer year, @Param("quarter") Integer quarter);
}
