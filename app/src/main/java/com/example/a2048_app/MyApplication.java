package com.example.a2048_app;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

/**
 * Point d'entrée global de l'application.
 * Cette classe est instanciée avant n'importe quelle activité ou service.
 * Elle permet d'initialiser des composants globaux et de surveiller
 * le cycle de vie de l'application entière.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialisation du Singleton MusicManager avec le contexte global de l'application.
        // Cela garantit que le manager a accès aux ressources dès le démarrage.
        MusicManager.getInstance().init(this);

        /**
         * ProcessLifecycleOwner permet de surveiller le cycle de vie du PROCESSUS de l'application.
         * En ajoutant le MusicManager comme observateur ici, la musique réagira aux événements globaux :
         * - onStart : L'utilisateur ouvre l'app ou revient dessus -> La musique reprend.
         * - onStop  : L'utilisateur quitte l'app ou la met en arrière-plan -> La musique se coupe.
         * Cela évite de devoir gérer la musique manuellement dans chaque activité.
         */
        ProcessLifecycleOwner.get().getLifecycle().addObserver(MusicManager.getInstance());
    }
}