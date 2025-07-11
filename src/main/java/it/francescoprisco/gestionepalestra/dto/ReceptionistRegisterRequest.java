package it.francescoprisco.gestionepalestra.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReceptionistRegisterRequest {
    // CAMPI AGGIUNTI
    @NotBlank
    private String nome;

    @NotBlank
    private String cognome;

    @NotBlank
    private String email;

    @NotBlank
    private String password;
}