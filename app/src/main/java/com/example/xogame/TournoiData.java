package com.example.xogame;

import java.io.Serializable;
import androidx.annotation.NonNull;

public class TournoiData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int scoreX;
    private final int scoreO;
    private final int partiesNulles;
    private final int nombreTotalParties;
    private final String vainqueur;


    public TournoiData(int scoreX, int scoreO, int partiesNulles, int nombreTotalParties, String vainqueur) {
        this.scoreX = scoreX;
        this.scoreO = scoreO;
        this.partiesNulles = partiesNulles;
        this.nombreTotalParties = nombreTotalParties;
        this.vainqueur = vainqueur;
    }

    public int getScoreX() {
        return scoreX;
    }

    public int getScoreO() {
        return scoreO;
    }

    public int getPartiesNulles() {
        return partiesNulles;
    }

    public int getNombreTotalParties() {
        return nombreTotalParties;
    }

    @NonNull
    public String getVainqueur() {
        return vainqueur;
    }

    @Override
    @NonNull
    public String toString() {
        return "Score X : " + scoreX + "\n" +
                "Score O : " + scoreO + "\n" +
                "Parties nulles : " + partiesNulles + "\n" +
                "Total parties : " + nombreTotalParties + "\n" +
                "Vainqueur : " + vainqueur;
    }
}