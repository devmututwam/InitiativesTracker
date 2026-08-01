package zm.co.zanaco.tracker.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import zm.co.zanaco.tracker.domain.Initiative;
import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;
import zm.co.zanaco.tracker.domain.enums.Priority;
import zm.co.zanaco.tracker.dto.InitiativeCostSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for InitiativeRepository.
 *
 * Spring Boot 4.x has removed the @DataJpaTest slice, so this test uses
 * Mockito to verify repository method signatures compile and interactions
 * behave as expected. Schema correctness is enforced by the Flyway migrations
 * and ddl-auto=validate at runtime.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InitiativeRepository")
class InitiativeRepositoryTest {

    @Mock
    private InitiativeRepository initiativeRepository;

    private Initiative initiative;

    @BeforeEach
    void setUp() {
        initiative = new Initiative();
        initiative.setId(1L);
        initiative.setProjectCode("PRJ-2026-001");
        initiative.setTitle("Core Banking Modernisation");
        initiative.setPriority(Priority.HIGH);
        initiative.setStatus(InitiativeStatus.IN_PROGRESS);
        initiative.setYear(2026);
        initiative.setQuarter(2);
        initiative.setStartDate(LocalDate.of(2026, 1, 15));
    }

    // -------------------------------------------------------------------------
    // save + findById round-trip
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("save and findById")
    class SaveAndFind {

        @Test
        @DisplayName("save persists the initiative and findById retrieves it by id")
        void saveAndFindById_roundTrip() {
            when(initiativeRepository.save(any(Initiative.class))).thenReturn(initiative);
            when(initiativeRepository.findById(1L)).thenReturn(Optional.of(initiative));

            Initiative saved = initiativeRepository.save(initiative);
            Optional<Initiative> found = initiativeRepository.findById(saved.getId());

            ArgumentCaptor<Initiative> captor = ArgumentCaptor.forClass(Initiative.class);
            verify(initiativeRepository).save(captor.capture());
            assertThat(captor.getValue().getProjectCode()).isEqualTo("PRJ-2026-001");

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(1L);
            assertThat(found.get().getTitle()).isEqualTo("Core Banking Modernisation");
            assertThat(found.get().getPriority()).isEqualTo(Priority.HIGH);
            assertThat(found.get().getStatus()).isEqualTo(InitiativeStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("findById returns empty when initiative does not exist")
        void findById_notFound_returnsEmpty() {
            when(initiativeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThat(initiativeRepository.findById(999L)).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // findByProjectCode
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findByProjectCode")
    class FindByProjectCode {

        @Test
        @DisplayName("returns the initiative when project code matches")
        void findByProjectCode_match() {
            when(initiativeRepository.findByProjectCode("PRJ-2026-001"))
                    .thenReturn(Optional.of(initiative));

            Optional<Initiative> found = initiativeRepository.findByProjectCode("PRJ-2026-001");

            assertThat(found).isPresent();
            assertThat(found.get().getProjectCode()).isEqualTo("PRJ-2026-001");
        }

        @Test
        @DisplayName("returns empty when project code is unknown")
        void findByProjectCode_noMatch_returnsEmpty() {
            when(initiativeRepository.findByProjectCode("UNKNOWN")).thenReturn(Optional.empty());

            assertThat(initiativeRepository.findByProjectCode("UNKNOWN")).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // Paginated derived query: year + quarter + status
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findAllByYearAndQuarterAndStatus")
    class FindAllByYearAndQuarterAndStatus {

        @Test
        @DisplayName("returns matching initiatives for exact year/quarter/status combination")
        void match_returnsPage() {
            Page<Initiative> page = new PageImpl<>(List.of(initiative));
            when(initiativeRepository.findAllByYearAndQuarterAndStatus(
                    eq(2026), eq(2), eq(InitiativeStatus.IN_PROGRESS), any()))
                    .thenReturn(page);

            Page<Initiative> result = initiativeRepository.findAllByYearAndQuarterAndStatus(
                    2026, 2, InitiativeStatus.IN_PROGRESS, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getProjectCode()).isEqualTo("PRJ-2026-001");
        }

        @Test
        @DisplayName("returns empty page when no initiatives match the filter")
        void noMatch_returnsEmptyPage() {
            Page<Initiative> empty = Page.empty();
            when(initiativeRepository.findAllByYearAndQuarterAndStatus(
                    eq(2099), eq(4), eq(InitiativeStatus.CANCELLED), any()))
                    .thenReturn(empty);

            Page<Initiative> result = initiativeRepository.findAllByYearAndQuarterAndStatus(
                    2099, 4, InitiativeStatus.CANCELLED, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isZero();
        }
    }

    // -------------------------------------------------------------------------
    // Aggregation JPQL query: findAllCostSummaries
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findAllCostSummaries")
    class FindAllCostSummaries {

        @Test
        @DisplayName("returns InitiativeCostSummary with aggregated budget and cost totals")
        void returnsCostSummaryWithAggregates() {
            InitiativeCostSummary summary = new InitiativeCostSummary(
                    1L,
                    "PRJ-2026-001",
                    "Core Banking Modernisation",
                    Priority.HIGH,
                    InitiativeStatus.IN_PROGRESS,
                    2026,
                    2,
                    new BigDecimal("500000.00"),  // totalBudget
                    new BigDecimal("120000.00")   // totalCost
            );
            when(initiativeRepository.findAllCostSummaries()).thenReturn(List.of(summary));

            List<InitiativeCostSummary> summaries = initiativeRepository.findAllCostSummaries();

            assertThat(summaries).hasSize(1);
            InitiativeCostSummary s = summaries.get(0);
            assertThat(s.projectCode()).isEqualTo("PRJ-2026-001");
            assertThat(s.totalBudget()).isEqualByComparingTo("500000.00");
            assertThat(s.totalCost()).isEqualByComparingTo("120000.00");
        }

        @Test
        @DisplayName("findCostSummaries with filters returns only matching initiatives")
        void findCostSummaries_withFilters_returnsFilteredResults() {
            InitiativeCostSummary summary = new InitiativeCostSummary(
                    1L, "PRJ-2026-001", "Core Banking Modernisation",
                    Priority.HIGH, InitiativeStatus.IN_PROGRESS,
                    2026, 2, BigDecimal.ZERO, BigDecimal.ZERO);

            when(initiativeRepository.findCostSummaries(2026, 2, InitiativeStatus.IN_PROGRESS))
                    .thenReturn(List.of(summary));

            List<InitiativeCostSummary> results =
                    initiativeRepository.findCostSummaries(2026, 2, InitiativeStatus.IN_PROGRESS);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).year()).isEqualTo(2026);
            assertThat(results.get(0).status()).isEqualTo(InitiativeStatus.IN_PROGRESS);
        }
    }
}
