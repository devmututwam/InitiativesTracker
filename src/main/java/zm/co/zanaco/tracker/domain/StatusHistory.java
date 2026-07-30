package zm.co.zanaco.tracker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "status_histories")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "initiative")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiative_id", nullable = false)
    private Initiative initiative;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 30)
    private InitiativeStatus oldStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private InitiativeStatus newStatus;

    @Size(max = 100)
    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @NotNull
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    private String comment;
}
