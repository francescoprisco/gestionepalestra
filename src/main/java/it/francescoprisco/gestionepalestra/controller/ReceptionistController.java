package it.francescoprisco.gestionepalestra.controller;

import it.francescoprisco.gestionepalestra.dto.BookingRequest;
import it.francescoprisco.gestionepalestra.dto.MessageResponse;
import it.francescoprisco.gestionepalestra.dto.ReceptionistBookingRequest; // Importa il nuovo DTO
import it.francescoprisco.gestionepalestra.model.Prenotazione;
import it.francescoprisco.gestionepalestra.repository.PrenotazioneRepository;
import it.francescoprisco.gestionepalestra.service.BookingService;
import jakarta.validation.Valid; // Importa @Valid
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/receptionist")
@PreAuthorize("hasRole('RECEPTIONIST')")
public class ReceptionistController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    // ... (metodo getBookingsForDay rimane invariato) ...
    @GetMapping("/bookings/day/{data}")
    public ResponseEntity<List<Prenotazione>> getBookingsForDay(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(prenotazioneRepository.findByData(data));
    }

    // --- NUOVO ENDPOINT PER PRENOTARE UN CLIENTE REGISTRATO ---
    @PostMapping("/bookings/registered-client")
    public ResponseEntity<Prenotazione> createBookingForRegisteredClient(@Valid @RequestBody ReceptionistBookingRequest request) {
        Prenotazione prenotazione = bookingService.createBookingForRegisteredClient(
            request.getClienteId(),
            request.getFasciaOrariaId(),
            request.getData()
        );
        return ResponseEntity.ok(prenotazione);
    }
    
    // Endpoint per prenotare un cliente occasionale
    @PostMapping("/bookings/occasional")
    public ResponseEntity<Prenotazione> createBookingForOccasional(@RequestBody BookingRequest request) {
        Prenotazione prenotazione = bookingService.createBookingForOccasional(
            request.getNomeClienteOccasionale(),
            request.getFasciaOrariaId(),
            request.getData()
        );
        return ResponseEntity.ok(prenotazione);
    }

    // ... (metodi cancelAnyBooking e disableTimeSlot rimangono invariati) ...
    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<MessageResponse> cancelAnyBooking(@PathVariable String bookingId) {
        bookingService.cancelAnyBooking(bookingId);
        return ResponseEntity.ok(new MessageResponse("Prenotazione cancellata dal receptionist."));
    }

    @PostMapping("/slots/disable/{fasciaOrariaId}")
    public ResponseEntity<MessageResponse> disableTimeSlot(@PathVariable String fasciaOrariaId) {
        bookingService.disableSlot(fasciaOrariaId);
        return ResponseEntity.ok(new MessageResponse("Fascia oraria disabilitata correttamente."));
    }
}