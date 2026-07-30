package zm.co.zanaco.tracker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;
import zm.co.zanaco.tracker.domain.enums.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "initiatives",
    indexes = {
        @Index(name = "idx_initiative_project_code", columnList = "project_code"),
        @Index(name = "idx_initiative_start_date", columnList = "start_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"budgets", "costEntries", "statusHistories", "initiativeUnits"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Initiative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "project_code", nullable = false, unique = true, length = 50)
    private String projectCode;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 100)
    @Column(name = "source_department", length = 100)
    private String sourceDepartment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InitiativeStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expected_end_date")
    private LocalDate expectedEndDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @NotNull
    @Min(2000)
    @Column(nullable = false)
    private Integer year;

    @Min(1)
    @Max(4)
    @Column
    private Integer quarter;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "initiative", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets = new ArrayList<>();

    @OneToMany(mappedBy = "initiative", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CostEntry> costEntries = new ArrayList<>();

    @OneToMany(mappedBy = "initiative", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatusHistory> statusHistories = new ArrayList<>();

    @OneToMany(mappedBy = "initiative", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InitiativeUnit> initiativeUnits = new ArrayList<>();
}
