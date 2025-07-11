package it.francescoprisco.gestionepalestra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // Crea il costruttore con tutti i campi (es. new MessageResponse("messaggio"))
@NoArgsConstructor  // Crea il costruttore vuoto
public class MessageResponse {
    private String message;
}