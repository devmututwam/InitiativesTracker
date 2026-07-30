package zm.co.zanaco.tracker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "units")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "initiativeUnits")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InitiativeUnit> initiativeUnits = new ArrayList<>();
}
