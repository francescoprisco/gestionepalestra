package it.francescoprisco.gestionepalestra.dto;

import lombok.Data;
import java.util.List;

// Oggetto per la risposta di login, contenente il token
@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private List<String> roles;

    public JwtResponse(String accessToken, String email, List<String> roles) {
        this.token = accessToken;
        this.email = email;
        this.roles = roles;
    }
}