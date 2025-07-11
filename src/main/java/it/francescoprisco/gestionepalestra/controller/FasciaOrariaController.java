package it.francescoprisco.gestionepalestra.controller;

import it.francescoprisco.gestionepalestra.dto.FasciaOrariaRequest;
import it.francescoprisco.gestionepalestra.model.FasciaOraria;
import it.francescoprisco.gestionepalestra.repository.FasciaOrariaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/slots") // Endpoint protetti per amministrazione
@PreAuthorize("hasRole('RECEPTIONIST')") // Solo i receptionist possono accedere
public class FasciaOrariaController {

    @Autowired
    private FasciaOrariaRepository fasciaOrariaRepository;

    // --- NUOVO ENDPOINT PER CREARE UNA FASCIA ORARIA ---
    @PostMapping
    public ResponseEntity<FasciaOraria> createFasciaOraria(@Valid @RequestBody FasciaOrariaRequest request) {
        FasciaOraria nuovaFascia = new FasciaOraria();
        nuovaFascia.setNome(request.getNome());
        nuovaFascia.setOraInizio(request.getOraInizio());
        nuovaFascia.setOraFine(request.getOraFine());
        nuovaFascia.setPostiTotali(request.getPostiTotali());
        nuovaFascia.setActive(true); // Di default è attiva

        fasciaOrariaRepository.save(nuovaFascia);
        return ResponseEntity.ok(nuovaFascia);
    }

    // --- ENDPOINT PER VISUALIZZARE TUTTE LE FASCE ORARIE CREATE ---
    @GetMapping
    public ResponseEntity<List<FasciaOraria>> getAllFasceOrarie() {
        return ResponseEntity.ok(fasciaOrariaRepository.findAll());
    }

    // --- ENDPOINT PER ELIMINARE UNA FASCIA ORARIA ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFasciaOraria(@PathVariable String id) {
        fasciaOrariaRepository.deleteById(id);
        return ResponseEntity.ok("Fascia oraria eliminata con successo.");
    }
}