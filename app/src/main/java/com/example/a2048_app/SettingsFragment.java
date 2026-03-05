package com.example.a2048_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        MaterialButtonToggleGroup themeToggleGroup = view.findViewById(R.id.themeToggleGroup);
        SwitchMaterial switchAnimationsTuiles = view.findViewById(R.id.switch_animation_tuiles);

        SharedPreferences prefs = requireActivity().getSharedPreferences("2048_settings", Context.MODE_PRIVATE);
        int themeActuel = prefs.getInt("themeChoisi", 0);
        int animationsTuiles = prefs.getInt("animationsTuiles", 1);

        if (themeActuel == 0) themeToggleGroup.check(R.id.btnThemeLight);
        else if (themeActuel == 1) themeToggleGroup.check(R.id.btnThemeDark);
        else if (themeActuel == 2) themeToggleGroup.check(R.id.btnThemeColor);

        switchAnimationsTuiles.setChecked(animationsTuiles != 0);

        switchAnimationsTuiles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int nouvellesAnimationsTuiles = isChecked ? 1 : 0;

            prefs.edit().putInt("animationsTuiles", nouvellesAnimationsTuiles).apply();
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