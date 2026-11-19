package smisko11.gym_social.catalog.musclegroup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MuscleGroupRequest(
        @NotBlank(message = "Naziv grupe mišića je obavezan")
        @Size(max = 50, message = "Naziv može imati najviše 50 karaktera")
        String name
) {}