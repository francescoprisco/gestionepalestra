package it.francescoprisco.gestionepalestra.controller;

import it.francescoprisco.gestionepalestra.dto.MessageResponse;
import it.francescoprisco.gestionepalestra.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * Controller pubblico per la validazione degli accessi tramite NFC.
 */
@RestController
@RequestMapping("/api/nfc")
public class NfcController {

    @Autowired
    private BookingService bookingService;
    /**
     * Valida una tessera NFC e controlla se esiste una prenotazione valida
     * per il giorno corrente, con una finestra di tolleranza.
     * @param nfcId Il codice della tessera NFC passata.
     * @return Messaggio di "Accesso Consentito" o "Accesso Negato".
     */
    @GetMapping("/validate/{nfcId}")
    public ResponseEntity<?> validateBooking(@PathVariable String nfcId) {
        boolean isValid = bookingService.validateNfcBooking(nfcId);
        if (isValid) {
            return ResponseEntity.ok(new MessageResponse("Accesso Consentito."));
        } else {
            // Restituisce 403 Forbidden se la prenotazione non è valida
            return ResponseEntity.status(403).body(new MessageResponse("Accesso Negato: Prenotazione non valida o non trovata."));
        }
    }
}