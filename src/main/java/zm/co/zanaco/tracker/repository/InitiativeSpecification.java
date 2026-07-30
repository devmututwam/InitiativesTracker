package zm.co.zanaco.tracker.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import zm.co.zanaco.tracker.domain.Initiative;
import zm.co.zanaco.tracker.dto.InitiativeFilterDto;

import java.util.ArrayList;
import java.util.List;

public final class InitiativeSpecification {

    private InitiativeSpecification() {}

    public static Specification<Initiative> withFilter(InitiativeFilterDto filter) {
        return (root, query, cb) -> {
            if (filter == null) return cb.conjunction();

            List<Predicate> predicates = new ArrayList<>();

            if (filter.year() != null) {
                predicates.add(cb.equal(root.get("year"), filter.year()));
            }
            if (filter.quarter() != null) {
                predicates.add(cb.equal(root.get("quarter"), filter.quarter()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.priority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.priority()));
            }
            if (filter.sourceDepartment() != null && !filter.sourceDepartment().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("sourceDepartment")),
                        "%" + filter.sourceDepartment().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
