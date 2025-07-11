package it.francescoprisco.gestionepalestra.repository;

import it.francescoprisco.gestionepalestra.model.FasciaOraria;
import org.springframework.data.mongodb.repository.MongoRepository;
/**
 * Repository per l'accesso ai dati delle Fasce Orarie.
 */
public interface FasciaOrariaRepository extends MongoRepository<FasciaOraria, String> {
}