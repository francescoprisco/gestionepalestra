package it.francescoprisco.gestionepalestra.controller;

import it.francescoprisco.gestionepalestra.dto.MessageResponse;
import it.francescoprisco.gestionepalestra.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nfc") // Gestisce tutti gli URL che iniziano con /api/nfc
public class NfcController {

    @Autowired
    private BookingService bookingService;

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