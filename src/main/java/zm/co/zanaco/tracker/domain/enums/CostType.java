package zm.co.zanaco.tracker.domain.enums;

public enum CostType {
    /** Internal staff time billed at hourly rate */
    INTERNAL_HOURS,
    /** Infrastructure and hosting costs */
    INFRA,
    /** Software licensing fees */
    LICENSE,
    // Legacy / general types kept for backward compatibility
    LABOUR,
    MATERIALS,
    EQUIPMENT,
    SERVICES,
    OVERHEAD,
    OTHER
}
