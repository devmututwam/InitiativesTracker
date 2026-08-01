package zm.co.zanaco.tracker.dto;

import java.time.Instant;
import java.util.List;

public record LoginResponse(
        String token,
        String tokenType,
        String username,
        List<String> roles,
        Instant expiresAt
) {}
