package it.francescoprisco.gestionepalestra.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant; // CAMBIATO DA LocalDate A Instant

@Document(collection = "prenotazioni")
@Data
public class Prenotazione {
    @Id
    private String id;

    @DBRef
    private Cliente cliente;

    private String nomeClienteOccasionale;

    @DBRef
    private FasciaOraria fasciaOraria;

    // Usiamo Instant per rappresentare un momento esatto, indipendente dal fuso orario
    private Instant data; 
}