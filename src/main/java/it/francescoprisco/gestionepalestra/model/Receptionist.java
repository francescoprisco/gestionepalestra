package it.francescoprisco.gestionepalestra.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;

@Document(collection = "receptionists")
@Data
public class Receptionist {
    @Id
    private String id;
    
    // CAMPI AGGIUNTI
    private String nome;
    private String cognome;

    private String email;
    private String password;
    private Set<String> roles;
}