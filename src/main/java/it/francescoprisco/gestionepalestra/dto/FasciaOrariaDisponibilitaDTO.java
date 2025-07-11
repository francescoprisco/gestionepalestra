package it.francescoprisco.gestionepalestra.dto;

import it.francescoprisco.gestionepalestra.model.FasciaOraria;
import lombok.Data;

// DTO per comunicare la disponibilità di una fascia oraria
@Data
public class FasciaOrariaDisponibilitaDTO {
    private FasciaOraria fasciaOraria;
    private long postiOccupati;
    private boolean disponibile;

    public FasciaOrariaDisponibilitaDTO(FasciaOraria fasciaOraria, long postiOccupati) {
        this.fasciaOraria = fasciaOraria;
        this.postiOccupati = postiOccupati;
        this.disponibile = postiOccupati < fasciaOraria.getPostiTotali();
    }
}