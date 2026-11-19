package smisko11.gym_social.catalog.exercise.dto;

import jakarta.validation.constraints.NotNull;

public record MuscleGroupTargetResponse(
        Long muscleGroupId,
        String name,
        boolean primary
) {}