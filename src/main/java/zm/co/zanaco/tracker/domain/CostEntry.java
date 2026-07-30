package zm.co.zanaco.tracker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import zm.co.zanaco.tracker.domain.enums.CostType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cost_entries")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "initiative")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CostEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiative_id", nullable = false)
    private Initiative initiative;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "cost_type", nullable = false, length = 20)
    private CostType costType;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 15, fraction = 2)
    @Column(nullable = false, precision = 17, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    @Column(nullable = false, length = 3)
    private String currency;

    @NotNull
    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
