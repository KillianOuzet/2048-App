package com.example.a2048_app;

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

/**
 * Activité principale gérant l'interface de la partie en cours.
 * Elle respecte le pattern MVVM : elle ne contient aucune logique mathématique,
 * elle se contente d'observer le GameViewModel et de mettre à jour l'affichage en conséquence.
 */
public class GameActivity extends BaseActivity {

    private GameViewModel model;
    private GridLayout gameGrid;

    private TextView tvScore;
    private TextView tvBestScore;

    private SharedPreferences prefs;
    private final Gson gson = new Gson();

    private int bestScore = 0;
    private String bestScoreKey;
    private String gameMode;
    private int gridSize;

    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.prefs = getSharedPreferences("2048_settings", Context.MODE_PRIVATE);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        tvScore = findViewById(R.id.tv_score);
        tvBestScore = findViewById(R.id.tv_best_score);

        gridSize = getIntent().getIntExtra("grid_size", 4);
        gameMode = getIntent().getStringExtra("game_mode");
        if (gameMode == null) gameMode = "classique";

        prefs.edit().putString("last_played_mode", gameMode).putInt("last_played_size", gridSize).apply();

        // Le record local est lié à la taille de la grille
        bestScoreKey = "best_score_" + gameMode + "_" + gridSize;
        bestScore = prefs.getInt(bestScoreKey, 0);
        tvBestScore.setText(String.valueOf(bestScore));
        tvScore.setText("0");

        // Ajustement des marges pour éviter que l'UI ne chevauche la barre de statut (encoche, heure)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // --- Restauration de la session (Hybridation SharedPreferences / ViewModel) ---
        String gridStateKey = "last_grid_" + gameMode + "_" + gridSize;
        boolean forceNewGame = getIntent().getBooleanExtra("force_new_game", false);

        GameViewModelFactory factory;

        if (forceNewGame) {
            prefs.edit().remove(gridStateKey).apply();
            factory = new GameViewModelFactory(getApplication(), gridSize);
        } else {
            // Si le joueur avait quitté l'application en pleine partie, on désérialise l'état JSON
            String savedGridJson = prefs.getString(gridStateKey, null);
            if (savedGridJson != null) {
                Grid savedGrid = gson.fromJson(savedGridJson, Grid.class);
                factory = new GameViewModelFactory(getApplication(), gridSize, savedGrid);
            } else {
                factory = new GameViewModelFactory(getApplication(), gridSize);
            }
        }

        // L'utilisation de ViewModelProvider garantit que le ViewModel survit aux rotations de l'écran
        model = new ViewModelProvider(this, factory).get(GameViewModel.class);

        gameGrid = findViewById(R.id.game_grid);
        buildGrid(gridSize);

        // Connexion vitale du MVVM : Dès que le modèle modifie la grille, updateGrid() est appelée automatiquement.
        model.getCurrentGrid().observe(this, this::updateGrid);

        MaterialCardView buttonRestart = findViewById(R.id.btn_restart);
        buttonRestart.setOnClickListener(v -> model.resetGrid(gridSize));

        // --- Gestion des balayages et du Sound Design ("Game Feel") ---
        findViewById(R.id.grid_parent).setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                handleSwipe(() -> model.rightSlide());
            }

            @Override
            public void onSwipeLeft() {
                handleSwipe(() -> model.leftSlide());
            }

            @Override
            public void onSwipeTop() {
                handleSwipe(() -> model.upSlide());
            }

            @Override
            public void onSwipeBottom() {
                handleSwipe(() -> model.downSlide());
            }
        });

        soundManager = new SoundManager(this);
    }

    /**
     * Centralise la logique audio lors d'un mouvement.
     * On mémorise le score AVANT l'action. Si le score a augmenté après, c'est qu'une fusion a eu lieu.
     */
    private void handleSwipe(java.util.function.Supplier<Boolean> slideAction) {
        int oldScore = Objects.requireNonNull(model.getCurrentGrid().getValue()).getScore();

        if (slideAction.get()) { // Si la grille s'est effectivement déplacée
            soundManager.playMoveSound();

            int newScore = model.getCurrentGrid().getValue().getScore();
            if (newScore > oldScore) {
                soundManager.playMergeSound();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libération de la mémoire allouée au pool audio (crucial pour éviter les fuites)
        if (soundManager != null) {
            soundManager.release();
        }
    }

    /**
     * Génère la vue de la grille programmatiquement.
     * Cela permet de gérer dynamiquement des grilles de 3x3 à 6x6 sans créer de multiples fichiers XML.
     */
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

                // Le Tag permet de retrouver instantanément cette cellule lors de la mise à jour (updateGrid)
                cell.setTag(row * size + col);

                gameGrid.addView(cell);
            }
        }
    }

    private float dpToPx() {
        return (float) 8 * getResources().getDisplayMetrics().density;
    }

    /**
     * Méthode appelée à chaque action du joueur par le LiveData.
     * Elle met à jour les couleurs, textes, animations, et gère la sauvegarde automatique.
     */
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

                    // Auto-sizing basique pour éviter que les grands nombres (ex: 2048) ne débordent de la tuile
                    if (newValue >= 1024) cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    else if (newValue >= 128) cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                    else cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);

                    GradientDrawable bg = (GradientDrawable) cell.getBackground();
                    bg.setColor(TileTheme.getBackgroundColor(this, newValue));
                    cell.setTextColor(TileTheme.getTextColor(this, newValue));

                    // Animations conditionnelles (Apparition vs Fusion)
                    if (prefs.getInt("animationsTuiles", 1) == 1) {
                        if (oldValue == 0 && newValue > 0) { // Nouvelle tuile (pop)
                            cell.setScaleX(0f);
                            cell.setScaleY(0f);
                            cell.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                        } else if (newValue > oldValue) { // Fusion (rebond)
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
            prefs.edit().putInt(bestScoreKey, bestScore).apply();
        }

        // Sérialisation continue de l'état du jeu pour ne rien perdre en cas de fermeture brutale
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

    /**
     * Interface de fin de partie. Propose d'associer un joueur (Player) à la session (Game)
     * pour l'enregistrement définitif dans la base de données Room.
     */
    private void showEndGameBottomSheet(boolean isWin, int finalScore, boolean isNewBestRecord, Grid grid) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_end_game, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setCancelable(false);

        // Permet à la popup de remonter quand le clavier virtuel s'affiche
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        TextView tvEmoji = bottomSheetView.findViewById(R.id.tv_end_emoji);
        TextView tvTitle = bottomSheetView.findViewById(R.id.tv_end_title);
        TextView tvScoreVal = bottomSheetView.findViewById(R.id.tv_end_score_val);
        MaterialCardView cardBadge = bottomSheetView.findViewById(R.id.card_end_badge);
        TextView tvBadge = bottomSheetView.findViewById(R.id.tv_end_badge);
        AutoCompleteTextView inputPseudo = bottomSheetView.findViewById(R.id.input_pseudo);
        MaterialButton btnSave = bottomSheetView.findViewById(R.id.btn_save_score);
        MaterialButton btnRestart = bottomSheetView.findViewById(R.id.btn_restart_only);

        int themePrimary = getThemeColor(R.attr.primaryColor);
        int themeError = getThemeColor(R.attr.dangerColor);

        // Auto-complétion : on observe la base de données Room pour proposer les pseudos déjà utilisés
        model.getAllPlayers().observe(this, players -> {
            List<String> playerNames = new ArrayList<>();
            for (Player player : players) {
                if (!player.getName().equals("Anonyme")) playerNames.add(player.getName());
            }
            ArrayAdapter<String> autoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, playerNames);
            inputPseudo.setAdapter(autoAdapter);
        });

        int currentColor = isWin ? themePrimary : themeError;
        tvScoreVal.setText(String.format("%,d", finalScore).replace(',', ' '));
        tvEmoji.setText(isWin ? "🎉" : "😔");
        tvTitle.setText(isWin ? "Félicitations !" : "Game Over");
        tvTitle.setTextColor(currentColor);
        btnSave.setBackgroundColor(currentColor);

        if (isNewBestRecord) {
            tvBadge.setText("✨ Nouveau meilleur score !");
            tvBadge.setTextColor(themePrimary);
            cardBadge.setStrokeColor(themePrimary);
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

            if (pseudo.isEmpty()) {
                // Sauvegarde anonyme (modeId = 1 pour le mode Classique)
                Game gameToSave = new Game(grid.getScore(), grid.getSize(), grid.getMaxTile(), grid.getNbMove(), 1, 1);
                model.saveGame(gameToSave);
            } else {
                // Requête BDD pour vérifier si le joueur existe. S'il n'existe pas, on le crée avant de sauvegarder.
                model.getPlayerByName(finalPseudo).observe(this, player -> {
                    if (player != null) {
                        int playerId = player.getId();
                        // L'ID 1 correspond au Mode Classique
                        Game gameToSave = new Game(grid.getScore(), grid.getSize(), grid.getMaxTile(), grid.getNbMove(), playerId, 1);
                        model.saveGame(gameToSave);
                    } else {
                        // CORRECTION ICI : Ajout du '1' en 3ème argument pour spécifier le Mode Classique
                        model.insertPlayerAndSaveGame(finalPseudo, grid, 1);
                    }
                });
            }

            android.widget.Toast.makeText(this, "Score enregistré pour " + (pseudo.isEmpty() ? "Anonyme" : pseudo), android.widget.Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
            model.resetGrid(gridSize);
        });

        btnRestart.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            model.resetGrid(gridSize);
        });

        bottomSheetDialog.show();
    }

    /**
     * Utilitaire pour récupérer une couleur dynamiquement en fonction du thème (Clair/Sombre) actuellement appliqué.
     */
    private int getThemeColor(int attrResId) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(attrResId, typedValue, true);
        return typedValue.data;
    }
}