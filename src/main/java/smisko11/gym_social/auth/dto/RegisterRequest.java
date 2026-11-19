package smisko11.gym_social.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email je obavezan")
        @Email(message = "Email nije ispravan")
        @Size(max = 255, message = "Email može imati najviše 255 karaktera")
        String email,

        @NotBlank(message = "Username je obavezan")
        @Pattern(
                regexp = "^[a-zA-Z0-9_.]{3,30}$",
                message = "Username može imati 3-30 karaktera: slova, brojeve, _ i ."
        )
        String username,

        @NotBlank(message = "Lozinka je obavezna")
        @Size(min = 8, max = 72, message = "Lozinka mora imati 8-72 karaktera")
        String password
) {}