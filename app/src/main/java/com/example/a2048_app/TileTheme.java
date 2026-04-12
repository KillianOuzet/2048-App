package com.example.a2048_app;

import android.content.Context;
import android.util.TypedValue;

/**
 * Gestionnaire utilitaire pour l'apparence des tuiles.
 * Cette classe permet de récupérer dynamiquement les couleurs de fond et de texte
 * en fonction de la valeur de la tuile et du thème actuellement appliqué à l'application.
 */
public class TileTheme {

    /**
     * Récupère la couleur de fond correspondant à la valeur d'une tuile.
     * Utilise resolveAttribute pour s'adapter automatiquement au thème (Clair, Sombre, Coloré).
     */
    public static int getBackgroundColor(Context context, int value) {
        int attr = getBackgroundAttr(value);
        TypedValue typedValue = new TypedValue();

        // On demande au thème actuel de nous donner la couleur associée à l'attribut
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    /**
     * Détermine la couleur du texte (chiffre) à afficher sur la tuile.
     * La règle classique du 2048 est d'avoir un texte sombre pour les petites valeurs (2, 4)
     * et un texte clair/blanc pour les valeurs plus élevées afin de garder un bon contraste.
     */
    public static int getTextColor(Context context, int value) {
        // Sélection de l'attribut selon la lisibilité
        int attr = value <= 4 ? R.attr.tileTextDark : R.attr.tileTextLight;
        TypedValue typedValue = new TypedValue();

        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    /**
     * Mappe chaque valeur de tuile à un attribut de couleur défini dans "attrs.xml".
     * Cela permet de changer toutes les couleurs du jeu simplement en modifiant le fichier XML des thèmes.
     */
    private static int getBackgroundAttr(int value) {
        switch (value) {
            case 4:
                return R.attr.tile4Color;
            case 8:
                return R.attr.tile8Color;
            case 16:
                return R.attr.tile16Color;
            case 32:
                return R.attr.tile32Color;
            case 64:
                return R.attr.tile64Color;
            case 128:
                return R.attr.tile128Color;
            case 256:
                return R.attr.tile256Color;
            case 512:
                return R.attr.tile512Color;
            case 1024:
                return R.attr.tile1024Color;
            case 2048:
                return R.attr.tile2048Color;
            default:
                // Pour la valeur 2 ou les cases vides (0)
                return R.attr.tile2Color;
        }
    }
}