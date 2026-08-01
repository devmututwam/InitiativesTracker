package zm.co.zanaco.tracker.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zm.co.zanaco.tracker.domain.Budget;
import zm.co.zanaco.tracker.domain.Initiative;
import zm.co.zanaco.tracker.domain.SavingsRecord;
import zm.co.zanaco.tracker.domain.StatusHistory;
import zm.co.zanaco.tracker.domain.enums.CostType;
import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;
import zm.co.zanaco.tracker.domain.enums.Priority;
import zm.co.zanaco.tracker.dto.CalculateSavingRequest;
import zm.co.zanaco.tracker.dto.SavingsRecordResponseDto;
import zm.co.zanaco.tracker.dto.StatusChangeDto;
import zm.co.zanaco.tracker.exception.ResourceNotFoundException;
import zm.co.zanaco.tracker.mapper.InitiativeMapper;
import zm.co.zanaco.tracker.mapper.SavingsRecordMapper;
import zm.co.zanaco.tracker.repository.BudgetRepository;
import zm.co.zanaco.tracker.repository.CostEntryRepository;
import zm.co.zanaco.tracker.repository.InitiativeRepository;
import zm.co.zanaco.tracker.repository.SavingsRecordRepository;
import zm.co.zanaco.tracker.repository.StatusHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InitiativeServiceImpl")
class InitiativeServiceImplTest {

    @Mock private InitiativeRepository initiativeRepository;
    @Mock private StatusHistoryRepository statusHistoryRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private CostEntryRepository costEntryRepository;
    @Mock private SavingsRecordRepository savingsRecordRepository;
    @Mock private InitiativeMapper initiativeMapper;

    // Use the real mapper so toResponseDto logic is covered by these tests as well
    private final SavingsRecordMapper savingsRecordMapper = new SavingsRecordMapper();

    private InitiativeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InitiativeServiceImpl(
                initiativeRepository,
                statusHistoryRepository,
                budgetRepository,
                costEntryRepository,
                savingsRecordRepository,
                initiativeMapper,
                savingsRecordMapper
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Initiative stubInitiative(Long id, String projectCode) {
        Initiative i = new Initiative();
        i.setId(id);
        i.setProjectCode(projectCode);
        i.setTitle("Test Initiative");
        i.setPriority(Priority.HIGH);
        i.setStatus(InitiativeStatus.IN_PROGRESS);
        i.setYear(2025);
        when(initiativeRepository.findById(id)).thenReturn(Optional.of(i));
        return i;
    }

    private Budget stubVendorBudget(Initiative initiative, String amount) {
        Budget b = new Budget();
        b.setInitiative(initiative);
        b.setAmount(new BigDecimal(amount));
        b.setCurrency("ZMW");
        b.setBudgetSource("Vendor");
        when(budgetRepository.findFirstByInitiativeIdAndBudgetSourceIgnoreCase(
                initiative.getId(), "Vendor")).thenReturn(Optional.of(b));
        return b;
    }

    @SuppressWarnings("unchecked")
    private void stubCostSums(Long initiativeId, String internal, String incremental) {
        when(costEntryRepository.sumByCostTypesInAndInitiativeId(
                eq(initiativeId), any(Collection.class)))
                .thenReturn(new BigDecimal(internal));
        when(costEntryRepository.sumByCostTypesNotInAndInitiativeId(
                eq(initiativeId), any(Collection.class)))
                .thenReturn(new BigDecimal(incremental));
    }

    /** Makes the repository echo back the record that was passed to save(), with a synthetic ID. */
    private void stubSaveEcho() {
        when(savingsRecordRepository.save(any(SavingsRecord.class))).thenAnswer(inv -> {
            SavingsRecord r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("changeStatus")
    class ChangeStatus {

        @Test
        @DisplayName("persists a StatusHistory with old/new status, changedBy and comment")
        void changesStatus_persistsHistoryRecord() {
            Initiative initiative = stubInitiative(10L, "PRJ-010");
            initiative.setStatus(InitiativeStatus.PLANNED);

            StatusChangeDto dto = new StatusChangeDto(
                    InitiativeStatus.IN_PROGRESS, "analyst@zanaco.zm", "Starting delivery");

            when(statusHistoryRepository.save(any(StatusHistory.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.changeStatus(10L, dto);

            // Verify initiative status is updated in memory
            assertThat(initiative.getStatus()).isEqualTo(InitiativeStatus.IN_PROGRESS);

            // Verify the saved StatusHistory has correct field values
            ArgumentCaptor<StatusHistory> captor = ArgumentCaptor.forClass(StatusHistory.class);
            verify(statusHistoryRepository).save(captor.capture());

            StatusHistory saved = captor.getValue();
            assertThat(saved.getOldStatus()).isEqualTo(InitiativeStatus.PLANNED);
            assertThat(saved.getNewStatus()).isEqualTo(InitiativeStatus.IN_PROGRESS);
            assertThat(saved.getChangedBy()).isEqualTo("analyst@zanaco.zm");
            assertThat(saved.getComment()).isEqualTo("Starting delivery");
            assertThat(saved.getChangedAt()).isNotNull();
            assertThat(saved.getInitiative()).isSameAs(initiative);
        }

        @Test
        @DisplayName("changedAt is set to a time at or after the call instant")
        void changesStatus_changedAtIsRecent() {
            stubInitiative(11L, "PRJ-011");
            when(statusHistoryRepository.save(any(StatusHistory.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            service.changeStatus(11L, new StatusChangeDto(InitiativeStatus.ON_HOLD, null, null));
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            ArgumentCaptor<StatusHistory> captor = ArgumentCaptor.forClass(StatusHistory.class);
            verify(statusHistoryRepository).save(captor.capture());

            LocalDateTime changedAt = captor.getValue().getChangedAt();
            assertThat(changedAt).isAfterOrEqualTo(before).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("null changedBy and comment are propagated to the history record")
        void changesStatus_nullFields_propagatedToHistory() {
            stubInitiative(12L, "PRJ-012");
            when(statusHistoryRepository.save(any(StatusHistory.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.changeStatus(12L, new StatusChangeDto(InitiativeStatus.CANCELLED, null, null));

            ArgumentCaptor<StatusHistory> captor = ArgumentCaptor.forClass(StatusHistory.class);
            verify(statusHistoryRepository).save(captor.capture());

            assertThat(captor.getValue().getChangedBy()).isNull();
            assertThat(captor.getValue().getComment()).isNull();
            assertThat(captor.getValue().getNewStatus()).isEqualTo(InitiativeStatus.CANCELLED);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when initiative does not exist")
        void changeStatus_unknownInitiative_throwsNotFound() {
            when(initiativeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.changeStatus(999L,
                            new StatusChangeDto(InitiativeStatus.COMPLETED, null, null)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Positive saving (vendor budget > total costs)")
    class PositiveSaving {

        @Test
        @DisplayName("returns correct saving amount and persists a SavingsRecord")
        void positiveSaving_calculatesAndPersists() {
            Initiative initiative = stubInitiative(1L, "PRJ-001");
            stubVendorBudget(initiative, "100000.00");
            stubCostSums(1L, "30000.00", "10000.00");
            stubSaveEcho();

            SavingsRecordResponseDto result =
                    service.calculateSavings(1L, new CalculateSavingRequest("analyst", "Q1 review"));

            // Verify saved values
            ArgumentCaptor<SavingsRecord> captor = ArgumentCaptor.forClass(SavingsRecord.class);
            verify(savingsRecordRepository).save(captor.capture());
            SavingsRecord saved = captor.getValue();

            assertThat(saved.getVendorBudget()).isEqualByComparingTo("100000.00");
            assertThat(saved.getInternalCost()).isEqualByComparingTo("30000.00");
            assertThat(saved.getIncrementalExpenses()).isEqualByComparingTo("10000.00");
            assertThat(saved.getSavingAmount()).isEqualByComparingTo("60000.00");
            assertThat(saved.getCalculatedBy()).isEqualTo("analyst");
            assertThat(saved.getNotes()).isEqualTo("Q1 review");

            // Verify response DTO
            assertThat(result.id()).isEqualTo(99L);
            assertThat(result.savingAmount()).isEqualByComparingTo("60000.00");
            assertThat(result.positiveSaving()).isTrue();
            assertThat(result.initiativeId()).isEqualTo(1L);
            assertThat(result.projectCode()).isEqualTo("PRJ-001");
        }

        @Test
        @DisplayName("positiveSaving flag is true when saving equals zero exactly")
        void positiveSaving_zeroSaving_flagIsTrue() {
            Initiative initiative = stubInitiative(2L, "PRJ-002");
            stubVendorBudget(initiative, "50000.00");
            stubCostSums(2L, "30000.00", "20000.00"); // total cost exactly equals budget
            stubSaveEcho();

            SavingsRecordResponseDto result =
                    service.calculateSavings(2L, CalculateSavingRequest.empty());

            assertThat(result.savingAmount()).isEqualByComparingTo("0.00");
            assertThat(result.positiveSaving()).isTrue();
        }
    }

    @Nested
    @DisplayName("Negative saving (costs exceed vendor budget)")
    class NegativeSaving {

        @Test
        @DisplayName("saving amount is negative and positiveSaving flag is false")
        void negativeSaving_flagIsFalse() {
            Initiative initiative = stubInitiative(3L, "PRJ-003");
            stubVendorBudget(initiative, "50000.00");
            stubCostSums(3L, "40000.00", "20000.00"); // total = 60 000, over budget by 10 000
            stubSaveEcho();

            SavingsRecordResponseDto result =
                    service.calculateSavings(3L, new CalculateSavingRequest("manager", null));

            assertThat(result.savingAmount()).isEqualByComparingTo("-10000.00");
            assertThat(result.positiveSaving()).isFalse();

            ArgumentCaptor<SavingsRecord> captor = ArgumentCaptor.forClass(SavingsRecord.class);
            verify(savingsRecordRepository).save(captor.capture());
            assertThat(captor.getValue().getSavingAmount()).isNegative();
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("when no cost entries exist, saving equals the full vendor budget")
        @SuppressWarnings("unchecked")
        void noCostEntries_savingEqualsVendorBudget() {
            Initiative initiative = stubInitiative(4L, "PRJ-004");
            stubVendorBudget(initiative, "75000.00");
            // Repository returns null (no matching rows) – service must treat as ZERO
            when(costEntryRepository.sumByCostTypesInAndInitiativeId(
                    eq(4L), any(Collection.class))).thenReturn(null);
            when(costEntryRepository.sumByCostTypesNotInAndInitiativeId(
                    eq(4L), any(Collection.class))).thenReturn(null);
            stubSaveEcho();

            SavingsRecordResponseDto result =
                    service.calculateSavings(4L, CalculateSavingRequest.empty());

            assertThat(result.savingAmount()).isEqualByComparingTo("75000.00");
            assertThat(result.internalCost()).isEqualByComparingTo("0.00");
            assertThat(result.incrementalExpenses()).isEqualByComparingTo("0.00");
            assertThat(result.positiveSaving()).isTrue();
        }

        @Test
        @DisplayName("when no vendor budget exists, throws IllegalArgumentException")
        void noVendorBudget_throwsIllegalArgument() {
            stubInitiative(5L, "PRJ-005");
            when(budgetRepository.findFirstByInitiativeIdAndBudgetSourceIgnoreCase(
                    anyLong(), eq("Vendor"))).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.calculateSavings(5L, CalculateSavingRequest.empty()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("No vendor budget found")
                    .hasMessageContaining("PRJ-005");
        }

        @Test
        @DisplayName("when initiative does not exist, throws ResourceNotFoundException")
        void unknownInitiative_throwsNotFound() {
            when(initiativeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.calculateSavings(999L, CalculateSavingRequest.empty()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("null request body is handled gracefully (calculatedBy and notes are null)")
        @SuppressWarnings("unchecked")
        void nullRequest_isHandledGracefully() {
            Initiative initiative = stubInitiative(6L, "PRJ-006");
            stubVendorBudget(initiative, "20000.00");
            stubCostSums(6L, "5000.00", "3000.00");
            stubSaveEcho();

            SavingsRecordResponseDto result = service.calculateSavings(6L, null);

            assertThat(result.savingAmount()).isEqualByComparingTo("12000.00");
            assertThat(result.calculatedBy()).isNull();
            assertThat(result.notes()).isNull();
        }

        @Test
        @DisplayName("internal cost types constant contains exactly INTERNAL_HOURS, INFRA, LICENSE")
        void internalCostTypesConstant_hasExpectedValues() {
            assertThat(InitiativeServiceImpl.INTERNAL_COST_TYPES)
                    .containsExactlyInAnyOrder(
                            CostType.INTERNAL_HOURS,
                            CostType.INFRA,
                            CostType.LICENSE)
                    .doesNotContain(
                            CostType.LABOUR,
                            CostType.MATERIALS,
                            CostType.EQUIPMENT,
                            CostType.SERVICES,
                            CostType.OVERHEAD,
                            CostType.OTHER);
        }
    }
}
