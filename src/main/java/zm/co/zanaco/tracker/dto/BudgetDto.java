package zm.co.zanaco.tracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetDto(

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false)
        @Digits(integer = 15, fraction = 2)
        BigDecimal amount,

        @NotBlank
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
        String currency,

        LocalDate approvedDate,

        @Size(max = 100)
        String budgetSource
) {}
