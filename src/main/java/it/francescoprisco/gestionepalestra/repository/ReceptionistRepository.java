package it.francescoprisco.gestionepalestra.repository;

import it.francescoprisco.gestionepalestra.model.Receptionist;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

/**
 * Repository per l'accesso ai dati dei Receptionist.
 */
public interface ReceptionistRepository extends MongoRepository<Receptionist, String> {
    Optional<Receptionist> findByEmail(String email);
    Boolean existsByEmail(String email);
}