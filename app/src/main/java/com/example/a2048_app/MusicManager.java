package com.example.a2048_app;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

public class MusicManager implements DefaultLifecycleObserver {
    private static MusicManager instance;
    private ExoPlayer player;

    private Context appContext;

    // Constructeur privé (Singleton)
    private MusicManager() {
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void init(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void demarrerMusique(Context context) {
        if (player == null) {
            // Utiliser l'ApplicationContext évite les fuites de mémoire (memory leaks)
            player = new ExoPlayer.Builder(context.getApplicationContext()).build();

            // Chemin vers votre fichier dans res/raw
            String uri = "android.resource://" + context.getPackageName() + "/" + R.raw.starlight;
            MediaItem mediaItem = MediaItem.fromUri(uri);

            player.setMediaItem(mediaItem);
            player.setRepeatMode(Player.REPEAT_MODE_ALL); // Boucle infinie
            player.prepare();
        }

        // Lance la musique si elle n'est pas déjà en cours
        if (!player.isPlaying()) {
            player.play();
        }
    }

    public void mettreEnPause() {
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    public void arreterTout() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    public void setVolume(float volume) {
        if (player != null) {
            player.setVolume(volume);
        }
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        // L'application revient au premier plan (Foreground)
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences("2048_settings", Context.MODE_PRIVATE);
            boolean musiqueActive = prefs.getInt("musiqueDeFond", 1) == 1;

            if (musiqueActive) {
                demarrerMusique(appContext);

                // On remet le bon volume
                int volumePref = prefs.getInt("volumeMusique", 100);
                setVolume(volumePref / 100f);
            }
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        // L'application passe en arrière-plan (Background)
        mettreEnPause();
    }
}
