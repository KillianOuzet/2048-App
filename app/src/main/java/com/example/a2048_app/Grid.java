package com.example.a2048_app;

import java.util.ArrayList;
import java.util.Random;

/**
 * Moteur logique et mathématique du jeu 2048.
 * Cette classe est "Pure Java" (aucune dépendance à Android).
 * Elle gère l'état du plateau, les mouvements, les fusions et les conditions de victoire/défaite.
 */
public class Grid {

    private final int size;
    private final Tile[][] grid;

    private int score;
    private int nbMove;

    public Grid(int size) {
        this.size = size;
        this.grid = new Tile[size][size];
        this.score = 0;
        this.nbMove = 0;

        // Au lancement d'une nouvelle partie, on fait apparaître 2 tuiles de départ.
        generateTile();
        generateTile();
    }

    // --- Getters ---

    public int getSize() {
        return size;
    }

    public Tile[][] getGrid() {
        return grid;
    }

    public int getScore() {
        return score;
    }

    public int getNbMove() {
        return nbMove;
    }

    // --- Algorithmes de déplacement ---

    /**
     * Glissement vers la droite.
     * L'ordre de la boucle (col = size - 2 vers 0) est fondamental :
     * on doit toujours déplacer en priorité les tuiles les plus proches du bord visé
     * pour "libérer la place" à celles qui suivent derrière.
     */
    public boolean rightSlide() {
        boolean moved = false;
        for (int row = 0; row < size; row++) {
            for (int col = size - 2; col >= 0; col--) {
                if (grid[row][col] != null) {
                    moved |= moveTile(row, col, 0, 1);
                }
            }
        }
        if (moved) triggerAfterMove();
        return moved;
    }

    public boolean leftSlide() {
        boolean moved = false;
        for (int row = 0; row < size; row++) {
            // On parcourt de la gauche vers la droite
            for (int col = 1; col < size; col++) {
                if (grid[row][col] != null) {
                    moved |= moveTile(row, col, 0, -1);
                }
            }
        }
        if (moved) triggerAfterMove();
        return moved;
    }

    public boolean upSlide() {
        boolean moved = false;
        for (int col = 0; col < size; col++) {
            // On parcourt du haut vers le bas
            for (int row = 1; row < size; row++) {
                if (grid[row][col] != null) {
                    moved |= moveTile(row, col, -1, 0);
                }
            }
        }
        if (moved) triggerAfterMove();
        return moved;
    }

    public boolean downSlide() {
        boolean moved = false;
        for (int col = 0; col < size; col++) {
            // On parcourt du bas vers le haut
            for (int row = size - 2; row >= 0; row--) {
                if (grid[row][col] != null) {
                    moved |= moveTile(row, col, 1, 0);
                }
            }
        }
        if (moved) triggerAfterMove();
        return moved;
    }

    /**
     * Méthode utilitaire appelée après un glissement réussi.
     * Incrémente le compteur de coups et génère une nouvelle tuile.
     */
    private void triggerAfterMove() {
        generateTile();
        nbMove++;
    }

    /**
     * Calcule la trajectoire d'une tuile individuelle dans une direction donnée (dRow, dCol).
     * La tuile glisse tant que la case suivante est vide, ou fusionne si la case contient
     * une tuile de valeur identique.
     */
    private boolean moveTile(int row, int col, int dRow, int dCol) {
        boolean moved = false;
        int newRow = row + dRow;
        int newCol = col + dCol;

        // Tant qu'on ne sort pas des limites du plateau de jeu
        while (isInBounds(newRow, newCol)) {

            if (grid[newRow][newCol] == null) {
                // Cas 1 : La case suivante est vide, on avance.
                grid[newRow][newCol] = grid[row][col];
                grid[row][col] = null;

                row = newRow;
                col = newCol;
                newRow += dRow;
                newCol += dCol;
                moved = true;

            } else if (grid[newRow][newCol].getValue() == grid[row][col].getValue()) {
                // Cas 2 : Collision avec une tuile de même valeur -> FUSION.
                // Le score de la fusion est ajouté au score global.
                score += grid[newRow][newCol].merge(grid[row][col]);
                grid[row][col] = null;
                moved = true;
                break; // On stoppe le mouvement de cette tuile pour ce tour.

            } else {
                // Cas 3 : Collision avec une tuile de valeur différente.
                break; // Blocage, on arrête d'avancer.
            }
        }
        return moved;
    }

    /**
     * Fait apparaître une nouvelle tuile (2 ou 4) sur une case vide aléatoire.
     */
    public void generateTile() {
        // On dresse la liste de toutes les coordonnées [row, col] actuellement vides
        ArrayList<int[]> emptyCells = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid[row][col] == null) {
                    emptyCells.add(new int[]{row, col});
                }
            }
        }

        // Sécurité : si la grille est pleine, on ne génère rien
        if (emptyCells.isEmpty()) return;

        // Tirage au sort d'une case parmi celles disponibles
        Random random = new Random();
        int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
        grid[cell[0]][cell[1]] = new Tile();
    }

    // --- Conditions de fin de partie ---

    public boolean isWon() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid[row][col] != null && grid[row][col].getValue() == 2048) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Un "Game Over" se produit uniquement si DEUX conditions sont réunies :
     * 1. La grille est totalement remplie (aucun null).
     * 2. Aucune fusion n'est possible (aucune tuile adjacente n'a la même valeur).
     */
    public boolean isGameOver() {
        // Étape 1 : Vérifier s'il reste de la place
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid[row][col] == null) return false;
            }
        }

        // Étape 2 : Vérifier les adjacences horizontales et verticales
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int val = grid[row][col].getValue();
                // Vérification avec la case du dessous
                if (row + 1 < size && grid[row + 1][col].getValue() == val) return false;
                // Vérification avec la case de droite
                if (col + 1 < size && grid[row][col + 1].getValue() == val) return false;
            }
        }

        return true; // Bloqué !
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    /**
     * Parcours la grille pour trouver la tuile de plus haute valeur.
     * Utilisé pour la sauvegarde des statistiques (records et badges).
     */
    public int getMaxTile() {
        int max = 0;
        for (int r = 0; r < this.getSize(); r++) {
            for (int c = 0; c < this.getSize(); c++) {
                Tile t = this.getGrid()[r][c];
                if (t != null && t.getValue() > max) {
                    max = t.getValue();
                }
            }
        }
        return max;
    }
}