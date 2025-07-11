package it.francescoprisco.gestionepalestra.service;

import it.francescoprisco.gestionepalestra.dto.FasciaOrariaDisponibilita;
import it.francescoprisco.gestionepalestra.model.Cliente;
import it.francescoprisco.gestionepalestra.model.FasciaOraria;
import it.francescoprisco.gestionepalestra.model.Prenotazione;
import it.francescoprisco.gestionepalestra.repository.ClienteRepository;
import it.francescoprisco.gestionepalestra.repository.FasciaOrariaRepository;
import it.francescoprisco.gestionepalestra.repository.PrenotazioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired private PrenotazioneRepository prenotazioneRepository;
    @Autowired private FasciaOrariaRepository fasciaOrariaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private NotificationService notificationService;

    // --- METODI PER DISPONIBILITÀ ---

    /**
     * Restituisce la lista di tutte le fasce orarie per un dato giorno
     * con l'indicazione dei posti occupati e della disponibilità.
     * Implementa: Visualizzazione in Tempo Reale della Disponibilità [cite: 152]
     */
    public List<FasciaOrariaDisponibilita> getAvailabilityForDay(LocalDate data) {
        List<FasciaOraria> tutteLeFasce = fasciaOrariaRepository.findAll();
        return tutteLeFasce.stream().map(fascia -> {
            long postiOccupati = prenotazioneRepository.countByDataAndFasciaOraria_Id(data, fascia.getId());
            return new FasciaOrariaDisponibilita(fascia, postiOccupati);
        }).collect(Collectors.toList());
    }

    // --- METODI PER PRENOTAZIONI (CLIENTE) ---

    /**
     * Crea una nuova prenotazione per il cliente loggato.
     * Implementa: Prenotazione turno [cite: 201]
     */
    public Prenotazione createBooking(String clienteEmail, String fasciaOrariaId, LocalDate data) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        Cliente cliente = findClienteByEmail(clienteEmail);
        
        // Regola: Impedisce prenotazioni multiple nella stessa fascia oraria [cite: 148]
        if (prenotazioneRepository.existsByCliente_IdAndDataAndFasciaOraria_Id(cliente.getId(), data, fasciaOrariaId)) {
            throw new RuntimeException("Hai già una prenotazione per questa fascia oraria.");
        }
        
        checkAvailability(fascia, data);
        
        Prenotazione p = new Prenotazione();
        p.setCliente(cliente);
        p.setFasciaOraria(fascia);
        p.setData(data);
        return prenotazioneRepository.save(p);
    }
    
    /**
     * Modifica una prenotazione esistente per un cliente.
     * Implementa: Modifica turno [cite: 202]
     */
    public Prenotazione modifyBooking(String bookingId, String clienteEmail, String nuovaFasciaId, LocalDate nuovaData) {
        Prenotazione vecchiaPrenotazione = findBookingById(bookingId);
        Cliente cliente = findClienteByEmail(clienteEmail);

        // Sicurezza: un cliente può modificare solo le proprie prenotazioni
        if (!vecchiaPrenotazione.getCliente().getId().equals(cliente.getId())) {
            throw new SecurityException("Non autorizzato a modificare questa prenotazione.");
        }

        FasciaOraria nuovaFascia = findFasciaOrariaById(nuovaFasciaId);
        checkAvailability(nuovaFascia, nuovaData);
        
        vecchiaPrenotazione.setFasciaOraria(nuovaFascia);
        vecchiaPrenotazione.setData(nuovaData);
        return prenotazioneRepository.save(vecchiaPrenotazione);
    }
    
    /**
     * Cancella una prenotazione per un cliente.
     * Implementa: Cancella prenotazione turno [cite: 203]
     */
    public void cancelBooking(String bookingId, String clienteEmail) {
        Prenotazione prenotazione = findBookingById(bookingId);
        Cliente cliente = findClienteByEmail(clienteEmail);

        // Sicurezza: un cliente può cancellare solo le proprie prenotazioni
        if (!prenotazione.getCliente().getId().equals(cliente.getId())) {
             throw new SecurityException("Non autorizzato a cancellare questa prenotazione.");
        }
        prenotazioneRepository.delete(prenotazione);
    }
    
    // --- METODI PER RECEPTIONIST ---
    
    /**
     * Crea prenotazione per un cliente occasionale.
     * Implementa: Prenotazione turno (Receptionist) [cite: 206]
     */
    public Prenotazione createBookingForOccasional(String nomeCliente, String fasciaOrariaId, LocalDate data) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        checkAvailability(fascia, data);
        
        Prenotazione p = new Prenotazione();
        p.setNomeClienteOccasionale(nomeCliente);
        p.setFasciaOraria(fascia);
        p.setData(data);
        return prenotazioneRepository.save(p);
    }
    
    /**
     * Cancella qualsiasi prenotazione (usato dal receptionist).
     * Implementa: Cancella prenotazione turno (Receptionist) [cite: 208]
     */
    public void cancelAnyBooking(String bookingId) {
        prenotazioneRepository.deleteById(bookingId);
    }
    
    /**
     * Disabilita le prenotazioni per una specifica fascia oraria.
     * Implementa: Disabilita prenotazione turno [cite: 209]
     */
    public void disableSlot(String fasciaOrariaId) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        fascia.setActive(false);
        fasciaOrariaRepository.save(fascia);
        // Notifica ai clienti che avevano già prenotato [cite: 198]
        notificationService.notifyUsersOfCancellation(fasciaOrariaId, null); 
    }

    // --- METODI DI SUPPORTO E VALIDAZIONE ---
    
    /**
     * Verifica la prenotazione tramite NFC.
     * Implementa: Validazione prenotazione [cite: 211]
     */
    public boolean validateNfcBooking(String nfcId) {
        return clienteRepository.findByNfcId(nfcId)
                .map(cliente -> prenotazioneRepository.existsByCliente_IdAndData(cliente.getId(), LocalDate.now()))
                .orElse(false);
    }
    
    private void checkAvailability(FasciaOraria fascia, LocalDate data) {
        if (!fascia.isActive()) {
            throw new RuntimeException("Questa fascia oraria non è attiva.");
        }
        long postiOccupati = prenotazioneRepository.countByDataAndFasciaOraria_Id(data, fascia.getId());
        if (postiOccupati >= fascia.getPostiTotali()) {
            // Se non ci sono posti, consiglia altre fasce [cite: 165]
            throw new RuntimeException("Posti esauriti per la fascia oraria selezionata.");
        }
    }
    
    private Cliente findClienteByEmail(String email) {
        return clienteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Cliente non trovato con email: " + email));
    }
    
    private FasciaOraria findFasciaOrariaById(String id) {
        return fasciaOrariaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fascia oraria non trovata con ID: " + id));
    }
    
    private Prenotazione findBookingById(String id) {
        return prenotazioneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prenotazione non trovata con ID: " + id));
    }
}

@Service
class NotificationService {
    public void notifyUsersOfCancellation(String fasciaOrariaId, LocalDate data) {
        System.out.println("NOTIFICA: Avvio invio notifiche di cancellazione per fascia " + fasciaOrariaId);
    }
}