package com.example.a2048_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;


import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

public class GameActivity extends BaseActivity {

    private GameViewModel model;
    private GridLayout gameGrid;

    private TextView tvScore;
    private TextView tvBestScore;
    private int bestScore = 0;
    private String bestScoreKey;

    private SharedPreferences prefs;

    Gson gson = new Gson();

    String gameMode;

    int gridSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.prefs = getSharedPreferences("2048_settings", Context.MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        tvScore = findViewById(R.id.tv_score);
        tvBestScore = findViewById(R.id.tv_best_score);

        gridSize = getIntent().getIntExtra("grid_size", 4);
        gameMode = getIntent().getStringExtra("game_mode");
        if (gameMode == null) gameMode = "classique";

        prefs.edit().putString("last_played_mode", gameMode).putInt("last_played_size", gridSize).apply();

        bestScoreKey = "best_score_" + gameMode + "_" + gridSize;

        bestScore = prefs.getInt(bestScoreKey, 0);

        tvBestScore.setText(String.valueOf(bestScore));
        tvScore.setText("0");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        String gridStateKey = "last_grid_" + gameMode + "_" + gridSize;
        boolean forceNewGame = getIntent().getBooleanExtra("force_new_game", false);

        GameViewModelFactory factory;

        if (forceNewGame) {
            prefs.edit().remove(gridStateKey).apply();
            factory = new GameViewModelFactory(gridSize);
        } else {
            String savedGridJson = prefs.getString(gridStateKey, null);

            if (savedGridJson != null) {
                Grid savedGrid = gson.fromJson(savedGridJson, Grid.class);
                factory = new GameViewModelFactory(gridSize, savedGrid);
            } else {
                factory = new GameViewModelFactory(gridSize);
            }
        }

        model = new ViewModelProvider(this, factory).get(GameViewModel.class);

        gameGrid = findViewById(R.id.game_grid);

        buildGrid(gridSize);

        model.getCurrentGrid().observe(this, this::updateGrid);

        MaterialCardView buttonRestart = findViewById(R.id.btn_restart);

        buttonRestart.setOnClickListener(v -> model.resetGrid(gridSize));

        findViewById(R.id.grid_parent).setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                model.rightSlide();
            }

            @Override
            public void onSwipeLeft() {
                model.leftSlide();
            }

            @Override
            public void onSwipeTop() {
                model.upSlide();
            }

            @Override
            public void onSwipeBottom() {
                model.downSlide();
            }
        });

    }

    private void buildGrid(int size) {
        gameGrid.removeAllViews();
        gameGrid.setColumnCount(size);
        gameGrid.setRowCount(size);

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {

                TextView cell = new TextView(this);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(row, 1, 1f);
                params.columnSpec = GridLayout.spec(col, 1, 1f);
                params.width = 0;
                params.height = 0;
                params.setMargins(8, 8, 8, 8);
                cell.setLayoutParams(params);

                cell.setGravity(Gravity.CENTER);
                cell.setTextColor(Color.WHITE);
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                cell.setTypeface(ResourcesCompat.getFont(this, R.font.outfit_black));

                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setCornerRadius(dpToPx());
                bg.setColor(TileTheme.getBackgroundColor(this, 0));
                cell.setBackground(bg);

                cell.setTag(row * size + col);

                gameGrid.addView(cell);
            }
        }
    }

    private float dpToPx() {
        return (float) 8 * getResources().getDisplayMetrics().density;
    }

    private void updateGrid(Grid grid) {
        int size = grid.getSize();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                TextView cell = gameGrid.findViewWithTag(row * size + col);
                Tile tile = grid.getGrid()[row][col];

                int newValue = (tile != null) ? tile.getValue() : 0;

                String oldText = cell.getText().toString();
                int oldValue = oldText.isEmpty() ? 0 : Integer.parseInt(oldText);

                if (newValue != oldValue) {
                    cell.setText(newValue > 0 ? String.valueOf(newValue) : "");
                    if (newValue >= 1024) cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    else if (newValue >= 128) cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                    else cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);

                    GradientDrawable bg = (GradientDrawable) cell.getBackground();
                    bg.setColor(TileTheme.getBackgroundColor(this, newValue));
                    cell.setTextColor(TileTheme.getTextColor(this, newValue));

                    if (prefs.getInt("animationsTuiles", 1) == 1) {
                        if (oldValue == 0 && newValue > 0) {
                            cell.setScaleX(0f);
                            cell.setScaleY(0f);
                            cell.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                        } else if (newValue > oldValue) {
                            cell.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() -> {
                                cell.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                            }).start();
                        }
                    }
                }
            }
        }

        int currentScore = grid.getScore();
        tvScore.setText(String.valueOf(currentScore));

        if (currentScore > bestScore) {
            bestScore = currentScore;
            tvBestScore.setText(String.valueOf(bestScore));

            android.content.SharedPreferences prefs = getSharedPreferences("2048_settings", MODE_PRIVATE);
            prefs.edit().putInt(bestScoreKey, bestScore).apply();
        }

        String gridStateKey = "last_grid_" + gameMode + "_" + gridSize;

        if (!grid.isGameOver() && !grid.isWon()) {
            String json = gson.toJson(grid);
            prefs.edit().putString(gridStateKey, json).apply();
        } else {
            prefs.edit().remove(gridStateKey).apply();
        }

        if (grid.isWon() || grid.isGameOver()) {
            showEndGameBottomSheet();
        }
    }

    private void showEndGameBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_end_game, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);

        view.findViewById(R.id.btn_replay).setOnClickListener(v -> {
            dialog.dismiss();
            model.resetGrid(gridSize);
        });

        view.findViewById(R.id.btn_home).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}