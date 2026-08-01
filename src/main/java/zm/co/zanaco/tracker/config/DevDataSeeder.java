package zm.co.zanaco.tracker.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import zm.co.zanaco.tracker.domain.Unit;
import zm.co.zanaco.tracker.domain.enums.CostType;
import zm.co.zanaco.tracker.domain.enums.InitiativeStatus;
import zm.co.zanaco.tracker.domain.enums.Priority;
import zm.co.zanaco.tracker.dto.BudgetDto;
import zm.co.zanaco.tracker.dto.CostEntryDto;
import zm.co.zanaco.tracker.dto.InitiativeCreateDto;
import zm.co.zanaco.tracker.dto.InitiativeResponseDto;
import zm.co.zanaco.tracker.repository.InitiativeRepository;
import zm.co.zanaco.tracker.repository.UnitRepository;
import zm.co.zanaco.tracker.service.BudgetService;
import zm.co.zanaco.tracker.service.CostService;
import zm.co.zanaco.tracker.service.InitiativeService;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Inserts sample Units and 5 Initiatives (with Budgets and CostEntries) on startup.
 * Runs only when no initiatives are present so restarts are idempotent.
 * Disabled in the {@code prod} Spring profile.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final InitiativeRepository initiativeRepository;
    private final UnitRepository        unitRepository;
    private final InitiativeService     initiativeService;
    private final BudgetService         budgetService;
    private final CostService           costService;

    @Override
    public void run(String... args) {
        if (initiativeRepository.count() > 0) {
            log.info("[DevDataSeeder] Sample data already present – skipping.");
            return;
        }
        log.info("[DevDataSeeder] Inserting sample data…");
        seedUnits();
        seedInitiatives();
        log.info("[DevDataSeeder] Done – 5 initiatives seeded.");
    }

    // -------------------------------------------------------------------------
    // Units
    // -------------------------------------------------------------------------

    private void seedUnits() {
        saveUnit("Information Technology",  "Core IT delivery and infrastructure");
        saveUnit("Finance & Treasury",      "Financial planning and cost control");
        saveUnit("Digital Innovation",      "Digital products and customer experience");
        saveUnit("Risk & Compliance",       "Regulatory compliance and risk management");
        saveUnit("Operations",              "Business operations and process efficiency");
    }

    private void saveUnit(String name, String description) {
        if (!unitRepository.existsByNameIgnoreCase(name)) {
            Unit u = new Unit();
            u.setName(name);
            u.setDescription(description);
            unitRepository.save(u);
        }
    }

    // -------------------------------------------------------------------------
    // Initiatives
    // -------------------------------------------------------------------------

    private void seedInitiatives() {
        // 1 – Core Banking Upgrade  (IN_PROGRESS, Q1 2026)
        InitiativeResponseDto i1 = initiativeService.createInitiative(new InitiativeCreateDto(
                "PRJ-2026-001",
                "Core Banking System Upgrade",
                "Full replacement of the legacy T24 core banking platform with Temenos Infinity.",
                "Information Technology",
                Priority.HIGH,
                InitiativeStatus.IN_PROGRESS,
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 9, 30),
                2026, 1
        ));
        attachVendorBudget(i1.id(), "2500000.00", "2026-01-10");
        addCost(i1.id(), CostType.INTERNAL_HOURS, "180000.00", "2026-02-28", "Sprint 1-4 internal dev hours");
        addCost(i1.id(), CostType.INFRA,          "95000.00",  "2026-03-15", "Cloud infrastructure provisioning");
        addCost(i1.id(), CostType.LABOUR,         "340000.00", "2026-03-31", "Vendor professional services Q1");

        // 2 – Mobile Banking App  (UAT, Q1 2026)
        InitiativeResponseDto i2 = initiativeService.createInitiative(new InitiativeCreateDto(
                "PRJ-2026-002",
                "Mobile Banking App Redesign",
                "Complete UX overhaul of the Zanaco mobile app with biometric login and instant transfers.",
                "Digital Innovation",
                Priority.HIGH,
                InitiativeStatus.UAT,
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 6, 30),
                2026, 1
        ));
        attachVendorBudget(i2.id(), "850000.00", "2025-12-20");
        addCost(i2.id(), CostType.INTERNAL_HOURS, "120000.00", "2026-02-15", "Internal design and dev");
        addCost(i2.id(), CostType.LICENSE,        "45000.00",  "2026-01-20", "UI component licences");
        addCost(i2.id(), CostType.SERVICES,       "210000.00", "2026-03-10", "Outsourced testing partner");

        // 3 – Data Warehouse Migration  (COMPLETED, Q4 2025)
        InitiativeResponseDto i3 = initiativeService.createInitiative(new InitiativeCreateDto(
                "PRJ-2025-004",
                "Data Warehouse Migration to Snowflake",
                "Migrate on-premise Oracle DWH to Snowflake cloud; decommission legacy servers.",
                "Information Technology",
                Priority.MEDIUM,
                InitiativeStatus.COMPLETED,
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 12, 31),
                2025, 4
        ));
        attachVendorBudget(i3.id(), "620000.00", "2025-06-30");
        addCost(i3.id(), CostType.INTERNAL_HOURS, "95000.00",  "2025-11-30", "Internal data engineering");
        addCost(i3.id(), CostType.INFRA,          "38000.00",  "2025-12-15", "Snowflake credits");
        addCost(i3.id(), CostType.MATERIALS,      "12000.00",  "2025-12-20", "Data validation tooling");

        // 4 – AI Fraud Detection  (PLANNED, Q2 2026)
        InitiativeResponseDto i4 = initiativeService.createInitiative(new InitiativeCreateDto(
                "PRJ-2026-003",
                "AI-Powered Fraud Detection",
                "Deploy real-time ML models on transaction streams to reduce fraud losses by 30%.",
                "Risk & Compliance",
                Priority.HIGH,
                InitiativeStatus.PLANNED,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 12, 31),
                2026, 2
        ));
        attachVendorBudget(i4.id(), "1200000.00", "2026-03-15");
        addCost(i4.id(), CostType.LICENSE, "75000.00", "2026-03-20", "ML platform annual licence");

        // 5 – Digital Onboarding Portal  (ON_HOLD, Q2 2026)
        InitiativeResponseDto i5 = initiativeService.createInitiative(new InitiativeCreateDto(
                "PRJ-2026-004",
                "Digital Onboarding Portal",
                "End-to-end digital account opening with KYC verification and instant card issuance.",
                "Digital Innovation",
                Priority.MEDIUM,
                InitiativeStatus.ON_HOLD,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 10, 31),
                2026, 2
        ));
        attachVendorBudget(i5.id(), "980000.00", "2026-02-28");
        addCost(i5.id(), CostType.INTERNAL_HOURS, "55000.00",  "2026-03-15", "Discovery phase internal hours");
        addCost(i5.id(), CostType.OVERHEAD,       "18000.00",  "2026-03-31", "Project management overhead");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void attachVendorBudget(Long initiativeId, String amount, String approvedDate) {
        budgetService.attachBudget(initiativeId, new BudgetDto(
                new BigDecimal(amount),
                "ZMW",
                LocalDate.parse(approvedDate),
                "Vendor"
        ));
    }

    private void addCost(Long initiativeId, CostType type, String amount,
                         String recordedDate, String notes) {
        costService.addCostEntry(initiativeId, new CostEntryDto(
                type,
                new BigDecimal(amount),
                "ZMW",
                LocalDate.parse(recordedDate),
                notes
        ));
    }
}
