package it.francescoprisco.gestionepalestra.repository;

import it.francescoprisco.gestionepalestra.model.Prenotazione;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;

public interface PrenotazioneRepository extends MongoRepository<Prenotazione, String> {

    long countByDataBetweenAndFasciaOraria_Id(Instant start, Instant end, String fasciaOrariaId);

    boolean existsByCliente_IdAndDataBetween(String clienteId, Instant start, Instant end);

    List<Prenotazione> findByCliente_Id(String clienteId);

    List<Prenotazione> findByCliente_IdAndDataBetween(String clienteId, Instant start, Instant end);
    
    List<Prenotazione> findByDataBetween(Instant start, Instant end);
}