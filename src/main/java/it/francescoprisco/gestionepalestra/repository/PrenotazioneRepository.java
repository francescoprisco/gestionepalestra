package it.francescoprisco.gestionepalestra.repository;

import it.francescoprisco.gestionepalestra.model.Prenotazione;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;
/**
 * Repository per l'accesso ai dati delle Prenotazioni.
 */
public interface PrenotazioneRepository extends MongoRepository<Prenotazione, String> {
    /**
     * Conta le prenotazioni in un dato intervallo di tempo per una specifica fascia oraria.
     * Usato per verificare la disponibilità.
     */
    long countByDataBetweenAndFasciaOraria_Id(Instant start, Instant end, String fasciaOrariaId);
    /**
     * Controlla se un cliente ha già una prenotazione in un dato intervallo di tempo.
     * Usato per la validazione NFC.
     */
    boolean existsByCliente_IdAndDataBetween(String clienteId, Instant start, Instant end);
    /**
     * Trova tutte le prenotazioni di un cliente specifico.
     */
    List<Prenotazione> findByCliente_Id(String clienteId);
    /**
     * Trova tutte le prenotazioni in un dato intervallo di tempo e con un id cliente specifico.
     */
    List<Prenotazione> findByCliente_IdAndDataBetween(String clienteId, Instant start, Instant end);
    /**
     * Trova tutte le prenotazioni in un dato intervallo di tempo.
     * Usato dal receptionist per vedere le prenotazioni del giorno.
     */
    List<Prenotazione> findByDataBetween(Instant start, Instant end);
}