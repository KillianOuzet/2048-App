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

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        MaterialButtonToggleGroup themeToggleGroup = view.findViewById(R.id.themeToggleGroup);

        SwitchMaterial switchAnimationsTuiles = view.findViewById(R.id.switch_animation_tuiles);
        SwitchMaterial switchMusiqueDeFond = view.findViewById(R.id.switch_musique_de_fond);
        SwitchMaterial switchSonsDesMouvements = view.findViewById(R.id.switch_sons_des_mouvements);
        SwitchMaterial switchSonsDeFusion = view.findViewById(R.id.switch_sons_de_fusion);

        Slider sliderVolumeMusique = view.findViewById(R.id.slider_volume_musique);
        TextView textVolumeMusique = view.findViewById(R.id.text_volume_musique);

        Slider sliderVolumeEffets = view.findViewById(R.id.slider_volume_effets);
        TextView textVolumeEffets = view.findViewById(R.id.text_volume_effets);

        SharedPreferences prefs = requireActivity().getSharedPreferences("2048_settings", Context.MODE_PRIVATE);
        int themeActuel = prefs.getInt("themeChoisi", 0);

        int animationsTuiles = prefs.getInt("animationsTuiles", 1);
        int musiqueDeFond = prefs.getInt("musiqueDeFond", 1);
        int sonsDesMouvements = prefs.getInt("sonsDesMouvements", 1);
        int sonsDeFusion = prefs.getInt("sonsDeFusion", 1);

        int volumeMusique = prefs.getInt("volumeMusique", 100);
        int volumeEffets = prefs.getInt("volumeEffets", 100);

        if (themeActuel == 0) themeToggleGroup.check(R.id.btnThemeLight);
        else if (themeActuel == 1) themeToggleGroup.check(R.id.btnThemeDark);
        else if (themeActuel == 2) themeToggleGroup.check(R.id.btnThemeColor);

        switchAnimationsTuiles.setChecked(animationsTuiles != 0);
        switchMusiqueDeFond.setChecked(musiqueDeFond != 0);
        switchSonsDesMouvements.setChecked(sonsDesMouvements != 0);
        switchSonsDeFusion.setChecked(sonsDeFusion != 0);

        sliderVolumeMusique.setValue(volumeMusique);
        textVolumeMusique.setText(String.format(Locale.getDefault(), "%d %%", (int) sliderVolumeMusique.getValue()));

        sliderVolumeEffets.setValue(volumeEffets);
        textVolumeEffets.setText(String.format(Locale.getDefault(), "%d %%", (int) sliderVolumeEffets.getValue()));

        switchAnimationsTuiles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int nouvellesAnimationsTuiles = isChecked ? 1 : 0;

            prefs.edit().putInt("animationsTuiles", nouvellesAnimationsTuiles).apply();
        });

        switchMusiqueDeFond.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int nouvellesMusiqueDeFond = isChecked ? 1 : 0;

            prefs.edit().putInt("musiqueDeFond", nouvellesMusiqueDeFond).apply();

            if (isChecked) {
                MusicManager.getInstance().demarrerMusique(requireContext());
                // On s'assure d'appliquer le bon volume dès la reprise
                float volumeActuel = sliderVolumeMusique.getValue() / 100f;
                MusicManager.getInstance().setVolume(volumeActuel);
            } else {
                MusicManager.getInstance().arreterTout();
            }
        });

        switchSonsDesMouvements.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int nouvellesSonsDesMouvements = isChecked ? 1 : 0;

            prefs.edit().putInt("sonsDesMouvements", nouvellesSonsDesMouvements).apply();
        });

        switchSonsDeFusion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int nouvellesSonDeFusion = isChecked ? 1 : 0;

            prefs.edit().putInt("sonsDeFusion", nouvellesSonDeFusion).apply();
        });

        sliderVolumeMusique.addOnChangeListener((slider, v, b) -> {
            prefs.edit().putInt("volumeMusique", (int) slider.getValue()).apply();
            textVolumeMusique.setText(String.format(Locale.getDefault(), "%d %%", (int) slider.getValue()));
            float volumeExoPlayer = slider.getValue() / 100f;
            MusicManager.getInstance().setVolume(volumeExoPlayer);
        });

        sliderVolumeEffets.addOnChangeListener((slider, v, b) -> {
            prefs.edit().putInt("volumeEffets", (int) slider.getValue()).apply();
            textVolumeEffets.setText(String.format(Locale.getDefault(), "%d %%", (int) slider.getValue()));
        });

        themeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                int nouveauTheme = 0;

                if (checkedId == R.id.btnThemeDark) {
                    nouveauTheme = 1;
                } else if (checkedId == R.id.btnThemeColor) {
                    nouveauTheme = 2;
                }

                if (nouveauTheme != themeActuel) {
                    prefs.edit().putInt("themeChoisi", nouveauTheme).apply();
                    requireActivity().recreate();
                }
            }
        });

        return view;
    }
}