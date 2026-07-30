package zm.co.zanaco.tracker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "savings_records")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "initiative")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SavingsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiative_id", nullable = false)
    private Initiative initiative;

    /** Total approved vendor budget used as the savings baseline. Null when no vendor budget exists. */
    @Column(name = "vendor_budget", precision = 17, scale = 2)
    private BigDecimal vendorBudget;

    /** Sum of INTERNAL_HOURS + INFRA + LICENSE cost entries. */
    @NotNull
    @Column(name = "internal_cost", nullable = false, precision = 17, scale = 2)
    private BigDecimal internalCost;

    /** Sum of all other (non-internal) cost entries. */
    @NotNull
    @Column(name = "incremental_expenses", nullable = false, precision = 17, scale = 2)
    private BigDecimal incrementalExpenses;

    /** vendorBudget - internalCost - incrementalExpenses; may be negative. */
    @NotNull
    @Column(name = "saving_amount", nullable = false, precision = 17, scale = 2)
    private BigDecimal savingAmount;

    @Size(max = 100)
    @Column(name = "calculated_by", length = 100)
    private String calculatedBy;

    @NotNull
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
