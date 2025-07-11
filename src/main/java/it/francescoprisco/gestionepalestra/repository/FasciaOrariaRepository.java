package it.francescoprisco.gestionepalestra.repository;

import it.francescoprisco.gestionepalestra.model.FasciaOraria;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FasciaOrariaRepository extends MongoRepository<FasciaOraria, String> {
}