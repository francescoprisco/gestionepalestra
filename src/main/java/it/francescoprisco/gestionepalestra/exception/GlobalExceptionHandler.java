package it.francescoprisco.gestionepalestra.exception;

import it.francescoprisco.gestionepalestra.dto.MessageResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestore di eccezioni globale per tutta l'applicazione.
 * Cattura specifiche eccezioni e le converte in risposte JSON standardizzate.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Gestisce le eccezioni di validazione dei DTO (causate da @Valid).
     * Restituisce un errore 400 (Bad Request) con i dettagli dei campi non validi.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Gestisce le eccezioni di business logic generiche (es. "Posti esauriti").
     * Restituisce un errore 400 (Bad Request).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Gestisce gli errori di accesso negato (es. un cliente prova ad accedere a un'area admin).
     * Restituisce un errore 403 (Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return new ResponseEntity<>(new MessageResponse("Accesso Negato: non si dispone dei permessi necessari per accedere a questa risorsa."), HttpStatus.FORBIDDEN);
    }

    /**
     * Gestore "catch-all" per qualsiasi altra eccezione non gestita.
     * Restituisce un errore 500 (Internal Server Error) per evitare di esporre dettagli interni del server.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGlobalException(Exception ex, WebRequest request) {
        // Log dell'errore per il debug interno (opzionale ma consigliato)
        logger.error("Errore interno non gestito: ", ex);
        return new ResponseEntity<>(new MessageResponse("Si è verificato un errore interno al server."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}