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
/**
 * Controller per le operazioni amministrative sulle fasce orarie.
 * Tutti gli endpoint richiedono il ruolo 'ROLE_RECEPTIONIST'.
 */
@RestController
@RequestMapping("/api/receptionist/slots")
@PreAuthorize("hasRole('RECEPTIONIST')")
public class FasciaOrariaController {

    @Autowired
    private FasciaOrariaRepository fasciaOrariaRepository;

    /**
     * Crea una nuova fascia oraria.
     * @param request DTO con i dettagli della nuova fascia.
     * @return La fascia oraria creata.
     */
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

    /**
     * Restituisce la lista di tutte le fasce orarie esistenti.
     * @return Lista di tutte le fasce orarie.
     */
    @GetMapping
    public ResponseEntity<List<FasciaOraria>> getAllFasceOrarie() {
        return ResponseEntity.ok(fasciaOrariaRepository.findAll());
    }

    /**
     * Elimina una fascia oraria.
     * @param id L'ID della fascia da eliminare.
     * @return Un messaggio di conferma.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFasciaOraria(@PathVariable String id) {
        fasciaOrariaRepository.deleteById(id);
        return ResponseEntity.ok("Fascia oraria eliminata con successo.");
    }
}