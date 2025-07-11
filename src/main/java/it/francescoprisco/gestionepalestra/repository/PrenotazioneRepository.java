package it.francescoprisco.gestionepalestra.repository;

import it.francescoprisco.gestionepalestra.model.Prenotazione;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;

public interface PrenotazioneRepository extends MongoRepository<Prenotazione, String> {

    long countByDataAndFasciaOraria_Id(LocalDate data, String fasciaOrariaId);

    boolean existsByCliente_IdAndDataAndFasciaOraria_Id(String clienteId, LocalDate data, String fasciaOrariaId);
    
    boolean existsByCliente_IdAndData(String clienteId, LocalDate data);

    List<Prenotazione> findByCliente_Mail(String mail);

    List<Prenotazione> findByData(LocalDate data);
}