package za.co.fleettelematics.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.dto.request.UpdateUserStatusRequest;
import za.co.fleettelematics.dto.request.UserRequest;
import za.co.fleettelematics.dto.response.UserResponse;
import za.co.fleettelematics.entity.User;
import za.co.fleettelematics.exception.ResourceNotFoundException;
import za.co.fleettelematics.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must not exceed 72 bytes when encoded as UTF-8.");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole().trim().toUpperCase(Locale.ROOT));
        user.setStatus(request.getStatus() == null || request.getStatus().isBlank()
                ? "ACTIVE" : request.getStatus().trim().toUpperCase(Locale.ROOT));
        return toResponse(userRepository.saveAndFlush(user));
    }

    @Transactional
    public UserResponse updateStatus(Long id, UpdateUserStatusRequest request, String actingUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        String status = request.getStatus().trim().toUpperCase(Locale.ROOT);
        if (user.getUsername().equalsIgnoreCase(actingUsername) && !"ACTIVE".equals(status)) {
            throw new IllegalArgumentException("You cannot deactivate or suspend your own account.");
        }
        user.setStatus(status);
        return toResponse(userRepository.saveAndFlush(user));
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
