package it.francescoprisco.gestionepalestra.repository;

import it.francescoprisco.gestionepalestra.model.Prenotazione;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.Instant;
import java.util.List;

public interface PrenotazioneRepository extends MongoRepository<Prenotazione, String> {

    /**
     * Usa una @Query esplicita per contare le prenotazioni in un intervallo di tempo.
     * $gte (greater than or equal) e $lt (less than) è l'approccio corretto.
     */
    @Query(value = "{ 'data': { '$gte': ?0, '$lt': ?1 }, 'fasciaOraria.$id': ?2 }", count = true)
    long countByDataBetweenAndFasciaOraria_Id(Instant start, Instant end, String fasciaOrariaId);
    
    /**
     * Usa una @Query esplicita per verificare l'esistenza di una prenotazione.
     */
    @Query(value = "{ 'cliente.$id': ?0, 'data': { '$gte': ?1, '$lt': ?2 } }", exists = true)
    boolean existsByCliente_IdAndDataBetween(String clienteId, Instant start, Instant end);

    List<Prenotazione> findByCliente_Id(String clienteId);

    /**
     * Usa una @Query esplicita per trovare tutte le prenotazioni in un intervallo di tempo.
     */
    @Query("{ 'data': { '$gte': ?0, '$lt': ?1 } }")
    List<Prenotazione> findByDataBetween(Instant start, Instant end);
    
    List<Prenotazione> findByCliente_IdAndDataBetween(String clienteId, Instant start, Instant end);
}