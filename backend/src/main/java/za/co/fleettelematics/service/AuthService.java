package za.co.fleettelematics.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import za.co.fleettelematics.dto.request.BootstrapAdminRequest;
import za.co.fleettelematics.dto.request.LoginRequest;
import za.co.fleettelematics.dto.response.BootstrapResponse;
import za.co.fleettelematics.dto.response.LoginResponse;
import za.co.fleettelematics.entity.User;
import za.co.fleettelematics.repository.UserRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final String issuer;
    private final long expirationSeconds;
    private final boolean bootstrapEnabled;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate,
            @Value("${app.security.jwt.issuer}") String issuer,
            @Value("${app.security.jwt.expiration-seconds}") long expirationSeconds,
            @Value("${app.security.bootstrap.enabled:false}") boolean bootstrapEnabled) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.issuer = issuer;
        if (expirationSeconds < 60 || expirationSeconds > 86400) {
            throw new IllegalArgumentException("JWT expiration must be between 60 and 86400 seconds.");
        }
        this.expirationSeconds = expirationSeconds;
        this.bootstrapEnabled = bootstrapEnabled;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername().trim(), request.getPassword()));

        Instant now = Instant.now();
        List<String> authorityNames = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(authentication.getName())
                .claim("roles", authorityNames)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        List<String> roles = authorityNames.stream()
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .toList();
        return new LoginResponse(token, "Bearer", expirationSeconds, authentication.getName(), roles);
    }

    @Transactional
    public BootstrapResponse bootstrapAdministrator(BootstrapAdminRequest request) {
        if (!bootstrapEnabled) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Initial administrator setup is disabled. Enable BOOTSTRAP_ENABLED temporarily for first setup.");
        }

        // A transaction-scoped PostgreSQL advisory lock makes first-account setup single-writer.
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            try (var statement = connection.createStatement()) {
                statement.execute("SELECT pg_advisory_xact_lock(1910112, 1)");
            }
            return null;
        });

        if (userRepository.count() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An account already exists. Initial administrator setup can only run once.");
        }

        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must not exceed 72 bytes when encoded as UTF-8.");
        }

        User administrator = new User();
        administrator.setUsername(request.getUsername().trim());
        administrator.setEmail(request.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
        administrator.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        administrator.setRole("ADMIN");
        administrator.setStatus("ACTIVE");

        User saved = userRepository.saveAndFlush(administrator);
        return new BootstrapResponse(
                "Initial administrator created. Disable BOOTSTRAP_ENABLED and restart the backend now.",
                saved.getUsername(), saved.getRole());
    }
}
