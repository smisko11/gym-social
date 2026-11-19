package smisko11.gym_social.catalog.exercise.dto;

import java.util.List;

public record ExerciseResponse(
        Long id,
        String name,
        String description,
        boolean bodyweight,
        List<MuscleGroupTargetResponse> muscleGroups
) {}