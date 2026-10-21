package za.co.fleettelematics.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateUserStatusRequest {
    @NotBlank
    @Pattern(regexp = "ACTIVE|INACTIVE|SUSPENDED", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
