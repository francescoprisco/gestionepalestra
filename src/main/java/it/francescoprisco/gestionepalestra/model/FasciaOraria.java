package it.francescoprisco.gestionepalestra.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalTime;

@Document(collection = "fasce_orarie")
@Data
public class FasciaOraria {
    @Id
    private String id;
    private String nome; // e.g., "09:00 - 10:00"
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private int postiTotali; // numPosti from Palestra
    private boolean active = true;
}