package com.example.a2048_app;

import android.content.Context;
import android.util.TypedValue;

public class TileTheme {

    // Retourne la couleur de fond d'une tuile selon sa valeur et le thème actif
    public static int getBackgroundColor(Context context, int value) {
        int attr = getBackgroundAttr(value);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    // Retourne la couleur du texte d'une tuile selon sa valeur et le thème actif
    public static int getTextColor(Context context, int value) {
        int attr = value <= 4 ? R.attr.tileTextDark : R.attr.tileTextLight;
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    // Associe chaque valeur à son attribut de couleur
    private static int getBackgroundAttr(int value) {
        switch (value) {
            case 4:    return R.attr.tile4Color;
            case 8:    return R.attr.tile8Color;
            case 16:   return R.attr.tile16Color;
            case 32:   return R.attr.tile32Color;
            case 64:   return R.attr.tile64Color;
            case 128:  return R.attr.tile128Color;
            case 256:  return R.attr.tile256Color;
            case 512:  return R.attr.tile512Color;
            case 1024: return R.attr.tile1024Color;
            case 2048: return R.attr.tile2048Color;
            default:   return R.attr.tile2Color; // tuile 2 ou vide
        }
    }
}
