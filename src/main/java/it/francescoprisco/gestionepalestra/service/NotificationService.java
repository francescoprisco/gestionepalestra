package it.francescoprisco.gestionepalestra.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
class NotificationService {
    public void notifyUsersOfCancellation(String fasciaOrariaId, LocalDate data) {
        System.out.println("NOTIFICA: Avvio invio notifiche di cancellazione per fascia " + fasciaOrariaId);
    }
}