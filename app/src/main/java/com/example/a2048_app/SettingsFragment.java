package com.example.a2048_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

/**
 * Fragment gérant les réglages de l'application.
 * Il permet de modifier le thème visuel, d'activer/désactiver les sons et les animations,
 * et de régler les volumes sonores en temps réel.
 */
public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // --- Initialisation des composants UI ---
        MaterialButtonToggleGroup themeToggleGroup = view.findViewById(R.id.themeToggleGroup);

        SwitchMaterial switchAnimationsTuiles = view.findViewById(R.id.switch_animation_tuiles);
        SwitchMaterial switchMusiqueDeFond = view.findViewById(R.id.switch_musique_de_fond);
        SwitchMaterial switchSonsDesMouvements = view.findViewById(R.id.switch_sons_des_mouvements);
        SwitchMaterial switchSonsDeFusion = view.findViewById(R.id.switch_sons_de_fusion);

        Slider sliderVolumeMusique = view.findViewById(R.id.slider_volume_musique);
        TextView textVolumeMusique = view.findViewById(R.id.text_volume_musique);

        Slider sliderVolumeEffets = view.findViewById(R.id.slider_volume_effets);
        TextView textVolumeEffets = view.findViewById(R.id.text_volume_effets);

        // --- Récupération des réglages sauvegardés ---
        SharedPreferences prefs = requireActivity().getSharedPreferences("2048_settings", Context.MODE_PRIVATE);

        int themeActuel = prefs.getInt("themeChoisi", 0);
        int animationsTuiles = prefs.getInt("animationsTuiles", 1);
        int musiqueDeFond = prefs.getInt("musiqueDeFond", 1);
        int sonsDesMouvements = prefs.getInt("sonsDesMouvements", 1);
        int sonsDeFusion = prefs.getInt("sonsDeFusion", 1);
        int volumeMusique = prefs.getInt("volumeMusique", 100);
        int volumeEffets = prefs.getInt("volumeEffets", 100);

        // --- Application des états sauvegardés aux composants ---

        // Sélection du bouton de thème
        if (themeActuel == 0) themeToggleGroup.check(R.id.btnThemeLight);
        else if (themeActuel == 1) themeToggleGroup.check(R.id.btnThemeDark);
        else if (themeActuel == 2) themeToggleGroup.check(R.id.btnThemeColor);

        // État des interrupteurs
        switchAnimationsTuiles.setChecked(animationsTuiles != 0);
        switchMusiqueDeFond.setChecked(musiqueDeFond != 0);
        switchSonsDesMouvements.setChecked(sonsDesMouvements != 0);
        switchSonsDeFusion.setChecked(sonsDeFusion != 0);

        // Valeurs des sliders et affichage textuel des pourcentages
        sliderVolumeMusique.setValue(volumeMusique);
        textVolumeMusique.setText(String.format(Locale.getDefault(), "%d %%", (int) sliderVolumeMusique.getValue()));

        sliderVolumeEffets.setValue(volumeEffets);
        textVolumeEffets.setText(String.format(Locale.getDefault(), "%d %%", (int) sliderVolumeEffets.getValue()));

        // --- Écouteurs d'événements (Listeners) ---

        // Gestion des animations
        switchAnimationsTuiles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putInt("animationsTuiles", isChecked ? 1 : 0).apply();
        });

        // Gestion de la musique de fond
        switchMusiqueDeFond.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putInt("musiqueDeFond", isChecked ? 1 : 0).apply();

            if (isChecked) {
                MusicManager.getInstance().demarrerMusique(requireContext());
                float volumeActuel = sliderVolumeMusique.getValue() / 100f;
                MusicManager.getInstance().setVolume(volumeActuel);
            } else {
                MusicManager.getInstance().arreterTout();
            }
        });

        // Gestion des sons (mouvements et fusions)
        switchSonsDesMouvements.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putInt("sonsDesMouvements", isChecked ? 1 : 0).apply();
        });

        switchSonsDeFusion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putInt("sonsDeFusion", isChecked ? 1 : 0).apply();
        });

        // Contrôle du volume de la musique via le Slider
        sliderVolumeMusique.addOnChangeListener((slider, v, b) -> {
            prefs.edit().putInt("volumeMusique", (int) slider.getValue()).apply();
            textVolumeMusique.setText(String.format(Locale.getDefault(), "%d %%", (int) slider.getValue()));

            // On répercute immédiatement le changement sur le MusicManager (ExoPlayer)
            float volumeExoPlayer = slider.getValue() / 100f;
            MusicManager.getInstance().setVolume(volumeExoPlayer);
        });

        // Contrôle du volume des effets sonores
        sliderVolumeEffets.addOnChangeListener((slider, v, b) -> {
            prefs.edit().putInt("volumeEffets", (int) slider.getValue()).apply();
            textVolumeEffets.setText(String.format(Locale.getDefault(), "%d %%", (int) slider.getValue()));
        });

        /**
         * Gestion du changement de thème.
         * Lorsque l'utilisateur sélectionne un nouveau thème, on sauvegarde l'ID
         * et on redémarre l'activité principale pour appliquer les styles XML.
         */
        themeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                int nouveauTheme = 0;
                if (checkedId == R.id.btnThemeDark) nouveauTheme = 1;
                else if (checkedId == R.id.btnThemeColor) nouveauTheme = 2;

                if (nouveauTheme != themeActuel) {
                    prefs.edit().putInt("themeChoisi", nouveauTheme).apply();
                    // Redémarre l'activité pour appliquer le nouveau thème (nécessaire sur Android)
                    requireActivity().recreate();
                }
            }
        });

        return view;
    }
}