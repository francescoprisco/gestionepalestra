package it.francescoprisco.gestionepalestra.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
/**
 * Componente che gestisce i tentativi di accesso non autorizzati.
 * Viene attivato da Spring Security quando un utente non autenticato
 * prova ad accedere a una risorsa protetta.
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthEntryPoint.class);
    /**
     * Metodo chiamato quando l'autenticazione fallisce.
     * Invece di restituire la pagina di errore di default, costruisce una risposta JSON personalizzata
     * con status 401 Unauthorized.
     * @param request La richiesta che ha causato l'eccezione.
     * @param response La risposta HTTP.
     * @param authException L'eccezione che ha causato l'errore.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        logger.error("Unauthorized error: {}", authException.getMessage());

        // Impostiamo il tipo di risposta su JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // Impostiamo lo stato HTTP su 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Creiamo il corpo della nostra risposta JSON
        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", "Accesso negato. È richiesta l'autenticazione completa.");
        body.put("path", request.getServletPath());

        // Usiamo ObjectMapper per scrivere il nostro JSON nella risposta
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}