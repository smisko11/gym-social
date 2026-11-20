package smisko11.gym_social.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email je obavezan")
        @Email(message = "Email nije ispravan")
        String email,

        @NotBlank(message = "Lozinka je obavezna")
        String password
) {
}
