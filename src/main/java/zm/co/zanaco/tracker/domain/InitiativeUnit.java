package zm.co.zanaco.tracker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import zm.co.zanaco.tracker.domain.enums.UnitRole;

import java.math.BigDecimal;

@Entity
@Table(name = "initiative_units")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"initiative", "unit"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class InitiativeUnit {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private InitiativeUnitId id = new InitiativeUnitId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("initiativeId")
    @JoinColumn(name = "initiative_id", nullable = false)
    private Initiative initiative;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("unitId")
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnitRole role;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    @Digits(integer = 3, fraction = 2)
    @Column(name = "contribution_percent", precision = 5, scale = 2)
    private BigDecimal contributionPercent;
}
