package com.example.a2048_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundManager {

    private SoundPool soundPool;
    private final int moveSoundId;
    private final int mergeSoundId; // <-- NOUVEAU
    private boolean isLoaded = false;
    private final Context context;

    public SoundManager(Context context) {
        this.context = context;

        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

        soundPool = new SoundPool.Builder().setMaxStreams(4) // J'ai augmenté à 4 pour éviter que les sons se coupent
                .setAudioAttributes(audioAttributes).build();

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                isLoaded = true;
            }
        });

        // Chargement des deux fichiers audio
        moveSoundId = soundPool.load(context, R.raw.whoosh, 1);
        mergeSoundId = soundPool.load(context, R.raw.pop, 1); // <-- NOUVEAU (Remplace "pop" par le nom de ton fichier)
    }

    public void playMoveSound() {
        SharedPreferences prefs = context.getSharedPreferences("2048_settings", Context.MODE_PRIVATE);
        int soundEnabled = prefs.getInt("sonsDesMouvements", 1);
        int volumeEffets = prefs.getInt("volumeEffets", 100);

        if (isLoaded && soundEnabled == 1) {
            float actualVolume = volumeEffets / 100f;
            soundPool.play(moveSoundId, actualVolume, actualVolume, 1, 0, 1f);
        }
    }

    // <-- NOUVELLE MÉTHODE -->
    public void playMergeSound() {
        SharedPreferences prefs = context.getSharedPreferences("2048_settings", Context.MODE_PRIVATE);
        int soundEnabled = prefs.getInt("sonsDeFusion", 1);
        int volumeEffets = prefs.getInt("volumeEffets", 100);

        if (isLoaded && soundEnabled == 1) {
            float actualVolume = volumeEffets / 100f;
            soundPool.play(mergeSoundId, actualVolume, actualVolume, 1, 0, 1f);
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}