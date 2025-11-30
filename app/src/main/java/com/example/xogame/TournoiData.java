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
    private final String nomJoueurX;
    private final String nomJoueurO;


    public TournoiData(int scoreX, int scoreO, int partiesNulles, int nombreTotalParties, String vainqueur,
                       String nomJoueurX, String nomJoueurO) {
        this.scoreX = scoreX;
        this.scoreO = scoreO;
        this.partiesNulles = partiesNulles;
        this.nombreTotalParties = nombreTotalParties;
        this.vainqueur = vainqueur;
        this.nomJoueurX = nomJoueurX;
        this.nomJoueurO = nomJoueurO;
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

    public String getNomJoueurX() { return nomJoueurX; }
    public String getNomJoueurO() { return nomJoueurO; }

    @Override
    @NonNull
    public String toString() {
        return "Joueurs : " + nomJoueurX + " (X) vs " + nomJoueurO + " (O)\n" +
                "Score X : " + scoreX + "\n" +
                "Score O : " + scoreO + "\n" +
                "Parties nulles : " + partiesNulles + "\n" +
                "Total parties : " + nombreTotalParties + "\n" +
                "Vainqueur : " + vainqueur;
    }
}