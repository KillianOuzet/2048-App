package com.example.a2048_app;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Gestionnaire d'événements tactiles personnalisé pour détecter les glissements (Swipes).
 * Cette classe simplifie l'utilisation des événements de toucher complexes en les
 * traduisant en méthodes compréhensibles : onSwipeRight, onSwipeLeft, etc.
 */
public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context ctx) {
        // Le GestureDetector analyse les MotionEvents bruts pour identifier des patterns (clics, glissements, etc.)
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // On délègue l'analyse de l'événement au gestureDetector
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * Classe interne étendant SimpleOnGestureListener pour filtrer uniquement
     * les gestes qui nous intéressent (le Fling/Glissement).
     */
    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        // Constantes définissant la sensibilité du swipe pour éviter les déclenchements involontaires
        private static final int SWIPE_THRESHOLD = 100;         // Distance minimale en pixels
        private static final int SWIPE_VELOCITY_THRESHOLD = 100; // Vitesse minimale du geste

        @Override
        public boolean onDown(MotionEvent e) {
            // Obligatoire pour consommer l'événement et permettre la détection du mouvement qui suit
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                // Calcul de la distance parcourue entre le point de contact (e1) et le point de levée (e2)
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();

                // On compare les valeurs absolues pour savoir si le mouvement est plutôt horizontal ou vertical
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    // Analyse HORIZONTALE
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                } else {
                    // Analyse VERTICALE
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    // Méthodes à surcharger dans les Activités/Fragments pour définir l'action métier
    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }
}