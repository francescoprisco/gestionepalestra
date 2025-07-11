package it.francescoprisco.gestionepalestra.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;

/**
 * Rappresenta un utente receptionist del sistema.
 */
@Document(collection = "receptionists")
@Data
public class Receptionist {
    @Id
    private String id;
    
    // CAMPI AGGIUNTI
    private String nome;
    private String cognome;

    private String email;
    private String password; // Salvata come hash Bcrypt
    private Set<String> roles; // Ruoli per la sicurezza (es. "ROLE_RECEPTIONIST")
}