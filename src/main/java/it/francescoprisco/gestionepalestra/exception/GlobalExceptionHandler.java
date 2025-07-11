package it.francescoprisco.gestionepalestra.exception;

import it.francescoprisco.gestionepalestra.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

// Gestore globale per le eccezioni
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex, WebRequest request) {
        // Restituisce un errore 400 (Bad Request) con il messaggio dell'eccezione.
        // È utile per errori di validazione del business logic (es. "Posti esauriti").
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}