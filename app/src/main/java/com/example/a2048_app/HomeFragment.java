package com.example.a2048_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

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

    Supplier<Integer> getSelectedSize;

    ChipGroup chipGroupGridSize;

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

        MaterialButton buttonNewGame = view.findViewById(R.id.newGame_button);
        MaterialCardView classic_card = view.findViewById(R.id.card_classic_mode);

        chipGroupGridSize = view.findViewById(R.id.chipGroupGridSize);

        getSelectedSize = () -> {
            int checkedId = chipGroupGridSize.getCheckedChipId();
            if (checkedId == R.id.chip_3x3) return 3;
            if (checkedId == R.id.chip_5x5) return 5;
            if (checkedId == R.id.chip_6x6) return 6;
            return 4; // défaut 4×4
        };

        chipGroupGridSize.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup chipGroup, @NonNull List<Integer> list) {
                String lastMode = prefs.getString("last_played_mode", "classique");
                String bestScoreKey = "best_score_" + lastMode + "_" + getSelectedSize.get();
                int bestScore = prefs.getInt(bestScoreKey, 0);
                textBestScore.setText(String.format("%,d", bestScore).replace(',', ' '));

                prefs.edit().putInt("current_grid_size", getSelectedSize.get()).apply();
            }
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
    }

    @Override
    public void onResume() {
        super.onResume();

        String lastMode = prefs.getString("last_played_mode", "classique");
        int lastSize = prefs.getInt("last_played_size", 4);

        String bestScoreKey = "best_score_" + lastMode + "_" + getSelectedSize.get();
        int bestScore = prefs.getInt(bestScoreKey, 0);

        textBestScore.setText(String.format("%,d", bestScore).replace(',', ' '));

        int savedGridSize = prefs.getInt("current_grid_size", 4);

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

        loadSavedGamePreview(lastMode, lastSize);
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

        int margin = (int) dpToPx(1.5f);

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
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 5f);
                    } else if (value >= 100) {
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 6.5f);
                    } else if (value >= 10) {
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f);
                    } else {
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
                    }
                }

                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setCornerRadius(dpToPx(4));
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