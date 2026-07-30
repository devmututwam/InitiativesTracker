package zm.co.zanaco.tracker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "initiative")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiative_id", nullable = false)
    private Initiative initiative;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 15, fraction = 2)
    @Column(nullable = false, precision = 17, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "approved_date")
    private LocalDate approvedDate;

    @Size(max = 100)
    @Column(name = "budget_source", length = 100)
    private String budgetSource;
}
