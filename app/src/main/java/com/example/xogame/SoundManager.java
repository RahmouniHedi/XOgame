package com.example.xogame;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundManager {
    private static SoundManager instance;
    private SoundPool soundPool;
    private int clickSound;
    private int winSound;
    private int drawSound;
    private int tournamentWinSound;
    private boolean soundEnabled = true;

    private SoundManager(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // Charger les sons
        clickSound = soundPool.load(context, R.raw.click, 1);
        winSound = soundPool.load(context, R.raw.win, 1);
        drawSound = soundPool.load(context, R.raw.draw, 1);
        tournamentWinSound = soundPool.load(context, R.raw.tournament_win, 1);
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context.getApplicationContext());
        }
        return instance;
    }

    public void playClickSound() {
        if (soundEnabled && clickSound != 0) {
            soundPool.play(clickSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void playWinSound() {
        if (soundEnabled && winSound != 0) {
            soundPool.play(winSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void playDrawSound() {
        if (soundEnabled && drawSound != 0) {
            soundPool.play(drawSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void playTournamentWinSound() {
        if (soundEnabled && tournamentWinSound != 0) {
            soundPool.play(tournamentWinSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}