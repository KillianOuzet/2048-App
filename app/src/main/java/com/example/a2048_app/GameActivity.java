package com.example.a2048_app;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;


import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.a2048_app.DbEntity.Game;
import com.example.a2048_app.DbEntity.Player;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            factory = new GameViewModelFactory(getApplication(), gridSize);
        } else {
            String savedGridJson = prefs.getString(gridStateKey, null);

            if (savedGridJson != null) {
                Grid savedGrid = gson.fromJson(savedGridJson, Grid.class);
                factory = new GameViewModelFactory(getApplication(), gridSize, savedGrid);
            } else {
                factory = new GameViewModelFactory(getApplication(),gridSize);
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
        boolean isNewBest = false;

        if (currentScore > bestScore) {
            bestScore = currentScore;
            tvBestScore.setText(String.valueOf(bestScore));
            isNewBest = true;

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

          if (grid.isWon()) {
            showEndGameBottomSheet(true, currentScore, isNewBest, grid);
        } else if (grid.isGameOver()) {
            showEndGameBottomSheet(false, currentScore, isNewBest, grid);
        }
      }

    private void showEndGameBottomSheet(boolean isWin, int finalScore, boolean isNewBestRecord, Grid grid) {
        BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_end_game, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setCancelable(false);

        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        TextView tvEmoji = bottomSheetView.findViewById(R.id.tv_end_emoji);
        TextView tvTitle = bottomSheetView.findViewById(R.id.tv_end_title);
        TextView tvScoreVal = bottomSheetView.findViewById(R.id.tv_end_score_val);
        MaterialCardView cardBadge = bottomSheetView.findViewById(R.id.card_end_badge);
        TextView tvBadge = bottomSheetView.findViewById(R.id.tv_end_badge);
        AutoCompleteTextView inputPseudo = bottomSheetView.findViewById(R.id.input_pseudo);
        MaterialButton btnSave = bottomSheetView.findViewById(R.id.btn_save_score);
        MaterialButton btnRestart = bottomSheetView.findViewById(R.id.btn_restart_only);

        // --- Récupération des couleurs depuis le thème actuel ---
        int themePrimary = getThemeColor(R.attr.primaryColor);
        int themeError = getThemeColor(R.attr.dangerColor);

        model.getAllPlayers().observe(this, players -> {
            List<String> playerNames = new ArrayList<>();
            for (Player player : players) {
                if (!player.getName().equals("Anonyme"))
                    playerNames.add(player.getName());
            }

            ArrayAdapter<String> autoAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    playerNames
            );
            inputPseudo.setAdapter(autoAdapter);
        });

        // Si l'utilisateur a gagné on utilise la couleur primaire, sinon la couleur d'erreur (rouge par défaut)
        int currentColor = isWin ? themePrimary : themeError;

        tvScoreVal.setText(String.format("%,d", finalScore).replace(',', ' ')); // Formatage des milliers
        tvEmoji.setText(isWin ? "🎉" : "😔");
        tvTitle.setText(isWin ? "Félicitations !" : "Game Over");
        tvTitle.setTextColor(currentColor);

        btnSave.setBackgroundColor(currentColor);

        if (isNewBestRecord) {
            tvBadge.setText("✨ Nouveau meilleur score !");
            tvBadge.setTextColor(themePrimary);
            cardBadge.setStrokeColor(themePrimary);
            // Fond légèrement coloré (30 sur 255 d'opacité)
            cardBadge.setCardBackgroundColor(androidx.core.graphics.ColorUtils.setAlphaComponent(themePrimary, 30));
        } else {
            int maxTile = grid.getMaxTile();
            tvBadge.setText("Tuile max : " + maxTile);
            tvBadge.setTextColor(themeError);
            cardBadge.setStrokeColor(themeError);
            cardBadge.setCardBackgroundColor(androidx.core.graphics.ColorUtils.setAlphaComponent(themeError, 30));
        }

        btnSave.setOnClickListener(v -> {
            String pseudo = inputPseudo.getText() != null ? inputPseudo.getText().toString().trim() : "";
            final String finalPseudo = pseudo;

            if (pseudo.isEmpty()){
                Game gameToSave = new Game(grid.getScore(), grid.getSize(), grid.getMaxTile(), 10, 1, 1);
                model.saveGame(gameToSave);
            }
            else {
                model.getPlayerByName(finalPseudo).observe(this, player -> {
                    int playerId;
                    if (player != null) {
                        playerId = player.getId();
                        Game gameToSave = new Game(
                                grid.getScore(),
                                grid.getSize(),
                                grid.getMaxTile(),
                                grid.getNbMove(),
                                playerId,
                                1
                        );
                        model.saveGame(gameToSave);
                    } else {
                        model.insertPlayerAndSaveGame(finalPseudo, grid);
                    }
                });
            }

            android.widget.Toast.makeText(this, "Score enregistré pour " + pseudo, android.widget.Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
            model.resetGrid(getIntent().getIntExtra("grid_size", 4));
        });

        btnRestart.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            model.resetGrid(getIntent().getIntExtra("grid_size", 4));
        });

        bottomSheetDialog.show();
    }

    // Méthode utilitaire pour lire un attribut couleur du thème courant
    private int getThemeColor(int attrResId) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(attrResId, typedValue, true);
        return typedValue.data;
    }

    /*private void showEndGameBottomSheet() {
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
    }*/
}