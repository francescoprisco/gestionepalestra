package it.francescoprisco.gestionepalestra.controller;

import it.francescoprisco.gestionepalestra.dto.BookingRequest;
import it.francescoprisco.gestionepalestra.dto.FasciaOrariaDisponibilita;
import it.francescoprisco.gestionepalestra.dto.MessageResponse;
import it.francescoprisco.gestionepalestra.model.Prenotazione;
import it.francescoprisco.gestionepalestra.repository.PrenotazioneRepository;
import it.francescoprisco.gestionepalestra.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@PreAuthorize("hasRole('CLIENTE')")
public class BookingController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @GetMapping("/availability/{data}")
    public ResponseEntity<List<FasciaOrariaDisponibilita>> getAvailability(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(bookingService.getAvailabilityForDay(data));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<Prenotazione>> getMyBookings(Principal principal) {
        // Nota: ho corretto il metodo in ClienteRepository per questa chiamata
        return ResponseEntity.ok(prenotazioneRepository.findByCliente_Email(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<Prenotazione> createBooking(@RequestBody BookingRequest request, Principal principal) {
        Prenotazione prenotazione = bookingService.createBooking(
            principal.getName(),
            request.getFasciaOrariaId(),
            request.getData()
        );
        return ResponseEntity.ok(prenotazione);
    }

    @PutMapping("/{bookingId}")
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

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<MessageResponse> cancelBooking(@PathVariable String bookingId, Principal principal) {
        bookingService.cancelBooking(bookingId, principal.getName());
        return ResponseEntity.ok(new MessageResponse("Prenotazione cancellata con successo."));
    }
}