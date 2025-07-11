package it.francescoprisco.gestionepalestra.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * Rappresenta una singola prenotazione effettuata da un cliente o receptionist.
 */
@Document(collection = "prenotazioni")
@Data
public class Prenotazione {
    @Id
    private String id;

    /**
     * Riferimento al cliente registrato.
     * Nullo se la prenotazione è per un cliente occasionale.
     */
    @DBRef
    private Cliente cliente;

    /**
     * Nome del cliente occasionale, inserito dal receptionist.
     * Nullo se la prenotazione è per un cliente registrato.
     */
    private String nomeClienteOccasionale;

    /**
     * Riferimento alla fascia oraria prenotata.
     */
    @DBRef
    private FasciaOraria fasciaOraria;

    /**
     * Data della prenotazione, salvata come Instant per gestire correttamente i fusi orari.
     * Rappresenta l'inizio della giornata (mezzanotte) nel fuso orario del server.
     */
    private Instant data;
}