package com.example.a2048_app;

import java.util.ArrayList;
import java.util.Random;

public class Grid {
    private final int size;
    private Tile[][] grid;
    private int score;

    public Grid(int size) {
        this.size = size;
        this.grid = new Tile[size][size];
        this.score = 0;

        generateTile();
        generateTile();
    }

    public int getSize() {
        return size;
    }

    public Tile[][] getGrid() {
        return grid;
    }

    public int getScore() {
        return score;
    }

    // SLIDES --------------------------------------------------------------------------------------
    public boolean rightSlide() {
        boolean moved = false;
        for (int row = 0; row < size; row++) {
            // On parcourt de droite à gauche
            for (int col = size - 2; col >= 0; col--) {
                if (grid[row][col] != null) {
                    moved |= moveTile(row, col, 0, 1);
                }
            }
        }
        if (moved) generateTile();
        return moved;
    }

    public boolean leftSlide() {
        boolean moved = false;
        for (int row = 0; row < size; row++) {
            // On parcourt de gauche à droite
            for (int col = 1; col < size; col++) {
                if (grid[row][col] != null) {
                    moved |= moveTile(row, col, 0, -1);
                }
            }
        }
        if (moved) generateTile();
        return moved;
    }

    public boolean upSlide() {
        boolean moved = false;
        for (int col = 0; col < size; col++) {
            // On parcourt de haut en bas
            for (int row = 1; row < size; row++) {
                if (grid[row][col] != null) {
                    moved |= moveTile(row, col, -1, 0);
                }
            }
        }
        if (moved) generateTile();
        return moved;
    }

    public boolean downSlide() {
        boolean moved = false;
        for (int col = 0; col < size; col++) {
            // On parcourt de bas en haut
            for (int row = size - 2; row >= 0; row--) {
                if (grid[row][col] != null) {
                    moved |= moveTile(row, col, 1, 0);
                }
            }
        }
        if (moved) generateTile();
        return moved;
    }

    // DÉPLACEMENT D'UNE TUILE ---------------------------------------------------------------------
    private boolean moveTile(int row, int col, int dRow, int dCol) {
        boolean moved = false;
        int newRow = row + dRow;
        int newCol = col + dCol;

        while (isInBounds(newRow, newCol)) {
            if (grid[newRow][newCol] == null) {
                // Case vide => on déplace
                grid[newRow][newCol] = grid[row][col];
                grid[row][col] = null;
                row = newRow;
                col = newCol;
                newRow += dRow;
                newCol += dCol;
                moved = true;
            } else if (grid[newRow][newCol].getValue() == grid[row][col].getValue()) {
                // Même valeur => on fusionne
                score += grid[newRow][newCol].merge(grid[row][col]);
                grid[row][col] = null;
                moved = true;
                break; // une tuile ne peut fusionner qu'une seule fois par slide : 2;2;4 => 4;4 et non 8
            } else {
                // Tuile différente => on s'arrête
                break;
            }
        }
        return moved;
    }

    // GÉNÉRATION D'UNE TUILE ----------------------------------------------------------------------
    public boolean generateTile() {
        // Récupère toutes les cases vides
        ArrayList<int[]> emptyCells = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid[row][col] == null) {
                    emptyCells.add(new int[]{row, col});
                }
            }
        }
        if (emptyCells.isEmpty()) return false;

        // Choisit une case vide aléatoire
        Random random = new Random();
        int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
        grid[cell[0]][cell[1]] = new Tile();
        return true;
    }

    // ÉTAT DU JEU ---------------------------------------------------------------------------------
    public boolean isWon() {
        for (int row = 0; row < size; row++)
            for (int col = 0; col < size; col++)
                if (grid[row][col] != null && grid[row][col].getValue() == 2048) return true;
        return false;
    }

    public boolean isGameOver() {
        // S'il reste des cases vides => pas game over
        for (int row = 0; row < size; row++)
            for (int col = 0; col < size; col++)
                if (grid[row][col] == null) return false;

        // S'il existe des fusions possibles => pas game over
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int val = grid[row][col].getValue();
                if (row + 1 < size && grid[row + 1][col].getValue() == val) return false;
                if (col + 1 < size && grid[row][col + 1].getValue() == val) return false;
            }
        }
        return true;
    }

    // UTILITAIRES ---------------------------------------------------------------------------------
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    public int getMaxTile() {
        int max = 0;

        for (int r = 0; r < this.getSize(); r++) {
            for (int c = 0; c < this.getSize(); c++) {
                Tile t = this.getGrid()[r][c];
                if (t != null && t.getValue() > max) max = t.getValue();
            }
        }
        return max;
    }
}


