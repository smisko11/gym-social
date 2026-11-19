package smisko11.gym_social.catalog.gym.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GymRequest(

        @NotBlank(message = "Naziv je obavezan")
        @Size(max = 150, message = "Naziv može imati najviše 150 karaktera")
        String name,

        @NotBlank(message = "Grad je obavezan")
        @Size(max = 100, message = "Grad može imati najviše 100 karaktera")
        String city,

        @NotBlank(message = "Adresa je obavezna")
        @Size(max = 255, message = "Adresa može imati najviše 255 karaktera")
        String address,

        @DecimalMin(value = "-90.0", message = "Geografska širina mora biti između -90 i 90")
        @DecimalMax(value = "90.0", message = "Geografska širina mora biti između -90 i 90")
        @Digits(integer = 3, fraction = 6, message = "Najviše 6 decimala")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Geografska dužina mora biti između -180 i 180")
        @DecimalMax(value = "180.0", message = "Geografska dužina mora biti između -180 i 180")
        @Digits(integer = 3, fraction = 6, message = "Najviše 6 decimala")
        BigDecimal longitude
) {}