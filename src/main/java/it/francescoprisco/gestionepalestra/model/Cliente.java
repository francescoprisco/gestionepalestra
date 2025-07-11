package it.francescoprisco.gestionepalestra.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;


/**
 * Rappresenta un cliente registrato nel sistema.
 */
@Document(collection = "clienti")
@Data
public class Cliente {
    @Id
    private String id;
    private String nome;
    private String cognome;
    private String email;
    private String password; // Salvata come hash Bcrypt
    private String nfcId;  // Codice univoco della tessera NFC
    private Set<String> roles; // Ruoli per la sicurezza (es. "ROLE_CLIENTE")
}