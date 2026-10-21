package za.co.fleettelematics.dto.response;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String username,
        List<String> roles) {
}
