package za.co.fleettelematics.dto.response;

import java.util.List;

public record CurrentUserResponse(String username, List<String> roles) {
}
