package it.francescoprisco.gestionepalestra.dto;

import lombok.Data;
import java.time.LocalDate;

// Oggetto per la richiesta di prenotazione 
@Data
public class BookingRequest {
    private String fasciaOrariaId;
    private LocalDate data;
    // Usato solo dalla receptionist per i clienti occasionali
    private String nomeClienteOccasionale;
}