package it.francescoprisco.gestionepalestra.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalTime;
/**
 * Rappresenta una fascia oraria disponibile per la prenotazione.
 */
@Document(collection = "fasce_orarie")
@Data
public class FasciaOraria {
    @Id
    private String id;
    private String nome; // Nome descrittivo, es. "09:00 - 10:00"
    private LocalTime oraInizio; // Orario di inizio
    private LocalTime oraFine; // Orario di fine
    private int postiTotali; // Capienza massima per questa fascia
    private boolean active = true; // Flag per attivare/disattivare la fascia
}