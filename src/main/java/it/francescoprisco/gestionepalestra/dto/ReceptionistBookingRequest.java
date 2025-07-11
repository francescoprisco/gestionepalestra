package it.francescoprisco.gestionepalestra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ReceptionistBookingRequest {
    @NotBlank
    private String clienteId; // ID del cliente registrato

    @NotBlank
    private String fasciaOrariaId;

    @NotNull
    private LocalDate data;
}