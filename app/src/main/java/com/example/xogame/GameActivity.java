package com.example.xogame;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";
    private static final int WIN_ANIMATION_DELAY_MS = 1100; // delay before showing result popup so animation can complete

    private Button[] buttons = new Button[9];
    private TextView tvPartieNum, tvScoreX, tvScoreO, tvNulles, tvTour;
    private TextView tvNomJoueurX, tvNomJoueurO;
    private String[][] board = new String[3][3];
    private String currentPlayer = "X";
    private String premierJoueur = "X";
    private String nomJoueurX = "Joueur X";
    private String nomJoueurO = "Joueur O";
    private int scoreX = 0, scoreO = 0, partiesNulles = 0;
    private int partieActuelle = 1;
    private int nbPartiesTotal;
    private String symboleJoueur;
    private boolean gameActive = true;
    private SoundManager soundManager;
    private int[] winningLine = null;
    private static final String FILENAME = "tournoi_data.ser";

    // single UI handler to avoid creating many Handler instances
    private Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        uiHandler = new Handler(getMainLooper());

        try {
            symboleJoueur = getIntent().getStringExtra("symboleJoueur");
            nbPartiesTotal = getIntent().getIntExtra("nbParties", 5);
            nomJoueurX = getIntent().getStringExtra("nomJoueurX");
            nomJoueurO = getIntent().getStringExtra("nomJoueurO");

            if (nomJoueurX == null || nomJoueurX.isEmpty()) nomJoueurX = "Joueur X";
            if (nomJoueurO == null || nomJoueurO.isEmpty()) nomJoueurO = "Joueur O";

            // Validate symboleJoueur
            if (symboleJoueur == null || (!symboleJoueur.equals("X") && !symboleJoueur.equals("O"))) {
                Log.w(TAG, "symboleJoueur absent ou invalide, valeur par défaut 'X' utilisée");
                symboleJoueur = "X";
            }

            // Initialiser le gestionnaire de sons (protected call)
            soundManager = SoundManager.getInstance(this);

            // Le symbole choisi commence la première partie
            premierJoueur = symboleJoueur;
            currentPlayer = premierJoueur;

            initializeViews();
            initializeGame();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation de l'activité", e);
            Toast.makeText(this, "Erreur lors du démarrage du jeu", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            tvPartieNum = findViewById(R.id.tvPartieNum);
            tvScoreX = findViewById(R.id.tvScoreX);
            tvScoreO = findViewById(R.id.tvScoreO);
            tvNulles = findViewById(R.id.tvNulles);
            tvTour = findViewById(R.id.tvTour);
            tvNomJoueurX = findViewById(R.id.tvNomJoueurX);
            tvNomJoueurO = findViewById(R.id.tvNomJoueurO);

            // Afficher les noms des joueurs (protect against null views)
            if (tvNomJoueurX != null) tvNomJoueurX.setText(nomJoueurX);
            if (tvNomJoueurO != null) tvNomJoueurO.setText(nomJoueurO);

            buttons[0] = findViewById(R.id.btn0);
            buttons[1] = findViewById(R.id.btn1);
            buttons[2] = findViewById(R.id.btn2);
            buttons[3] = findViewById(R.id.btn3);
            buttons[4] = findViewById(R.id.btn4);
            buttons[5] = findViewById(R.id.btn5);
            buttons[6] = findViewById(R.id.btn6);
            buttons[7] = findViewById(R.id.btn7);
            buttons[8] = findViewById(R.id.btn8);

            for (int i = 0; i < 9; i++) {
                final int index = i;
                if (buttons[i] != null) {
                    buttons[i].setOnClickListener(v -> onCellClick(index));
                } else {
                    Log.w(TAG, "Bouton index " + i + " est null (vue manquante)");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation des vues", e);
            Toast.makeText(this, "Erreur d'interface", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeGame() {
        // Start or continue the tournament; always play the configured number of parties
        gameActive = true;
        winningLine = null;

        // Déterminer qui commence cette partie
        if (partieActuelle % 2 == 1) {
            currentPlayer = premierJoueur;
        } else {
            currentPlayer = premierJoueur.equals("X") ? "O" : "X";
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = "";
            }
        }
        for (Button btn : buttons) {
            if (btn == null) continue;
            btn.setText("");
            btn.setEnabled(true);
            btn.setBackgroundResource(R.drawable.cell_background);
            btn.setAlpha(1.0f);
        }
        updateUI();
    }

    private void updateUI() {
        try {
            if (tvPartieNum != null) tvPartieNum.setText("Partie " + partieActuelle + " / " + nbPartiesTotal);
            if (tvScoreX != null) tvScoreX.setText(String.valueOf(scoreX));
            if (tvScoreO != null) tvScoreO.setText(String.valueOf(scoreO));
            if (tvNulles != null) tvNulles.setText(String.valueOf(partiesNulles));

            // Afficher le nom du joueur actuel
            String nomJoueurActuel = currentPlayer != null && currentPlayer.equals("X") ? nomJoueurX : nomJoueurO;
            if (tvTour != null) tvTour.setText("Tour de " + nomJoueurActuel);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la mise à jour de l'UI", e);
        }
    }

    private void onCellClick(int index) {
        try {
            if (!gameActive) return;

            if (index < 0 || index >= 9) {
                Log.w(TAG, "Index de cellule hors limites: " + index);
                return;
            }

            int row = index / 3;
            int col = index % 3;

            if (board[row][col].isEmpty()) {
                // Jouer le son de clic (defensive)
                try {
                    if (soundManager != null) soundManager.playClickSound();
                } catch (Exception e) {
                    Log.w(TAG, "Impossible de jouer le son de clic", e);
                }

                board[row][col] = currentPlayer;
                if (buttons[index] != null) {
                    buttons[index].setText(currentPlayer);
                    buttons[index].setEnabled(false);

                    // Colorer le texte selon le joueur
                    if (currentPlayer != null && currentPlayer.equals("X")) {
                        buttons[index].setTextColor(Color.parseColor("#EC4899"));
                    } else {
                        buttons[index].setTextColor(Color.parseColor("#3B82F6"));
                    }
                }

                if (checkWinner()) {
                    gameActive = false;
                    try { if (soundManager != null) soundManager.playWinSound(); } catch (Exception e) { Log.w(TAG, "Erreur playWinSound", e); }
                    animateWinningLine();

                    if (currentPlayer != null && currentPlayer.equals("X")) {
                        scoreX++;
                    } else {
                        scoreO++;
                    }
                    updateUI();

                    String nomGagnant = currentPlayer != null && currentPlayer.equals("X") ? nomJoueurX : nomJoueurO;

                    // Delay showing the popup so the winning-line animation can play fully
                    uiHandler.postDelayed(() -> showGameResultPopup(nomGagnant + " a gagné cette partie ! 🎉"), WIN_ANIMATION_DELAY_MS);

                } else if (isBoardFull()) {
                    gameActive = false;
                    try { if (soundManager != null) soundManager.playDrawSound(); } catch (Exception e) { Log.w(TAG, "Erreur playDrawSound", e); }

                    partiesNulles++;
                    updateUI();
                    uiHandler.postDelayed(() -> showGameResultPopup("Partie nulle ! ⚖️"), 500);
                } else {
                    currentPlayer = currentPlayer != null && currentPlayer.equals("X") ? "O" : "X";
                    updateUI();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du clic sur une cellule", e);
            Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkWinner() {
        try {
            // Vérifier les lignes
            for (int i = 0; i < 3; i++) {
                if (!board[i][0].isEmpty() &&
                        board[i][0].equals(board[i][1]) &&
                        board[i][1].equals(board[i][2])) {
                    winningLine = new int[]{i * 3, i * 3 + 1, i * 3 + 2};
                    return true;
                }
            }

            // Vérifier les colonnes
            for (int i = 0; i < 3; i++) {
                if (!board[0][i].isEmpty() &&
                        board[0][i].equals(board[1][i]) &&
                        board[1][i].equals(board[2][i])) {
                    winningLine = new int[]{i, i + 3, i + 6};
                    return true;
                }
            }

            // Vérifier les diagonales
            if (!board[0][0].isEmpty() &&
                    board[0][0].equals(board[1][1]) &&
                    board[1][1].equals(board[2][2])) {
                winningLine = new int[]{0, 4, 8};
                return true;
            }

            if (!board[0][2].isEmpty() &&
                    board[0][2].equals(board[1][1]) &&
                    board[1][1].equals(board[2][0])) {
                winningLine = new int[]{2, 4, 6};
                return true;
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la vérification du gagnant", e);
            return false;
        }
    }

    private void animateWinningLine() {
        if (winningLine == null) return;

        // Animation de pulsation sur la ligne gagnante
        for (int index : winningLine) {
            if (index < 0 || index >= buttons.length) continue;
            Button btn = buttons[index];
            if (btn == null) continue;

            // Animation de scale (agrandissement)
            try {
                btn.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(300)
                        .withEndAction(() -> {
                            btn.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(300)
                                    .start();
                        })
                        .start();
            } catch (Exception e) {
                Log.w(TAG, "Erreur d'animation scale sur le bouton " + index, e);
            }

            // Animation de clignotement (use uiHandler)
            final Button finalBtn = btn;
            String player = currentPlayer != null ? currentPlayer : "X";
            uiHandler.postDelayed(() -> {
                try {
                    if (player.equals("X")) {
                        finalBtn.setBackgroundColor(Color.parseColor("#FCE7F3"));
                    } else {
                        finalBtn.setBackgroundColor(Color.parseColor("#DBEAFE"));
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Erreur lors du changement de couleur (1)", e);
                }
            }, 300);

            uiHandler.postDelayed(() -> {
                try { finalBtn.setBackgroundResource(R.drawable.cell_background); } catch (Exception e) {
                    Log.w(TAG, "Erreur lors du changement de fond (2)", e);
                }
            }, 600);

            uiHandler.postDelayed(() -> {
                try {
                    if (player.equals("X")) {
                        finalBtn.setBackgroundColor(Color.parseColor("#FCE7F3"));
                    } else {
                        finalBtn.setBackgroundColor(Color.parseColor("#DBEAFE"));
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Erreur lors du changement de couleur (3)", e);
                }
            }, 900);
        }

        // Assombrir les cellules non-gagnantes
        for (int i = 0; i < 9; i++) {
            boolean isWinning = false;
            for (int winIndex : winningLine) {
                if (i == winIndex) {
                    isWinning = true;
                    break;
                }
            }
            if (!isWinning && buttons[i] != null) {
                try { buttons[i].setAlpha(0.3f); } catch (Exception e) { Log.w(TAG, "Erreur lors de l'assombrissement d'une cellule", e); }
            }
        }
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showGameResultPopup(String message) {
        try {
            TextView titleView = new TextView(this);
            titleView.setText("Résultat");
            titleView.setTextSize(22);
            titleView.setTextColor(Color.parseColor("#6366F1"));
            titleView.setPadding(40, 40, 40, 20);
            titleView.setGravity(Gravity.CENTER);
            titleView.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView messageView = new TextView(this);
            messageView.setText(message);
            messageView.setTextSize(18);
            messageView.setPadding(40, 20, 40, 40);
            messageView.setGravity(Gravity.CENTER);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(titleView);
            layout.addView(messageView);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(layout)
                    .setCancelable(false)
                    .create();

            dialog.show();

            uiHandler.postDelayed(() -> {
                try {
                    dialog.dismiss();

                    // Continue to next game until total parties are exhausted
                    if (partieActuelle < nbPartiesTotal) {
                        partieActuelle++;
                        initializeGame();
                    } else {
                        showTournamentResult();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur lors de la fermeture du popup de résultat", e);
                }
            }, 1500);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'affichage du popup de résultat", e);
            Toast.makeText(this, "Impossible d'afficher le résultat", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTournamentResult() {
        try {
            if (soundManager != null) soundManager.playTournamentWinSound();

            String vainqueur;
            String nomVainqueur;
            String colorCode;

            if (scoreX > scoreO) {
                vainqueur = "Joueur X";
                nomVainqueur = nomJoueurX;
                colorCode = "#EC4899";
            } else if (scoreO > scoreX) {
                vainqueur = "Joueur O";
                nomVainqueur = nomJoueurO;
                colorCode = "#3B82F6";
            } else {
                vainqueur = "Égalité";
                nomVainqueur = "Égalité";
                colorCode = "#10B981";
            }

            TextView titleView = new TextView(this);
            titleView.setText("TOURNOI TERMINÉ🏆");
            titleView.setTextSize(24);
            titleView.setTextColor(Color.parseColor(colorCode));
            titleView.setPadding(40, 40, 40, 20);
            titleView.setGravity(Gravity.CENTER);
            titleView.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView resultView = new TextView(this);
            // Show result as e.g. "Joueur X qui gagne" or "Joueur O qui gagne"
            String winnerText;
            if (vainqueur.equals("Égalité")) {
                winnerText = "Égalité";
            } else {
                // Use the internal X/O label as requested
                if (scoreX > scoreO) {
                    winnerText = nomVainqueur+" qui gagne";
                } else if (scoreO > scoreX) {
                    winnerText =nomVainqueur+" qui gagne";
                } else {
                    // Fallback to the provided winner name
                    winnerText = nomVainqueur + " qui gagne";
                }
            }
            String resultText = winnerText + "\n\n" +
                    " Total : " + nbPartiesTotal + " parties"+"\n" +
                    "🔴 " + nomJoueurX + " : " + scoreX + "\n" +
                    "🔵 " + nomJoueurO + " : " + scoreO + "\n" +
                    "⚪ Parties nulles : " + partiesNulles + "\n" ;

            resultView.setText(resultText);
            resultView.setTextSize(16);
            resultView.setPadding(40, 20, 40, 40);
            resultView.setGravity(Gravity.CENTER);
            resultView.setLineSpacing(10, 1);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(titleView);
            layout.addView(resultView);

            new AlertDialog.Builder(this)
                    .setView(layout)
                    .setPositiveButton("💾 Sauvegarder", (dialog, which) -> {
                        sauvegarderTournoi(nomVainqueur);
                        finish();
                    })
                    .setNegativeButton("🏠 Accueil", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'affichage du résultat du tournoi", e);
            Toast.makeText(this, "Erreur lors de l'affichage du tournoi", Toast.LENGTH_SHORT).show();
        }
    }

    private void sauvegarderTournoi(String nomVainqueur) {
        try {
            String vainqueurInfo = nomVainqueur.equals("Égalité") ?
                    "Égalité" : nomVainqueur;
            // Include player usernames when saving tournament data
            TournoiData data = new TournoiData(scoreX, scoreO, partiesNulles,
                    nbPartiesTotal, vainqueurInfo, nomJoueurX, nomJoueurO);
            FileOutputStream fos = openFileOutput(FILENAME, MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
            fos.close();

            HistoriqueManager.ajouterTournoi(this, scoreX, scoreO,
                    partiesNulles, nbPartiesTotal, vainqueurInfo, nomJoueurX, nomJoueurO);

            Toast.makeText(this, "Tournoi sauvegardé", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde du tournoi", e);
            Toast.makeText(this, "Impossible de sauvegarder le tournoi", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove any pending callbacks to avoid leaking the Activity or running UI code after finish
        try {
            if (uiHandler != null) uiHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            Log.w(TAG, "Erreur lors du nettoyage des handlers", e);
        }
    }

}
