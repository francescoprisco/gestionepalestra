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
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired private PrenotazioneRepository prenotazioneRepository;
    @Autowired private FasciaOrariaRepository fasciaOrariaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private NotificationService notificationService;

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Rome");

    /**
     * LOGICA FINALE: Valida l'accesso NFC con una finestra di tolleranza di
     * 15 minuti prima dell'inizio e 15 minuti dopo la fine della fascia oraria.
     */
    public boolean validateNfcBooking(String nfcId) {
        return clienteRepository.findByNfcId(nfcId)
            .map(cliente -> {
                LocalDate oggi = LocalDate.now(ZONE_ID);
                Instant inizioGiorno = oggi.atStartOfDay(ZONE_ID).toInstant();
                Instant fineGiorno = oggi.plusDays(1).atStartOfDay(ZONE_ID).toInstant();

                // 1. Trova tutte le prenotazioni del cliente per oggi
                List<Prenotazione> prenotazioniDiOggi = prenotazioneRepository.findByCliente_IdAndDataBetween(cliente.getId(), inizioGiorno, fineGiorno);

                if (prenotazioniDiOggi.isEmpty()) {
                    return false;
                }

                // 2. Controlla ogni prenotazione per vedere se l'orario attuale è valido
                Instant now = Instant.now();
                for (Prenotazione prenotazione : prenotazioniDiOggi) {
                    FasciaOraria fascia = prenotazione.getFasciaOraria();
                    LocalDate dataPrenotazione = LocalDate.ofInstant(prenotazione.getData(), ZONE_ID);
                    
                    // 3. Ricostruisce l'orario di inizio e fine della fascia oraria nel giorno corretto
                    Instant orarioInizioFascia = dataPrenotazione.atTime(fascia.getOraInizio()).atZone(ZONE_ID).toInstant();
                    Instant orarioFineFascia = dataPrenotazione.atTime(fascia.getOraFine()).atZone(ZONE_ID).toInstant();

                    // 4. Calcola la finestra di validità: 15 min prima dell'inizio e 15 min dopo la fine
                    Instant inizioFinestra = orarioInizioFascia.minus(15, ChronoUnit.MINUTES);
                    Instant fineFinestra = orarioFineFascia.plus(15, ChronoUnit.MINUTES);

                    // 5. Controlla se l'orario attuale è dentro la finestra
                    if (!now.isBefore(inizioFinestra) && now.isBefore(fineFinestra)) {
                        return true; // Accesso consentito!
                    }
                }

                return false; // Nessuna prenotazione trovata nella finestra di tolleranza
            })
            .orElse(false); // Cliente non trovato
    }

    // --- Metodi rimanenti (con la logica corretta per il fuso orario) ---
    
    public List<FasciaOrariaDisponibilita> getAvailabilityForDay(LocalDate data) {
        List<FasciaOraria> fasceAttive = fasciaOrariaRepository.findAll().stream()
                .filter(FasciaOraria::isActive)
                .collect(Collectors.toList());

        Instant inizioGiorno = data.atStartOfDay(ZONE_ID).toInstant();
        Instant fineGiorno = data.plusDays(1).atStartOfDay(ZONE_ID).toInstant();
        
        return fasceAttive.stream()
            .map(fascia -> {
                long postiOccupati = prenotazioneRepository.countByDataBetweenAndFasciaOraria_Id(inizioGiorno, fineGiorno, fascia.getId());
                return new FasciaOrariaDisponibilita(fascia, postiOccupati);
            })
            .filter(FasciaOrariaDisponibilita::isDisponibile)
            .collect(Collectors.toList());
    }
    
    public Prenotazione createBooking(String clienteEmail, String fasciaOrariaId, LocalDate data) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        Cliente cliente = findClienteByEmail(clienteEmail);
        checkAvailability(fascia, data);
        Prenotazione p = new Prenotazione();
        p.setCliente(cliente);
        p.setFasciaOraria(fascia);
        p.setData(data.atStartOfDay(ZONE_ID).toInstant());
        return prenotazioneRepository.save(p);
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
        Instant inizioGiorno = data.atStartOfDay(ZONE_ID).toInstant();
        Instant fineGiorno = data.plusDays(1).atStartOfDay(ZONE_ID).toInstant();
        return prenotazioneRepository.findByDataBetween(inizioGiorno, fineGiorno);
    }
}

@Service
class NotificationService {
    public void notifyUsersOfCancellation(String fasciaOrariaId, LocalDate data) {
        System.out.println("NOTIFICA: Avvio invio notifiche di cancellazione per fascia " + fasciaOrariaId);
    }
}