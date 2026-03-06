package com.example.a2048_app;

import android.content.Context;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

public class MusicManager {
    private static MusicManager instance;
    private ExoPlayer player;

    // Constructeur privé (Singleton)
    private MusicManager() {
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void demarrerMusique(Context context) {
        if (player == null) {
            // Utiliser l'ApplicationContext évite les fuites de mémoire (memory leaks)
            player = new ExoPlayer.Builder(context.getApplicationContext()).build();

            // Chemin vers votre fichier dans res/raw
            String uri = "android.resource://" + context.getPackageName() + "/" + R.raw.musique_fond;
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
}
