package com.example.a2048_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;

/**
 * Gestionnaire des effets sonores (SFX) du jeu.
 * Contrairement à la musique de fond, on utilise SoundPool car il est optimisé
 * pour jouer des sons très courts avec une latence quasi nulle, ce qui est
 * indispensable pour le ressenti (feedback) lors d'un glissement de tuile.
 */
public class SoundManager {

    private SoundPool soundPool;
    private final int moveSoundId;
    private final int mergeSoundId;
    private boolean isLoaded = false;
    private final Context context;

    public SoundManager(Context context) {
        this.context = context;

        // Configuration des attributs audio pour indiquer au système que
        // ces sons appartiennent à la catégorie "Jeu" (Game).
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

        // Initialisation de SoundPool permettant de jouer jusqu'à 4 sons simultanément.
        soundPool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(audioAttributes).build();

        // Listener pour s'assurer que les fichiers audio sont bien chargés en mémoire
        // avant de tenter de les jouer, évitant ainsi des erreurs silencieuses.
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                isLoaded = true;
            }
        });

        // Chargement des fichiers audio depuis le dossier res/raw
        moveSoundId = soundPool.load(context, R.raw.whoosh, 1);
        mergeSoundId = soundPool.load(context, R.raw.pop, 1);
    }

    /**
     * Joue le son de balayage (whoosh) si l'option est activée dans les réglages.
     */
    public void playMoveSound() {
        SharedPreferences prefs = context.getSharedPreferences("2048_settings", Context.MODE_PRIVATE);
        int soundEnabled = prefs.getInt("sonsDesMouvements", 1);
        int volumeEffets = prefs.getInt("volumeEffets", 100);

        if (isLoaded && soundEnabled == 1) {
            // Conversion du volume (0-100) en valeur flottante (0.0-1.0) pour SoundPool
            float actualVolume = volumeEffets / 100f;
            soundPool.play(moveSoundId, actualVolume, actualVolume, 1, 0, 1f);
        }
    }

    /**
     * Joue le son de fusion (pop) si l'option est activée dans les réglages.
     */
    public void playMergeSound() {
        SharedPreferences prefs = context.getSharedPreferences("2048_settings", Context.MODE_PRIVATE);
        int soundEnabled = prefs.getInt("sonsDeFusion", 1);
        int volumeEffets = prefs.getInt("volumeEffets", 100);

        if (isLoaded && soundEnabled == 1) {
            float actualVolume = volumeEffets / 100f;
            soundPool.play(mergeSoundId, actualVolume, actualVolume, 1, 0, 1f);
        }
    }

    /**
     * Libère les ressources du SoundPool.
     * Indispensable pour éviter les fuites de mémoire (Memory Leaks)
     * lorsque la GameActivity est détruite.
     */
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}