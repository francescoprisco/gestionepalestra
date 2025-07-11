package it.francescoprisco.gestionepalestra.repository;

import it.francescoprisco.gestionepalestra.model.Cliente;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ClienteRepository extends MongoRepository<Cliente, String> {

    Optional<Cliente> findByEmail(String email);
    
    Optional<Cliente> findByNfcId(String nfcId);

    Boolean existsByEmail(String email);
}