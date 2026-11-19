package smisko11.gym_social.catalog.exercise.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ExerciseRequest(
        @NotBlank(message = "Naziv vežbe je obavezan")
        @Size(max = 100, message = "Naziv može imati najviše 100 karaktera")
        String name,

        String description,

        boolean bodyweight,

        @NotEmpty(message = "Vežba mora gađati bar jednu grupu mišića")
        @Valid
        List<MuscleGroupTargetRequest> muscleGroups
) {}