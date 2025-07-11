package it.francescoprisco.gestionepalestra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;

@Data
public class FasciaOrariaRequest {

    @NotBlank
    private String nome; // es. "09:00 - 10:00"

    @NotNull
    private LocalTime oraInizio; // es. "09:00"

    @NotNull
    private LocalTime oraFine; // es. "10:00"

    @NotNull
    private int postiTotali;
}