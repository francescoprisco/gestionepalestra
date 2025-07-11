package it.francescoprisco.gestionepalestra.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Document(collection = "prenotazioni")
@Data
public class Prenotazione {
    @Id
    private String id; // ID_prenotazione

    @DBRef
    private Cliente cliente; // Link to the registered client

    // For occasional clients booked by receptionist
    private String nomeClienteOccasionale;

    @DBRef
    private FasciaOraria fasciaOraria;

    private LocalDate data;
}