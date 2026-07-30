package zm.co.zanaco.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zm.co.zanaco.tracker.domain.CostEntry;
import zm.co.zanaco.tracker.domain.enums.CostType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface CostEntryRepository extends JpaRepository<CostEntry, Long> {

    List<CostEntry> findAllByInitiativeId(Long initiativeId);

    List<CostEntry> findAllByInitiativeIdAndCostType(Long initiativeId, CostType costType);

    List<CostEntry> findAllByInitiativeIdAndRecordedDateBetween(
            Long initiativeId,
            LocalDate from,
            LocalDate to
    );

    @Query("SELECT COALESCE(SUM(ce.amount), 0) FROM CostEntry ce WHERE ce.initiative.id = :initiativeId")
    BigDecimal sumAmountByInitiativeId(@Param("initiativeId") Long initiativeId);

    @Query("""
            SELECT ce.costType, COALESCE(SUM(ce.amount), 0)
            FROM CostEntry ce
            WHERE ce.initiative.id = :initiativeId
            GROUP BY ce.costType
            """)
    List<Object[]> sumAmountByCostTypeForInitiative(@Param("initiativeId") Long initiativeId);

    @Query("SELECT COALESCE(SUM(ce.amount), 0) FROM CostEntry ce WHERE ce.initiative.id = :initiativeId AND ce.costType = :costType")
    BigDecimal sumByCostTypeAndInitiativeId(@Param("initiativeId") Long initiativeId, @Param("costType") CostType costType);

    @Query("SELECT COALESCE(SUM(ce.amount), 0) FROM CostEntry ce WHERE ce.initiative.id = :initiativeId AND ce.costType <> :costType")
    BigDecimal sumExcludingCostTypeAndInitiativeId(@Param("initiativeId") Long initiativeId, @Param("costType") CostType costType);

    /** Sum of entries whose costType is in the given collection (used for INTERNAL_HOURS, INFRA, LICENSE). */
    @Query("SELECT COALESCE(SUM(ce.amount), 0) FROM CostEntry ce WHERE ce.initiative.id = :initiativeId AND ce.costType IN :costTypes")
    BigDecimal sumByCostTypesInAndInitiativeId(@Param("initiativeId") Long initiativeId, @Param("costTypes") Collection<CostType> costTypes);

    /** Sum of entries whose costType is NOT in the given collection (incremental / external expenses). */
    @Query("SELECT COALESCE(SUM(ce.amount), 0) FROM CostEntry ce WHERE ce.initiative.id = :initiativeId AND ce.costType NOT IN :costTypes")
    BigDecimal sumByCostTypesNotInAndInitiativeId(@Param("initiativeId") Long initiativeId, @Param("costTypes") Collection<CostType> costTypes);

    @Query("""
            SELECT COALESCE(SUM(ce.amount), 0)
            FROM CostEntry ce
            WHERE (:year IS NULL OR ce.initiative.year = :year)
              AND (:quarter IS NULL OR ce.initiative.quarter = :quarter)
            """)
    BigDecimal sumByYearAndQuarter(@Param("year") Integer year, @Param("quarter") Integer quarter);
}
