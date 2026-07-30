package zm.co.zanaco.tracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class InitiativeUnitId implements Serializable {

    @Column(name = "initiative_id", nullable = false)
    private Long initiativeId;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;
}
