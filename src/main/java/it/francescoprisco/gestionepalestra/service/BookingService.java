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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired private PrenotazioneRepository prenotazioneRepository;
    @Autowired private FasciaOrariaRepository fasciaOrariaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private NotificationService notificationService;

    // Definiamo il fuso orario di riferimento per tutte le operazioni
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Rome");

    /**
     * LOGICA CORRETTA: Valida se un cliente ha una prenotazione per la giornata di oggi.
     */
    public boolean validateNfcBooking(String nfcId) {
        return clienteRepository.findByNfcId(nfcId)
            .map(cliente -> {
                LocalDate oggi = LocalDate.now(ZONE_ID);
                Instant inizioGiorno = oggi.atStartOfDay(ZONE_ID).toInstant();
                Instant fineGiorno = oggi.plusDays(1).atStartOfDay(ZONE_ID).toInstant();

                return prenotazioneRepository.existsByCliente_IdAndDataBetween(cliente.getId(), inizioGiorno, fineGiorno);
            })
            .orElse(false);
    }
    
    /**
     * LOGICA CORRETTA: Crea una prenotazione.
     */
    public Prenotazione createBooking(String clienteEmail, String fasciaOrariaId, LocalDate data) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        Cliente cliente = findClienteByEmail(clienteEmail);
        
        checkAvailability(fascia, data);

        Prenotazione p = new Prenotazione();
        p.setCliente(cliente);
        p.setFasciaOraria(fascia);
        // Salva la data come un istante preciso (inizio del giorno nel nostro fuso orario)
        p.setData(data.atStartOfDay(ZONE_ID).toInstant());
        return prenotazioneRepository.save(p);
    }
    
    public List<FasciaOrariaDisponibilita> getAvailabilityForDay(LocalDate data) {
        List<FasciaOraria> tutteLeFasce = fasciaOrariaRepository.findAll();
        Instant inizioGiorno = data.atStartOfDay(ZONE_ID).toInstant();
        Instant fineGiorno = data.plusDays(1).atStartOfDay(ZONE_ID).toInstant();
        
        return tutteLeFasce.stream().map(fascia -> {
            long postiOccupati = prenotazioneRepository.countByDataBetweenAndFasciaOraria_Id(inizioGiorno, fineGiorno, fascia.getId());
            return new FasciaOrariaDisponibilita(fascia, postiOccupati);
        }).collect(Collectors.toList());
    }

    private void checkAvailability(FasciaOraria fascia, LocalDate data) {
        if (!fascia.isActive()) {
            throw new RuntimeException("Questa fascia oraria non è attiva.");
        }
        Instant inizioGiorno = data.atStartOfDay(ZONE_ID).toInstant();
        Instant fineGiorno = data.plusDays(1).atStartOfDay(ZONE_ID).toInstant();

        long postiOccupati = prenotazioneRepository.countByDataBetweenAndFasciaOraria_Id(inizioGiorno, fineGiorno, fascia.getId());
        if (postiOccupati >= fascia.getPostiTotali()) {
            throw new RuntimeException("Posti esauriti per la fascia oraria selezionata.");
        }
    }

    // --- Metodi rimanenti (con logica invariata ma utile per il contesto) ---

    public List<Prenotazione> getMyBookings(String clienteEmail) {
        return clienteRepository.findByEmail(clienteEmail)
            .map(cliente -> prenotazioneRepository.findByCliente_Id(cliente.getId()))
            .orElse(Collections.emptyList());
    }
    
    public Prenotazione createBookingForRegisteredClient(String clienteId, String fasciaOrariaId, LocalDate data) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new RuntimeException("Nessun cliente trovato con l'ID fornito: " + clienteId));

        checkAvailability(fascia, data);

        Prenotazione p = new Prenotazione();
        p.setCliente(cliente);
        p.setFasciaOraria(fascia);
        p.setData(data.atStartOfDay(ZONE_ID).toInstant());
        return prenotazioneRepository.save(p);
    }

    public Prenotazione createBookingForOccasional(String nomeCliente, String fasciaOrariaId, LocalDate data) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        checkAvailability(fascia, data);
        
        Prenotazione p = new Prenotazione();
        p.setNomeClienteOccasionale(nomeCliente);
        p.setFasciaOraria(fascia);
        p.setData(data.atStartOfDay(ZONE_ID).toInstant());
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
        vecchiaPrenotazione.setData(nuovaData.atStartOfDay(ZONE_ID).toInstant());
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

    public List<Prenotazione> getBookingsForDay(LocalDate data) {
        // Calcoliamo l'inizio e la fine della giornata nel nostro fuso orario
        Instant inizioGiorno = data.atStartOfDay(ZoneId.of("Europe/Rome")).toInstant();
        Instant fineGiorno = data.plusDays(1).atStartOfDay(ZoneId.of("Europe/Rome")).toInstant();

        // Usiamo il nuovo metodo del repository per cercare nell'intervallo
        return prenotazioneRepository.findByDataBetween(inizioGiorno, fineGiorno);
    }
}