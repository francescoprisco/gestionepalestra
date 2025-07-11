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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired private PrenotazioneRepository prenotazioneRepository;
    @Autowired private FasciaOrariaRepository fasciaOrariaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private NotificationService notificationService;

    // ... metodi esistenti ...

    // --- NUOVO METODO PER OTTENERE LE PRENOTAZIONI DI UN CLIENTE ---
    public List<Prenotazione> getMyBookings(String clienteEmail) {
        // 1. Trova il cliente usando la sua email
        return clienteRepository.findByEmail(clienteEmail)
                // 2. Se il cliente esiste, usa il suo ID per trovare le prenotazioni
                .map(cliente -> prenotazioneRepository.findByCliente_Id(cliente.getId()))
                // 3. Se il cliente non esiste, restituisci una lista vuota
                .orElse(Collections.emptyList());
    }
    
    public List<FasciaOrariaDisponibilita> getAvailabilityForDay(LocalDate data) {
        List<FasciaOraria> tutteLeFasce = fasciaOrariaRepository.findAll();
        return tutteLeFasce.stream().map(fascia -> {
            long postiOccupati = prenotazioneRepository.countByDataAndFasciaOraria_Id(data, fascia.getId());
            return new FasciaOrariaDisponibilita(fascia, postiOccupati);
        }).collect(Collectors.toList());
    }

    public Prenotazione createBooking(String clienteEmail, String fasciaOrariaId, LocalDate data) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        Cliente cliente = findClienteByEmail(clienteEmail);
        
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
    
    public Prenotazione createBookingForRegisteredClient(String clienteId, String fasciaOrariaId, LocalDate data) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new RuntimeException("Nessun cliente trovato con l'ID fornito: " + clienteId));
        if (prenotazioneRepository.existsByCliente_IdAndDataAndFasciaOraria_Id(cliente.getId(), data, fasciaOrariaId)) {
            throw new RuntimeException("Questo cliente ha già una prenotazione per questa fascia oraria.");
        }
        checkAvailability(fascia, data);
        Prenotazione p = new Prenotazione();
        p.setCliente(cliente);
        p.setFasciaOraria(fascia);
        p.setData(data);
        return prenotazioneRepository.save(p);
    }

    public Prenotazione createBookingForOccasional(String nomeCliente, String fasciaOrariaId, LocalDate data) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        checkAvailability(fascia, data);
        
        Prenotazione p = new Prenotazione();
        p.setNomeClienteOccasionale(nomeCliente);
        p.setFasciaOraria(fascia);
        p.setData(data);
        return prenotazioneRepository.save(p);
    }
    
    public Prenotazione modifyBooking(String bookingId, String clienteEmail, String nuovaFasciaId, LocalDate nuovaData) {
        Prenotazione vecchiaPrenotazione = findBookingById(bookingId);
        Cliente cliente = findClienteByEmail(clienteEmail);
        if (!vecchiaPrenotazione.getCliente().getId().equals(cliente.getId())) {
            throw new SecurityException("Non autorizzato a modificare questa prenotazione.");
        }
        FasciaOraria nuovaFascia = findFasciaOrariaById(nuovaFasciaId);
        checkAvailability(nuovaFascia, nuovaData);
        vecchiaPrenotazione.setFasciaOraria(nuovaFascia);
        vecchiaPrenotazione.setData(nuovaData);
        return prenotazioneRepository.save(vecchiaPrenotazione);
    }
    
    public void cancelBooking(String bookingId, String clienteEmail) {
        Prenotazione prenotazione = findBookingById(bookingId);
        Cliente cliente = findClienteByEmail(clienteEmail);
        if (!prenotazione.getCliente().getId().equals(cliente.getId())) {
             throw new SecurityException("Non autorizzato a cancellare questa prenotazione.");
        }
        prenotazioneRepository.delete(prenotazione);
    }
    
    public void cancelAnyBooking(String bookingId) {
        prenotazioneRepository.deleteById(bookingId);
    }
    
    public void disableSlot(String fasciaOrariaId) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        fascia.setActive(false);
        fasciaOrariaRepository.save(fascia);
        notificationService.notifyUsersOfCancellation(fasciaOrariaId, null); 
    }

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