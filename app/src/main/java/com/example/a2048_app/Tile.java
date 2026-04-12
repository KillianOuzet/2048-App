package com.example.a2048_app;

import java.util.Random;

/**
 * Représente une tuile individuelle sur la grille du 2048.
 * Cette classe gère sa propre valeur et la logique de fusion avec une autre tuile.
 */
public class Tile {
    private int value;

    /**
     * Constructeur d'une nouvelle tuile.
     * Applique une règle de probabilité pour le tirage initial :
     * - 70% de chances d'obtenir une tuile "2"
     * - 30% de chances d'obtenir une tuile "4"
     */
    public Tile() {
        Random random = new Random();
        // Un tirage sur 100 permet de définir précisément le ratio d'apparition
        this.value = random.nextInt(100) < 70 ? 2 : 4;
    }

    public int getValue() {
        return value;
    }

    /**
     * Logique de fusion de deux tuiles.
     * Si les valeurs sont identiques, la tuile actuelle double sa valeur.
     * * @param tile La tuile entrante qui va fusionner avec la tuile actuelle.
     *
     * @return La nouvelle valeur de la tuile (utilisée pour mettre à jour le score global),
     * ou 0 si la fusion est impossible.
     */
    public int merge(Tile tile) {
        if (tile.getValue() == this.value) {
            this.value *= 2;
            return this.value;
        }
        return 0;
    }
}