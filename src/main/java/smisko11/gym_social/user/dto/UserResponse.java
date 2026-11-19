package smisko11.gym_social.user.dto;

import smisko11.gym_social.user.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String username,
        Role role,
        Instant createdAt
) {}