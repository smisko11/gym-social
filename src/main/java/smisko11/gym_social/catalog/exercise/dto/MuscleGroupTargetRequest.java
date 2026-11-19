package smisko11.gym_social.catalog.exercise.dto;

import jakarta.validation.constraints.NotNull;

public record MuscleGroupTargetRequest(
        @NotNull(message = "Id grupe mišića je obavezan")
        Long muscleGroupId,

        boolean primary
) {}