package it.francescoprisco.gestionepalestra.controller;

import it.francescoprisco.gestionepalestra.dto.BookingRequest;
import it.francescoprisco.gestionepalestra.dto.MessageResponse;
import it.francescoprisco.gestionepalestra.dto.ReceptionistBookingRequest;
import it.francescoprisco.gestionepalestra.model.Prenotazione;
import it.francescoprisco.gestionepalestra.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
/**
 * Controller per le operazioni eseguite da un receptionist.
 * Tutti gli endpoint richiedono il ruolo 'ROLE_RECEPTIONIST'.
 */
@RestController
@RequestMapping("/api/receptionist")
@PreAuthorize("hasRole('RECEPTIONIST')")
public class ReceptionistController {

    @Autowired
    private BookingService bookingService;

    /**
     * Restituisce tutte le prenotazioni per un dato giorno.
     * @param data La data di interesse.
     * @return Lista di tutte le prenotazioni.
     */
    @GetMapping("/bookings/day/{data}")
    public ResponseEntity<List<Prenotazione>> getBookingsForDay(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(bookingService.getBookingsForDay(data));
    }
    /**
     * Crea una prenotazione per un cliente registrato specifico.
     * @param request DTO che contiene l'ID del cliente e i dettagli della prenotazione.
     * @return La prenotazione creata.
     */
    @PostMapping("/bookings/registered-client")
    public ResponseEntity<Prenotazione> createBookingForRegisteredClient(@Valid @RequestBody ReceptionistBookingRequest request) {
        Prenotazione prenotazione = bookingService.createBookingForRegisteredClient(
            request.getClienteId(),
            request.getFasciaOrariaId(),
            request.getData()
        );
        return ResponseEntity.ok(prenotazione);
    }
    /**
     * Crea una prenotazione per un cliente occasionale (non registrato).
     * @param request DTO che contiene il nome del cliente occasionale e i dettagli della prenotazione.
     * @return La prenotazione creata.
     */   
    @PostMapping("/bookings/occasional")
    public ResponseEntity<Prenotazione> createBookingForOccasional(@RequestBody BookingRequest request) {
        Prenotazione prenotazione = bookingService.createBookingForOccasional(
            request.getNomeClienteOccasionale(),
            request.getFasciaOrariaId(),
            request.getData()
        );
        return ResponseEntity.ok(prenotazione);
    }
    /**
     * Cancella una qualsiasi prenotazione nel sistema.
     * @param bookingId L'ID della prenotazione da cancellare.
     * @return Un messaggio di conferma.
     */
    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<MessageResponse> cancelAnyBooking(@PathVariable String bookingId) {
        bookingService.cancelAnyBooking(bookingId);
        return ResponseEntity.ok(new MessageResponse("Prenotazione cancellata dal receptionist."));
    }
    /**
     * Disabilita una fascia oraria.
     * @param bookingId L'ID della fascia oraria da disabilitare.
     * @return Un messaggio di conferma.
     */
    @PostMapping("/slots/disable/{fasciaOrariaId}")
    public ResponseEntity<MessageResponse> disableTimeSlot(@PathVariable String fasciaOrariaId) {
        bookingService.disableSlot(fasciaOrariaId);
        return ResponseEntity.ok(new MessageResponse("Fascia oraria disabilitata correttamente."));
    }
}