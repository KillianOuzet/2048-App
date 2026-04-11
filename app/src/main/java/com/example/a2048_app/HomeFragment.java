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

import java.util.List;
import java.util.function.Supplier;

public class HomeFragment extends Fragment {

    private SharedPreferences prefs;
    private Gson gson;

    private MaterialCardView cardSavedGame;
    private GridLayout miniGridLayout;
    private TextView tvSavedDetails;
    private MaterialButton btnResume;

    private TextView textBestScore;

    private TextView textCurrentScore;

    Supplier<Integer> getSelectedSize;

    ChipGroup chipGroupGridSize;

    private TextView textGamesPlayed;

    private GameDao gameDao;

    private LiveData<Integer> gamesPlayedLiveData;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("2048_settings", Context.MODE_PRIVATE);
        gson = new Gson();

        cardSavedGame = view.findViewById(R.id.card_saved_game);
        miniGridLayout = view.findViewById(R.id.mini_grid_layout);
        tvSavedDetails = view.findViewById(R.id.tv_saved_details);
        btnResume = view.findViewById(R.id.btn_resume);

        textBestScore = view.findViewById(R.id.text_best_score);
        textCurrentScore = view.findViewById(R.id.text_current_score);
        textGamesPlayed = view.findViewById(R.id.text_games_played);

        AppDatabase db = AppDatabase.getInstance(requireContext());
        gameDao = db.gameDao();

        MaterialButton buttonNewGame = view.findViewById(R.id.newGame_button);
        MaterialCardView classic_card = view.findViewById(R.id.card_classic_mode);

        chipGroupGridSize = view.findViewById(R.id.chipGroupGridSize);

        getSelectedSize = () -> {
            int checkedId = chipGroupGridSize.getCheckedChipId();
            if (checkedId == R.id.chip_3x3) return 3;
            if (checkedId == R.id.chip_5x5) return 5;
            if (checkedId == R.id.chip_6x6) return 6;
            return 4;
        };

        chipGroupGridSize.setOnCheckedStateChangeListener((chipGroup, list) -> {
            String lastMode = prefs.getString("last_played_mode", "classique");
            int size = getSelectedSize.get();

            updateScoresDisplay(lastMode, size);

            prefs.edit().putInt("current_grid_size", size).apply();

            loadSavedGamePreview(lastMode, size);
        });

        buttonNewGame.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), GameActivity.class);
            intent.putExtra("grid_size", getSelectedSize.get());
            intent.putExtra("game_mode", "classique");
            intent.putExtra("force_new_game", true);
            startActivity(intent);
        });

        classic_card.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), GameActivity.class);
            intent.putExtra("grid_size", 4);
            intent.putExtra("game_mode", "classique");
            intent.putExtra("force_new_game", true);
            startActivity(intent);
        });

        MaterialCardView tuto_card = view.findViewById(R.id.card_tuto_mode);

        tuto_card.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), TutorialActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        String lastMode = prefs.getString("last_played_mode", "classique");
        int savedGridSize = prefs.getInt("current_grid_size", 4);

        updateScoresDisplay(lastMode, savedGridSize);

        int chipIdToCheck;
        if (savedGridSize == 3) {
            chipIdToCheck = R.id.chip_3x3;
        } else if (savedGridSize == 5) {
            chipIdToCheck = R.id.chip_5x5;
        } else if (savedGridSize == 6) {
            chipIdToCheck = R.id.chip_6x6;
        } else {
            chipIdToCheck = R.id.chip_4x4;
        }

        chipGroupGridSize.check(chipIdToCheck);

        loadSavedGamePreview(lastMode, savedGridSize);
    }

    private void updateScoresDisplay(String mode, int size) {
        String bestScoreKey = "best_score_" + mode + "_" + size;
        int bestScore = prefs.getInt(bestScoreKey, 0);
        textBestScore.setText(String.format("%,d", bestScore).replace(',', ' '));

        String gridStateKey = "last_grid_" + mode + "_" + size;
        String savedGridJson = prefs.getString(gridStateKey, null);
        int currentScore = 0;

        if (savedGridJson != null) {
            Grid savedGrid = gson.fromJson(savedGridJson, Grid.class);
            if (savedGrid != null) {
                currentScore = savedGrid.getScore();
            }
        }
        textCurrentScore.setText(String.format("%,d", currentScore).replace(',', ' '));

        if (gamesPlayedLiveData != null) {
            gamesPlayedLiveData.removeObservers(getViewLifecycleOwner());
        }

        int modeId = 1;
        if (mode.equals("multijoueur")) modeId = 2;
        else if (mode.equals("defi")) modeId = 3;

        gamesPlayedLiveData = gameDao.getNbGamePlayedByGridSizeAndMode(size, modeId);
        gamesPlayedLiveData.observe(getViewLifecycleOwner(), count -> {
            int nbParties = (count != null) ? count : 0;
            textGamesPlayed.setText(String.valueOf(nbParties));
        });
    }

    private void loadSavedGamePreview(String gameMode, int gridSize) {
        String gridStateKey = "last_grid_" + gameMode + "_" + gridSize;
        String savedGridJson = prefs.getString(gridStateKey, null);

        if (savedGridJson != null) {
            cardSavedGame.setVisibility(View.VISIBLE);

            Grid savedGrid = gson.fromJson(savedGridJson, Grid.class);

            String details = "Score : " + String.format("%,d", savedGrid.getScore()).replace(',', ' ') + " · Grille " + gridSize + "×" + gridSize;
            tvSavedDetails.setText(details);

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

    private void buildMiniGrid(Grid grid) {
        int size = grid.getSize();
        miniGridLayout.removeAllViews();
        miniGridLayout.setColumnCount(size);
        miniGridLayout.setRowCount(size);

        Tile[][] tiles = grid.getGrid();
        Context context = requireContext();

        float scaleFactor = 4.0f / size;

        int margin = Math.max(1, (int) dpToPx(1.5f * scaleFactor));

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                TextView cell = new TextView(context);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(row, 1, 1f);
                params.columnSpec = GridLayout.spec(col, 1, 1f);
                params.width = 0;
                params.height = 0;
                params.setMargins(margin, margin, margin, margin);
                cell.setLayoutParams(params);

                int value = (tiles[row][col] != null) ? tiles[row][col].getValue() : 0;

                cell.setGravity(Gravity.CENTER);
                cell.setTypeface(ResourcesCompat.getFont(context, R.font.outfit_black));

                cell.setIncludeFontPadding(false);
                cell.setPadding(0, 0, 0, 0);
                cell.setMaxLines(1);

                if (value > 0) {
                    cell.setText(String.valueOf(value));

                    if (value >= 1000) {
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 5f * scaleFactor);
                    } else if (value >= 100) {
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 6.5f * scaleFactor);
                    } else if (value >= 10) {
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f * scaleFactor);
                    } else {
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f * scaleFactor);
                    }
                }

                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);

                bg.setCornerRadius(dpToPx(4f * scaleFactor));
                bg.setColor(TileTheme.getBackgroundColor(context, value));

                cell.setTextColor(TileTheme.getTextColor(context, value));
                cell.setBackground(bg);

                miniGridLayout.addView(cell);
            }
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}