package com.example.a2048_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.a2048_app.DbDao.GameDao;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

import java.util.function.Supplier;

/**
 * Fragment gérant l'écran d'accueil de l'application.
 * Il permet de configurer la taille de la grille, de consulter ses records
 * et de reprendre une partie sauvegardée grâce à une prévisualisation miniature.
 */
public class HomeFragment extends Fragment {

    private SharedPreferences prefs;
    private Gson gson;

    // Éléments de la carte "Reprendre la partie"
    private MaterialCardView cardSavedGame;
    private GridLayout miniGridLayout;
    private TextView tvSavedDetails;
    private MaterialButton btnResume;

    // Affichage des scores et statistiques
    private TextView textBestScore;
    private TextView textCurrentScore;
    private TextView textGamesPlayed;

    // Composants de sélection
    private ChipGroup chipGroupGridSize;
    private Supplier<Integer> getSelectedSize;

    // Accès aux données
    private GameDao gameDao;
    private LiveData<Integer> gamesPlayedLiveData;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialisation des outils de persistance
        prefs = requireActivity().getSharedPreferences("2048_settings", Context.MODE_PRIVATE);
        gson = new Gson();
        gameDao = AppDatabase.getInstance(requireContext()).gameDao();

        // Liaison des vues
        cardSavedGame = view.findViewById(R.id.card_saved_game);
        miniGridLayout = view.findViewById(R.id.mini_grid_layout);
        tvSavedDetails = view.findViewById(R.id.tv_saved_details);
        btnResume = view.findViewById(R.id.btn_resume);
        textBestScore = view.findViewById(R.id.text_best_score);
        textCurrentScore = view.findViewById(R.id.text_current_score);
        textGamesPlayed = view.findViewById(R.id.text_games_played);
        chipGroupGridSize = view.findViewById(R.id.chipGroupGridSize);

        // Définition de la logique de récupération de la taille via un Supplier
        getSelectedSize = () -> {
            int checkedId = chipGroupGridSize.getCheckedChipId();
            if (checkedId == R.id.chip_3x3) return 3;
            if (checkedId == R.id.chip_5x5) return 5;
            if (checkedId == R.id.chip_6x6) return 6;
            return 4; // Valeur par défaut
        };

        // Écouteur sur le changement de taille de grille
        chipGroupGridSize.setOnCheckedStateChangeListener((chipGroup, list) -> {
            String lastMode = prefs.getString("last_played_mode", "classique");
            int size = getSelectedSize.get();

            updateScoresDisplay(lastMode, size);
            prefs.edit().putInt("current_grid_size", size).apply();
            loadSavedGamePreview(lastMode, size);
        });

        // Bouton "Nouvelle Partie"
        view.findViewById(R.id.newGame_button).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), GameActivity.class);
            intent.putExtra("grid_size", getSelectedSize.get());
            intent.putExtra("game_mode", "classique");
            intent.putExtra("force_new_game", true); // On ignore la sauvegarde existante
            startActivity(intent);
        });

        // Mode Classique (Raccourci 4x4)
        view.findViewById(R.id.card_classic_mode).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), GameActivity.class);
            intent.putExtra("grid_size", 4);
            intent.putExtra("game_mode", "classique");
            intent.putExtra("force_new_game", true);
            startActivity(intent);
        });

        // Accès au tutoriel
        view.findViewById(R.id.card_tuto_mode).setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), TutorialActivity.class));
        });
    }

    /**
     * Rafraîchit les informations à chaque fois que l'utilisateur revient sur l'écran d'accueil.
     */
    @Override
    public void onResume() {
        super.onResume();
        String lastMode = prefs.getString("last_played_mode", "classique");
        int savedGridSize = prefs.getInt("current_grid_size", 4);

        updateScoresDisplay(lastMode, savedGridSize);
        updateChipSelection(savedGridSize);
        loadSavedGamePreview(lastMode, savedGridSize);
    }

    /**
     * Met à jour graphiquement la puce (Chip) sélectionnée.
     */
    private void updateChipSelection(int size) {
        int id = R.id.chip_4x4;
        if (size == 3) id = R.id.chip_3x3;
        else if (size == 5) id = R.id.chip_5x5;
        else if (size == 6) id = R.id.chip_6x6;
        chipGroupGridSize.check(id);
    }

    /**
     * Affiche le meilleur score, le score actuel de la sauvegarde et
     * le nombre total de parties via Room et LiveData.
     */
    private void updateScoresDisplay(String mode, int size) {
        // Record personnel
        int bestScore = prefs.getInt("best_score_" + mode + "_" + size, 0);
        textBestScore.setText(String.format("%,d", bestScore).replace(',', ' '));

        // Score de la partie en pause
        String savedGridJson = prefs.getString("last_grid_" + mode + "_" + size, null);
        int currentScore = 0;
        if (savedGridJson != null) {
            Grid grid = gson.fromJson(savedGridJson, Grid.class);
            if (grid != null) currentScore = grid.getScore();
        }
        textCurrentScore.setText(String.format("%,d", currentScore).replace(',', ' '));

        // Observation du nombre de parties via le DAO
        if (gamesPlayedLiveData != null)
            gamesPlayedLiveData.removeObservers(getViewLifecycleOwner());

        // Mode Classique = 1 dans la base de données
        gamesPlayedLiveData = gameDao.getNbGamePlayedByGridSizeAndMode(size, 1);
        gamesPlayedLiveData.observe(getViewLifecycleOwner(), count -> {
            textGamesPlayed.setText(String.valueOf(count != null ? count : 0));
        });
    }

    /**
     * Charge et affiche la vue "Reprendre" si une sauvegarde JSON existe.
     */
    private void loadSavedGamePreview(String gameMode, int gridSize) {
        String savedGridJson = prefs.getString("last_grid_" + gameMode + "_" + gridSize, null);

        if (savedGridJson != null) {
            cardSavedGame.setVisibility(View.VISIBLE);
            Grid savedGrid = gson.fromJson(savedGridJson, Grid.class);

            tvSavedDetails.setText(String.format("Score : %,d · Grille %d×%d", savedGrid.getScore(), gridSize, gridSize).replace(',', ' '));

            buildMiniGrid(savedGrid);

            btnResume.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), GameActivity.class);
                intent.putExtra("grid_size", gridSize);
                intent.putExtra("game_mode", gameMode);
                startActivity(intent);
            });
        } else {
            cardSavedGame.setVisibility(View.GONE);
        }
    }

    /**
     * Construit programmatiquement une version miniature du plateau de jeu.
     * Utilise un facteur d'échelle pour s'adapter aux différentes tailles (3x3 à 6x6).
     */
    private void buildMiniGrid(Grid grid) {
        int size = grid.getSize();
        miniGridLayout.removeAllViews();
        miniGridLayout.setColumnCount(size);
        miniGridLayout.setRowCount(size);

        float scaleFactor = 4.0f / size;
        int margin = Math.max(1, (int) dpToPx(1.5f * scaleFactor));

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                TextView cell = new TextView(requireContext());

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(row, 1, 1f);
                params.columnSpec = GridLayout.spec(col, 1, 1f);
                params.width = 0;
                params.height = 0;
                params.setMargins(margin, margin, margin, margin);
                cell.setLayoutParams(params);

                int value = (grid.getGrid()[row][col] != null) ? grid.getGrid()[row][col].getValue() : 0;
                cell.setGravity(Gravity.CENTER);
                cell.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.outfit_black));
                cell.setIncludeFontPadding(false);

                if (value > 0) {
                    cell.setText(String.valueOf(value));
                    // Ajustement dynamique de la taille du texte miniature
                    float textSize = (value >= 1000) ? 5f : (value >= 100) ? 6.5f : (value >= 10) ? 8.5f : 10f;
                    cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize * scaleFactor);
                }

                // Application du thème visuel sur la miniature
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setCornerRadius(dpToPx(4f * scaleFactor));
                bg.setColor(TileTheme.getBackgroundColor(requireContext(), value));

                cell.setTextColor(TileTheme.getTextColor(requireContext(), value));
                cell.setBackground(bg);

                miniGridLayout.addView(cell);
            }
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}