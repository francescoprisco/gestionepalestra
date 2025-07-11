package it.francescoprisco.gestionepalestra.controller;

import it.francescoprisco.gestionepalestra.dto.BookingRequest;
import it.francescoprisco.gestionepalestra.dto.MessageResponse;
import it.francescoprisco.gestionepalestra.dto.ReceptionistBookingRequest;
import it.francescoprisco.gestionepalestra.model.Cliente;
import it.francescoprisco.gestionepalestra.model.Prenotazione;
import it.francescoprisco.gestionepalestra.repository.ClienteRepository;
import it.francescoprisco.gestionepalestra.service.BookingService;
import jakarta.validation.Valid;
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
    private ClienteRepository clienteRepository;

    @GetMapping("/clients")
    public ResponseEntity<List<Cliente>> getAllClients() {
        return ResponseEntity.ok(clienteRepository.findAll());
    }

    @GetMapping("/bookings/day/{data}")
    public ResponseEntity<List<Prenotazione>> getBookingsForDay(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        // VERIFICA CHE QUESTA RIGA CHIAMI "bookingService" E NON "prenotazioneRepository"
        return ResponseEntity.ok(bookingService.getBookingsForDay(data));
    }

    @PostMapping("/bookings/registered-client")
    public ResponseEntity<Prenotazione> createBookingForRegisteredClient(@Valid @RequestBody ReceptionistBookingRequest request) {
        Prenotazione prenotazione = bookingService.createBookingForRegisteredClient(
            request.getClienteId(),
            request.getFasciaOrariaId(),
            request.getData()
        );
        return ResponseEntity.ok(prenotazione);
    }

    @PostMapping("/bookings/occasional")
    public ResponseEntity<Prenotazione> createBookingForOccasional(@Valid @RequestBody BookingRequest request) {
        Prenotazione prenotazione = bookingService.createBookingForOccasional(
            request.getNomeClienteOccasionale(),
            request.getFasciaOrariaId(),
            request.getData()
        );
        return ResponseEntity.ok(prenotazione);
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<MessageResponse> cancelAnyBooking(@PathVariable String bookingId) {
        bookingService.cancelAnyBooking(bookingId);
        return ResponseEntity.ok(new MessageResponse("Prenotazione cancellata dal receptionist."));
    }
}