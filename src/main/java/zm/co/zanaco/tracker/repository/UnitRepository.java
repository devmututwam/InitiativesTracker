package zm.co.zanaco.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zm.co.zanaco.tracker.domain.Unit;

import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {

    Optional<Unit> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
