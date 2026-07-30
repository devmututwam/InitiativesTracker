package zm.co.zanaco.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zm.co.zanaco.tracker.domain.StatusHistory;
import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;

import java.util.List;
import java.util.Optional;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    List<StatusHistory> findAllByInitiativeIdOrderByChangedAtDesc(Long initiativeId);

    Optional<StatusHistory> findFirstByInitiativeIdOrderByChangedAtDesc(Long initiativeId);

    List<StatusHistory> findAllByInitiativeIdAndNewStatus(Long initiativeId, InitiativeStatus newStatus);
}
