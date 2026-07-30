package zm.co.zanaco.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zm.co.zanaco.tracker.domain.SavingsRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SavingsRecordRepository extends JpaRepository<SavingsRecord, Long> {

    List<SavingsRecord> findAllByInitiativeIdOrderByCalculatedAtDesc(Long initiativeId);

    Optional<SavingsRecord> findFirstByInitiativeIdOrderByCalculatedAtDesc(Long initiativeId);

    @Query("""
            SELECT COALESCE(SUM(sr.savingAmount), 0)
            FROM SavingsRecord sr
            WHERE (:year IS NULL OR sr.initiative.year = :year)
              AND (:quarter IS NULL OR sr.initiative.quarter = :quarter)
            """)
    BigDecimal sumSavingsByYearAndQuarter(@Param("year") Integer year, @Param("quarter") Integer quarter);
}
