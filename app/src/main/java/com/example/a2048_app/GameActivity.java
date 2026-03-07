package com.example.a2048_app;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;

public class GameActivity extends BaseActivity {

    private GameViewModel model;
    private GridLayout gameGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        int gridSize = getIntent().getIntExtra("grid_size", 5);
        GameViewModelFactory factory = new GameViewModelFactory(gridSize);
        model = new ViewModelProvider(this, factory).get(GameViewModel.class);

        gameGrid = findViewById(R.id.game_grid);

        buildGrid(gridSize);

        model.getCurrentGrid().observe(this, this::updateGrid);

        MaterialCardView buttonRestart = findViewById(R.id.btn_restart);

        buttonRestart.setOnClickListener(v -> model.resetGrid(gridSize));

    }

    private void buildGrid(int size) {
        gameGrid.removeAllViews();
        gameGrid.setColumnCount(size);
        gameGrid.setRowCount(size);

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {

                TextView cell = new TextView(this);

                // Paramètres de positionnement dans le GridLayout
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec    = GridLayout.spec(row, 1, 1f);
                params.columnSpec = GridLayout.spec(col, 1, 1f);
                params.width  = 0;
                params.height = 0;
                params.setMargins(8, 8, 8, 8);
                cell.setLayoutParams(params);

                // Style de la cellule
                cell.setGravity(Gravity.CENTER);
                cell.setTextColor(Color.WHITE);
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                cell.setTypeface(ResourcesCompat.getFont(this, R.font.outfit_black));

                // Fond arrondi vide
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setCornerRadius(dpToPx(8));
                bg.setColor(TileTheme.getBackgroundColor(this,0)); // 0 = case vide
                cell.setBackground(bg);

                // Tag pour retrouver la cellule facilement
                cell.setTag(row * size + col);

                gameGrid.addView(cell);
            }
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void updateGrid(Grid grid) {
        int size = grid.getSize();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                TextView cell = gameGrid.findViewWithTag(row * size + col);
                Tile tile = grid.getGrid()[row][col];

                int value = (tile != null) ? tile.getValue() : 0;

                // Texte
                cell.setText(value > 0 ? String.valueOf(value) : "");

                // Taille du texte selon la valeur
                if (value >= 1024)     cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                else if (value >= 128) cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                else                   cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);

                // Couleurs via TileTheme
                GradientDrawable bg = (GradientDrawable) cell.getBackground();
                bg.setColor(TileTheme.getBackgroundColor(this, value));
                cell.setTextColor(TileTheme.getTextColor(this, value));
            }
        }
    }
}
