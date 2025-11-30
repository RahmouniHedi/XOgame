package com.example.xogame;

import android.content.Context;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoriqueManager {
    private static final String HISTORY_FILENAME = "tournois_history.ser";

    public static class TournoiHistorique implements Serializable {
        private static final long serialVersionUID = 1L;

        private String date;
        private int scoreX;
        private int scoreO;
        private int partiesNulles;
        private int nombreTotalParties;
        private String vainqueur;
        private String nomJoueurX;
        private String nomJoueurO;

        public TournoiHistorique(int scoreX, int scoreO, int partiesNulles,
                                 int nombreTotalParties, String vainqueur,
                                 String nomJoueurX, String nomJoueurO) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);
            this.date = sdf.format(new Date());
            this.scoreX = scoreX;
            this.scoreO = scoreO;
            this.partiesNulles = partiesNulles;
            this.nombreTotalParties = nombreTotalParties;
            this.vainqueur = vainqueur;
            this.nomJoueurX = nomJoueurX;
            this.nomJoueurO = nomJoueurO;
        }

        public String getDate() { return date; }
        public int getScoreX() { return scoreX; }
        public int getScoreO() { return scoreO; }
        public int getPartiesNulles() { return partiesNulles; }
        public int getNombreTotalParties() { return nombreTotalParties; }
        public String getVainqueur() { return vainqueur; }
        public String getNomJoueurX() { return nomJoueurX; }
        public String getNomJoueurO() { return nomJoueurO; }

        @Override
        public String toString() {
            return "📅 " + date + "\n" +
                    "🎮 " + nombreTotalParties + " parties\n" +
                    "Joueurs : " + nomJoueurX + " (X) vs " + nomJoueurO + " (O)\n" +
                    "🔴 X: " + scoreX + " | 🔵 O: " + scoreO + " | ⚪ Nulles: " + partiesNulles + "\n" +
                    "🏆 " + vainqueur;
        }
    }

    public static void ajouterTournoi(Context context, int scoreX, int scoreO,
                                      int partiesNulles, int total, String vainqueur,
                                      String nomJoueurX, String nomJoueurO) {
        List<TournoiHistorique> historique = getHistorique(context);
        historique.add(0, new TournoiHistorique(scoreX, scoreO, partiesNulles, total, vainqueur, nomJoueurX, nomJoueurO));

        // Garder seulement les 20 derniers tournois
        if (historique.size() > 20) {
            historique = historique.subList(0, 20);
        }

        sauvegarderHistorique(context, historique);
    }

    public static List<TournoiHistorique> getHistorique(Context context) {
        try {
            FileInputStream fis = context.openFileInput(HISTORY_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            List<TournoiHistorique> historique = (List<TournoiHistorique>) ois.readObject();
            ois.close();
            fis.close();
            return historique;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void sauvegarderHistorique(Context context, List<TournoiHistorique> historique) {
        try {
            FileOutputStream fos = context.openFileOutput(HISTORY_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(historique);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}