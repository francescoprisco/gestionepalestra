package it.francescoprisco.gestionepalestra.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;

@Data
public class RegisterRequest {
    @NotBlank
    private String nome;

    @NotBlank
    private String cognome;
    
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String nfcId;

    // Opzionale, per passare i ruoli. Se non specificato, ne assegneremo uno di default.
    private Set<String> roles;
}