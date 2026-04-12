package com.example.a2048_app;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

/**
 * Gestionnaire de musique de fond utilisant Google ExoPlayer.
 * Implémente DefaultLifecycleObserver pour mettre en pause ou reprendre la musique
 * automatiquement lorsque l'utilisateur quitte ou revient dans l'application.
 */
public class MusicManager implements DefaultLifecycleObserver {

    private static MusicManager instance;
    private ExoPlayer player;
    private Context appContext;

    // Constructeur privé pour empêcher l'instanciation directe (Pattern Singleton)
    private MusicManager() {
    }

    /**
     * Récupère l'instance unique du gestionnaire de musique.
     */
    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    /**
     * Initialise le contexte global pour permettre au Manager d'accéder
     * aux ressources et préférences même sans activité active.
     */
    public void init(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /**
     * Configure et lance la lecture de la musique de fond.
     */
    public void demarrerMusique(Context context) {
        if (player == null) {
            // Initialisation de ExoPlayer (plus performant et flexible que MediaPlayer)
            player = new ExoPlayer.Builder(context.getApplicationContext()).build();

            // Construction de l'URI vers le fichier raw (starlight.mp3)
            String uri = "android.resource://" + context.getPackageName() + "/" + R.raw.starlight;
            MediaItem mediaItem = MediaItem.fromUri(uri);

            player.setMediaItem(mediaItem);
            player.setRepeatMode(Player.REPEAT_MODE_ALL); // Lecture en boucle infinie
            player.prepare();
        }

        if (!player.isPlaying()) {
            player.play();
        }
    }

    /**
     * Met la musique en pause sans libérer les ressources,
     * permettant une reprise instantanée.
     */
    public void mettreEnPause() {
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    /**
     * Arrête complètement la musique et libère le lecteur de la mémoire vive.
     */
    public void arreterTout() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    /**
     * Ajuste le volume du lecteur.
     *
     * @param volume Valeur flottante entre 0.0f (muet) et 1.0f (max).
     */
    public void setVolume(float volume) {
        if (player != null) {
            player.setVolume(volume);
        }
    }

    // =========================================================================================
    // CALLBACKS DE CYCLE DE VIE (Lifecycle Callbacks)
    // =========================================================================================

    /**
     * Déclenché automatiquement lorsque l'application passe au premier plan.
     */
    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences("2048_settings", Context.MODE_PRIVATE);
            boolean musiqueActive = prefs.getInt("musiqueDeFond", 1) == 1;

            if (musiqueActive) {
                demarrerMusique(appContext);

                // Application du volume sauvegardé dans les réglages
                int volumePref = prefs.getInt("volumeMusique", 100);
                setVolume(volumePref / 100f);
            }
        }
    }

    /**
     * Déclenché automatiquement lorsque l'application passe en arrière-plan.
     * Cela évite que la musique continue de jouer si l'utilisateur répond à un appel
     * ou change d'application.
     */
    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        mettreEnPause();
    }
}