package com.example.xogame;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnJouer, btnPrincipe, btnRetrouvScores, btnHistorique;
    private RadioButton radioX, radioO;
    private Spinner spinnerNbParties;
    private EditText etNomJoueurX, etNomJoueurO;
    private SoundManager soundManager;
    private static final String FILENAME = "tournoi_data.ser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundManager = SoundManager.getInstance(this);

        initializeViews();
        setupSpinner();
        setupListeners();
    }

    private void initializeViews() {
        btnJouer = findViewById(R.id.btnJouer);
        btnPrincipe = findViewById(R.id.btnPrincipe);
        btnRetrouvScores = findViewById(R.id.btnRetrouvScores);
        btnHistorique = findViewById(R.id.btnHistorique);
        radioX = findViewById(R.id.radioX);
        radioO = findViewById(R.id.radioO);
        spinnerNbParties = findViewById(R.id.spinnerNbParties);
        etNomJoueurX = findViewById(R.id.etNomJoueurX);
        etNomJoueurO = findViewById(R.id.etNomJoueurO);
    }

    private void setupSpinner() {
        String[] options = {"5 parties", "10 parties", "15 parties"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNbParties.setAdapter(adapter);
    }

    private void setupListeners() {
        btnJouer.setOnClickListener(v -> {
            soundManager.playClickSound();
            startGame();
        });
        btnPrincipe.setOnClickListener(v -> {
            soundManager.playClickSound();
            showPrincipe();
        });
        btnRetrouvScores.setOnClickListener(v -> {
            soundManager.playClickSound();
            afficherScoresDernierTournoi();
        });
        btnHistorique.setOnClickListener(v -> {
            soundManager.playClickSound();
            afficherHistorique();
        });
    }

    private void startGame() {
        String symboleJoueur = radioX.isChecked() ? "X" : "O";
        String selectedParties = spinnerNbParties.getSelectedItem().toString();
        int nbParties = Integer.parseInt(selectedParties.split(" ")[0]);

        // Récupérer les noms des joueurs
        String nomJoueurX = etNomJoueurX.getText().toString().trim();
        String nomJoueurO = etNomJoueurO.getText().toString().trim();

        // Valider les noms
        if (nomJoueurX.isEmpty()) {
            nomJoueurX = "Joueur X";
        }
        if (nomJoueurO.isEmpty()) {
            nomJoueurO = "Joueur O";
        }

        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra("symboleJoueur", symboleJoueur);
        intent.putExtra("nbParties", nbParties);
        intent.putExtra("nomJoueurX", nomJoueurX);
        intent.putExtra("nomJoueurO", nomJoueurO);
        startActivity(intent);
    }

    private void showPrincipe() {
        String principe = "🎮 Le jeu X-O se joue sur une grille de 3 × 3 cases.\n\n" +
                "👥 Deux joueurs s'affrontent : l'un choisit X et l'autre O.\n\n" +
                "🔄 À tour de rôle, chaque joueur place son symbole dans une case vide.\n\n" +
                "🏆 Une partie se termine lorsqu'un joueur aligne trois symboles identiques " +
                "sur une ligne, une colonne ou une diagonale.\n\n" +
                "⚖️ Si toutes les cases sont remplies sans vainqueur : partie nulle.\n\n" +
                "🎯 Dans le tournoi, plusieurs parties se succèdent et les scores sont " +
                "cumulés automatiquement.";

        showStyledDialog("Principe du jeu", principe, "#6366F1");
    }

    private void afficherScoresDernierTournoi() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            TournoiData data = (TournoiData) ois.readObject();
            ois.close();
            fis.close();

            String message = "🏆 RÉSULTATS\n\n" +
                    "🔴 Score X : " + data.getScoreX() + "\n" +
                    "🔵 Score O : " + data.getScoreO() + "\n" +
                    "⚪ Parties nulles : " + data.getPartiesNulles() + "\n" +
                    "📊 Total parties : " + data.getNombreTotalParties() + "\n\n" +
                    "👑 Vainqueur : " + data.getVainqueur();

            showStyledDialog("Dernier Tournoi", message, "#10B981");

        } catch (Exception e) {
            Toast.makeText(this, "Aucun tournoi sauvegardé", Toast.LENGTH_SHORT).show();
        }
    }

    private void afficherHistorique() {
        List<HistoriqueManager.TournoiHistorique> historique =
                HistoriqueManager.getHistorique(this);

        if (historique.isEmpty()) {
            Toast.makeText(this, "Aucun historique disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        for (int i = 0; i < historique.size(); i++) {
            HistoriqueManager.TournoiHistorique tournoi = historique.get(i);

            TextView tv = new TextView(this);
            tv.setText((i + 1) + ". " + tournoi.toString());
            tv.setTextSize(14);
            tv.setPadding(20, 20, 20, 20);
            tv.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 20);
            tv.setLayoutParams(params);

            layout.addView(tv);
        }

        scrollView.addView(layout);

        new AlertDialog.Builder(this)
                .setTitle("📊 Historique des Tournois")
                .setView(scrollView)
                .setPositiveButton("Fermer", null)
                .show();
    }

    private void showStyledDialog(String title, String message, String color) {
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(22);
        titleView.setTextColor(Color.parseColor(color));
        titleView.setPadding(40, 40, 40, 20);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setTextSize(16);
        messageView.setPadding(40, 20, 40, 40);
        messageView.setLineSpacing(8, 1);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(titleView);
        layout.addView(messageView);

        new AlertDialog.Builder(this)
                .setView(layout)
                .setPositiveButton("OK", null)
                .show();
    }
}