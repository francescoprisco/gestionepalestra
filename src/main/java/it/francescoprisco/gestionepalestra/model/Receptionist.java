package it.francescoprisco.gestionepalestra.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;

@Document(collection = "receptionists")
@Data
public class Receptionist {
    @Id
    private String id; // ID_Receptionist
    private String mail;
    private String password;
    private Set<String> roles; // e.g., "ROLE_RECEPTIONIST"
}