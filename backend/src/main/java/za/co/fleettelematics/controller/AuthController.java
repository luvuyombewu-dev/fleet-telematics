package za.co.fleettelematics.controller;

import java.util.List;
import java.util.Locale;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.co.fleettelematics.dto.request.BootstrapAdminRequest;
import za.co.fleettelematics.dto.request.LoginRequest;
import za.co.fleettelematics.dto.response.BootstrapResponse;
import za.co.fleettelematics.dto.response.CurrentUserResponse;
import za.co.fleettelematics.dto.response.LoginResponse;
import za.co.fleettelematics.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/bootstrap")
    public ResponseEntity<BootstrapResponse> bootstrap(@Valid @RequestBody BootstrapAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.bootstrapAdministrator(request));
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
        List<String> authorities = jwt.getClaimAsStringList("roles");
        List<String> roles = authorities == null ? List.of() : authorities.stream()
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .map(role -> role.toUpperCase(Locale.ROOT))
                .toList();
        return new CurrentUserResponse(jwt.getSubject(), roles);
    }
}
