package it.francescoprisco.gestionepalestra.repository;

import it.francescoprisco.gestionepalestra.model.Receptionist;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ReceptionistRepository extends MongoRepository<Receptionist, String> {
    Optional<Receptionist> findByMail(String email);
    Boolean existsByMail(String email);
}