package smisko11.gym_social.catalog.gym.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GymResponse(
        Long id,
        String name,
        String city,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant createdAt
) {}