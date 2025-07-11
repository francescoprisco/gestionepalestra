package it.francescoprisco.gestionepalestra.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;

@Document(collection = "clienti")
@Data
public class Cliente {
    @Id
    private String id;
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private String nfcId;
    private Set<String> roles; // e.g., "ROLE_CLIENTE"
}