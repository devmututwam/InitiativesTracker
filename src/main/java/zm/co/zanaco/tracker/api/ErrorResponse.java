package zm.co.zanaco.tracker.api;

import java.time.Instant;
import java.util.Map;

/**
 * Uniform error envelope returned by {@link GlobalExceptionHandler} for every
 * error response.  {@code fieldErrors} is populated only for validation failures.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, Instant.now(), Map.of());
    }

    public static ErrorResponse ofValidation(int status, String message, Map<String, String> fieldErrors) {
        return new ErrorResponse(status, "Validation Failed", message, Instant.now(), fieldErrors);
    }
}
