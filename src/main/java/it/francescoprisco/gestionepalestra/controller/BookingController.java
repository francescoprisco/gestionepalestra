package it.francescoprisco.gestionepalestra.controller;

import it.francescoprisco.gestionepalestra.dto.BookingRequest;
import it.francescoprisco.gestionepalestra.dto.FasciaOrariaDisponibilita;
import it.francescoprisco.gestionepalestra.dto.MessageResponse;
import it.francescoprisco.gestionepalestra.model.Prenotazione;
import it.francescoprisco.gestionepalestra.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;


/**
 * Controller per le operazioni relative alle prenotazioni eseguite da un cliente.
 * Tutti gli endpoint richiedono il ruolo 'ROLE_CLIENTE'.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;
   /**
     * Restituisce le fasce orarie con posti disponibili per una data specifica.
     * @param data La data nel formato YYYY-MM-DD.
     * @return Una lista di DTO con i dettagli delle fasce orarie disponibili.
     */
    @GetMapping("/availability/{data}")
    public ResponseEntity<List<FasciaOrariaDisponibilita>> getAvailability(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(bookingService.getAvailabilityForDay(data));
    }
    /**
     * Restituisce la lista di tutte le prenotazioni effettuate dal cliente autenticato.
     * @param principal Oggetto che rappresenta l'utente autenticato (iniettato da Spring Security).
     * @return Lista delle prenotazioni del cliente.
     */
    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('CLIENTE')") // <-- SPOSTATA QUI
    public ResponseEntity<List<Prenotazione>> getMyBookings(Principal principal) {
        // Usa il nuovo metodo del service
        return ResponseEntity.ok(bookingService.getMyBookings(principal.getName()));
    }
    /**
     * Crea una nuova prenotazione per il cliente autenticato.
     * @param request DTO con ID della fascia oraria e data.
     * @param principal L'utente autenticato.
     * @return La prenotazione creata.
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')") // <-- SPOSTATA QUI
    public ResponseEntity<Prenotazione> createBooking(@RequestBody BookingRequest request, Principal principal) {
        Prenotazione prenotazione = bookingService.createBooking(
            principal.getName(),
            request.getFasciaOrariaId(),
            request.getData()
        );
        return ResponseEntity.ok(prenotazione);
    }
    /**
     * Modifica una prenotazione esistente del cliente.
     * @param bookingId ID della prenotazione da modificare.
     * @param request DTO con i nuovi dati della prenotazione.
     * @param principal L'utente autenticato.
     * @return La prenotazione modificata.
     */
    @PutMapping("/{bookingId}")
    @PreAuthorize("hasRole('CLIENTE')") // <-- SPOSTATA QUI
    public ResponseEntity<Prenotazione> modifyBooking(
        @PathVariable String bookingId,
        @RequestBody BookingRequest request,
        Principal principal) {
        Prenotazione prenotazione = bookingService.modifyBooking(
            bookingId,
            principal.getName(),
            request.getFasciaOrariaId(),
            request.getData()
        );
        return ResponseEntity.ok(prenotazione);
    }
    /**
     * Cancella una prenotazione esistente del cliente.
     * @param bookingId ID della prenotazione da cancellare.
     * @param principal L'utente autenticato.
     * @return Un messaggio di conferma.
     */
    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasRole('CLIENTE')") // <-- SPOSTATA QUI
    public ResponseEntity<MessageResponse> cancelBooking(@PathVariable String bookingId, Principal principal) {
        bookingService.cancelBooking(bookingId, principal.getName());
        return ResponseEntity.ok(new MessageResponse("Prenotazione cancellata con successo."));
    }
}