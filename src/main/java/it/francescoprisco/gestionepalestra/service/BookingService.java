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
/**
 * Service che contiene tutta la logica di business per la gestione delle prenotazioni.
 */
@Service
public class BookingService {

    @Autowired private PrenotazioneRepository prenotazioneRepository;
    @Autowired private FasciaOrariaRepository fasciaOrariaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private NotificationService notificationService;
    // Definiamo un fuso orario di riferimento per tutte le operazioni di data/ora.

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Rome");

    /**
     * Valida un accesso tramite NFC ID.
     * L'accesso è consentito in una finestra di tolleranza di 15 minuti prima dell'inizio
     * e 15 minuti dopo la fine della fascia oraria prenotata per il giorno corrente.
     * @param nfcId L'ID della tessera NFC del cliente.
     * @return true se l'accesso è consentito, false altrimenti.
     */
    public boolean validateNfcBooking(String nfcId) {
                // Cerca il cliente tramite NFC ID

        return clienteRepository.findByNfcId(nfcId)
            .map(cliente -> {
                // Calcola l'inizio e la fine della giornata di oggi nel fuso orario del server
                LocalDate oggi = LocalDate.now(ZONE_ID);
                Instant inizioGiorno = oggi.atStartOfDay(ZONE_ID).toInstant();
                Instant fineGiorno = oggi.plusDays(1).atStartOfDay(ZONE_ID).toInstant();

                // Trova tutte le prenotazioni del cliente per la giornata odierna
                List<Prenotazione> prenotazioniDiOggi = prenotazioneRepository.findByCliente_IdAndDataBetween(cliente.getId(), inizioGiorno, fineGiorno);

                if (prenotazioniDiOggi.isEmpty()) {
                    return false;
                }


                Instant now = Instant.now();
                // Controlla ogni prenotazione del giorno per vedere se l'orario attuale è valido
                for (Prenotazione prenotazione : prenotazioniDiOggi) {
                    FasciaOraria fascia = prenotazione.getFasciaOraria();
                    LocalDate dataPrenotazione = LocalDate.ofInstant(prenotazione.getData(), ZONE_ID);
                    
                    // Ricostruisce l'orario esatto di inizio e fine della fascia prenotata
                    Instant orarioInizioFascia = dataPrenotazione.atTime(fascia.getOraInizio()).atZone(ZONE_ID).toInstant();
                    Instant orarioFineFascia = dataPrenotazione.atTime(fascia.getOraFine()).atZone(ZONE_ID).toInstant();

                    // Calcola la finestra di validità con la tolleranza
                    Instant inizioFinestra = orarioInizioFascia.minus(15, ChronoUnit.MINUTES);
                    Instant fineFinestra = orarioFineFascia.plus(15, ChronoUnit.MINUTES);

                    // Controlla se l'orario attuale è all'interno della finestra di tolleranza
                    if (!now.isBefore(inizioFinestra) && now.isBefore(fineFinestra)) {
                        return true; // Accesso consentito!
                    }
                }

                return false; // Nessuna prenotazione trovata nella finestra di tolleranza
            })
            .orElse(false); // Cliente non trovato
    }

     /**
     * Restituisce la lista delle sole fasce orarie che hanno posti disponibili per una data specifica.
     * @param data La data per cui verificare la disponibilità.
     * @return Una lista di DTO contenenti le informazioni sulle fasce disponibili.
     */   
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

    /**
     * Crea una nuova prenotazione per un cliente registrato.
     * @param clienteEmail Email del cliente.
     * @param fasciaOrariaId ID della fascia oraria.
     * @param data Data della prenotazione.
     * @return La prenotazione creata.
     */
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
    /**
     * Trova le prenotazioni di un cliente specifico.
     * @param clienteEmail Email del cliente.
     * @return Lista delle sue prenotazioni.
     */
    public List<Prenotazione> getMyBookings(String clienteEmail) {
        return clienteRepository.findByEmail(clienteEmail)
            .map(cliente -> prenotazioneRepository.findByCliente_Id(cliente.getId()))
            .orElse(Collections.emptyList());
    }

    /**
     * Crea una prenotazione per un cliente registrato, eseguita da un receptionist.
     * @param clienteId ID del cliente.
     * @param fasciaOrariaId ID della fascia oraria.
     * @param data Data della prenotazione.
     * @return La prenotazione creata.
     */
    public Prenotazione createBookingForRegisteredClient(String clienteId, String fasciaOrariaId, LocalDate data) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new RuntimeException("Nessun cliente trovato con l'ID fornito: " + clienteId));
        
        // CORREZIONE: Aggiunto il controllo di disponibilità che mancava
        checkAvailability(fascia, data);

        // Controlla se il cliente ha già una prenotazione per quel giorno
        Instant inizioGiorno = data.atStartOfDay(ZONE_ID).toInstant();
        Instant fineGiorno = data.plusDays(1).atStartOfDay(ZONE_ID).toInstant();
        if (prenotazioneRepository.existsByCliente_IdAndDataBetween(cliente.getId(), inizioGiorno, fineGiorno)) {
            throw new RuntimeException("Questo cliente ha già una prenotazione per questa giornata.");
        }

        Prenotazione p = new Prenotazione();
        p.setCliente(cliente);
        p.setFasciaOraria(fascia);
        p.setData(data.atStartOfDay(ZONE_ID).toInstant());
        return prenotazioneRepository.save(p);
    }
    /**
     * Crea una prenotazione per un cliente occasionale, eseguita da un receptionist.
     * @param nomeCliente Nome del cliente occasionale.
     * @param fasciaOrariaId ID della fascia oraria.
     * @param data Data della prenotazione.
     * @return La prenotazione creata.
     */
    public Prenotazione createBookingForOccasional(String nomeCliente, String fasciaOrariaId, LocalDate data) {
        FasciaOraria fascia = findFasciaOrariaById(fasciaOrariaId);
        checkAvailability(fascia, data);
        Prenotazione p = new Prenotazione();
        p.setNomeClienteOccasionale(nomeCliente);
        p.setFasciaOraria(fascia);
        p.setData(data.atStartOfDay(ZONE_ID).toInstant());
        return prenotazioneRepository.save(p);
    }
    /**
     * Modifica una prenotazione esistente di un cliente.
     * @param bookingId ID della prenotazione da modificare.
     * @param clienteEmail Email del cliente che esegue la modifica.
     * @param nuovaFasciaId ID della nuova fascia oraria.
     * @param nuovaData Nuova data della prenotazione.
     * @return La prenotazione aggiornata.
     */
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
    /**
     * Cancella una prenotazione esistente di un cliente.
     * @param bookingId ID della prenotazione da cancellare.
     * @param clienteEmail Email del cliente che esegue la cancellazione.
     */
    public void cancelBooking(String bookingId, String clienteEmail) {
        Prenotazione prenotazione = findBookingById(bookingId);
        Cliente cliente = findClienteByEmail(clienteEmail);
        if (!prenotazione.getCliente().getId().equals(cliente.getId())) {
             throw new SecurityException("Non autorizzato a cancellare questa prenotazione.");
        }
        prenotazioneRepository.delete(prenotazione);
    }
    /**
     * Cancella una qualsiasi prenotazione. Metodo riservato al receptionist.
     * @param bookingId ID della prenotazione da cancellare.
     */
    public void cancelAnyBooking(String bookingId) {
        prenotazioneRepository.deleteById(bookingId);
    }
    /**
     * Disabilita una fascia oraria per le prenotazioni future.
     * @param fasciaOrariaId ID della fascia da disabilitare.
     */
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
    /**
     * Trova tutte le prenotazioni per un dato giorno.
     * @param data La data di interesse.
     * @return Lista delle prenotazioni.
     */
    public List<Prenotazione> getBookingsForDay(LocalDate data) {
        Instant inizioGiorno = data.atStartOfDay(ZONE_ID).toInstant();
        Instant fineGiorno = data.plusDays(1).atStartOfDay(ZONE_ID).toInstant();
        return prenotazioneRepository.findByDataBetween(inizioGiorno, fineGiorno);
    }
}