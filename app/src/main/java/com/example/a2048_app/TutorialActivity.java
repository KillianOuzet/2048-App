package com.example.a2048_app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class TutorialActivity extends BaseActivity {

    private int currentStep = 0;
    private final int TOTAL_STEPS = 5;

    private LinearLayout progressDots;
    private TextView tvProgressLabel;
    private FrameLayout stepContainer;
    private MaterialButton btnPrev;
    private MaterialButton btnNext;

    private final Handler tutoHandler = new Handler(Looper.getMainLooper());
    private Runnable currentAnimation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        View tutorialMain = findViewById(R.id.tutorial_main);

        final int originalPaddingLeft = tutorialMain.getPaddingLeft();
        final int originalPaddingRight = tutorialMain.getPaddingRight();

        ViewCompat.setOnApplyWindowInsetsListener(tutorialMain, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(originalPaddingLeft + systemBars.left, systemBars.top, originalPaddingRight + systemBars.right, systemBars.bottom);
            return insets;
        });

        progressDots = findViewById(R.id.progress_dots);
        tvProgressLabel = findViewById(R.id.tv_progress_label);
        stepContainer = findViewById(R.id.step_container);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);

        btnNext.setOnClickListener(v -> {
            if (currentStep < TOTAL_STEPS - 1) {
                currentStep++;
                updateUI();
            } else {
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra("grid_size", 4);
                intent.putExtra("game_mode", "classique");
                intent.putExtra("force_new_game", true);
                startActivity(intent);
                finish();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                updateUI();
            }
        });

        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCurrentAnimation();
    }

    private void stopCurrentAnimation() {
        if (currentAnimation != null) {
            tutoHandler.removeCallbacks(currentAnimation);
            currentAnimation = null;
        }
    }

    private void updateUI() {
        stopCurrentAnimation();

        buildProgressDots();
        tvProgressLabel.setText((currentStep + 1) + " / " + TOTAL_STEPS);

        btnPrev.setVisibility(currentStep == 0 ? View.GONE : View.VISIBLE);
        if (currentStep == TOTAL_STEPS - 1) {
            btnNext.setText("Jouer maintenant !");
        } else {
            btnNext.setText("Suivant →");
        }

        stepContainer.removeAllViews();
        stepContainer.addView(createStepCard(currentStep));
    }

    private void buildProgressDots() {
        progressDots.removeAllViews();
        int primaryColor = getThemeColor(R.attr.primaryColor);
        int inactiveColor = getThemeColor(R.attr.borderColor2);

        for (int i = 0; i < TOTAL_STEPS; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dpToPx(6), 1f);
            params.setMargins(dpToPx(2), 0, dpToPx(2), 0);
            dot.setLayoutParams(params);
            dot.setBackgroundColor(i <= currentStep ? primaryColor : inactiveColor);
            progressDots.addView(dot);
        }
    }

    private View createStepCard(int stepIndex) {
        MaterialCardView card = new MaterialCardView(this);
        card.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        card.setCardBackgroundColor(getThemeColor(R.attr.cardColor));
        card.setRadius(dpToPx(18));
        card.setStrokeColor(getThemeColor(R.attr.borderColor));
        card.setStrokeWidth(dpToPx(1));
        card.setCardElevation(0);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dpToPx(10));

        TextView tvNum = new TextView(this);
        tvNum.setText(String.valueOf(stepIndex + 1));
        tvNum.setTextColor(Color.WHITE);
        tvNum.setTextSize(14f);
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setTypeface(ResourcesCompat.getFont(this, R.font.outfit_black));
        GradientDrawable bgNum = new GradientDrawable();
        bgNum.setColor(getThemeColor(R.attr.primaryColor));
        bgNum.setCornerRadius(dpToPx(8));
        tvNum.setBackground(bgNum);
        LinearLayout.LayoutParams paramNum = new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28));
        paramNum.setMargins(0, 0, dpToPx(10), 0);
        tvNum.setLayoutParams(paramNum);

        TextView tvTitle = new TextView(this);
        tvTitle.setTextSize(18f);
        tvTitle.setTextColor(getThemeColor(R.attr.textColor));
        tvTitle.setTypeface(ResourcesCompat.getFont(this, R.font.outfit_black));

        header.addView(tvNum);
        header.addView(tvTitle);

        TextView tvDesc = new TextView(this);
        tvDesc.setTextSize(14f);
        tvDesc.setTextColor(getThemeColor(R.attr.textColor2));
        tvDesc.setTypeface(ResourcesCompat.getFont(this, R.font.outfit_regular));
        tvDesc.setPadding(0, 0, 0, dpToPx(16));

        layout.addView(header);
        layout.addView(tvDesc);

        FrameLayout demoArea = new FrameLayout(this);
        demoArea.setBackgroundResource(R.drawable.bg_grid);
        demoArea.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        LinearLayout.LayoutParams paramDemo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        demoArea.setLayoutParams(paramDemo);

        switch (stepIndex) {
            case 0:
                tvTitle.setText("La grille de jeu");
                tvDesc.setText("La partie se joue sur une grille 4×4.\nAu départ, deux tuiles numérotées apparaissent aléatoirement.");
                buildStep1(demoArea);
                break;
            case 1:
                tvTitle.setText("Déplacer les tuiles");
                tvDesc.setText("Glissez dans une direction. Toutes les tuiles se déplacent jusqu'au bord ou jusqu'à une autre tuile.");
                buildStep2(demoArea);
                break;
            case 2:
                tvTitle.setText("Fusionner = Doubler");
                tvDesc.setText("Deux tuiles identiques qui se touchent fusionnent. Leur valeur double et s'ajoute au score.");
                buildStep3(demoArea);
                break;
            case 3:
                tvTitle.setText("De nouvelles tuiles");
                tvDesc.setText("Après chaque glissement valide, une nouvelle tuile (2 ou 4) apparaît sur une case vide.");
                buildStep4(demoArea);
                break;
            case 4:
                tvTitle.setText("Objectif & Fin");
                tvDesc.setText("Atteignez la tuile 2048 pour gagner ! La partie se termine si la grille est pleine sans mouvement possible.");
                buildStep5(demoArea);
                break;
        }

        layout.addView(demoArea);
        card.addView(layout);
        return card;
    }

    private void buildStep1(FrameLayout container) {
        GridLayout grid = createMiniGrid();
        container.addView(grid);

        currentAnimation = new Runnable() {
            boolean isVisible = true;

            @Override
            public void run() {
                updateMiniGridCell(grid, 1, isVisible ? 2 : 0);
                updateMiniGridCell(grid, 10, isVisible ? 4 : 0);

                if (isVisible) {
                    grid.getChildAt(1).setScaleX(0);
                    grid.getChildAt(1).setScaleY(0);
                    grid.getChildAt(10).setScaleX(0);
                    grid.getChildAt(10).setScaleY(0);
                    grid.getChildAt(1).animate().scaleX(1).scaleY(1).setDuration(300).start();
                    grid.getChildAt(10).animate().scaleX(1).scaleY(1).setDuration(300).start();
                }
                isVisible = !isVisible;
                tutoHandler.postDelayed(this, 1500);
            }
        };
        tutoHandler.post(currentAnimation);
    }

    private void buildStep2(FrameLayout container) {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);

        GridLayout grid = createMiniGrid();

        int[] initialGrid = {0, 2, 0, 0, 0, 0, 4, 0, 0, 0, 0, 2, 0, 0, 0, 0};
        applyStateToGrid(grid, initialGrid);

        TextView tvIndication = new TextView(this);
        tvIndication.setText("👆 Glissez directement sur la grille");
        tvIndication.setTextSize(14f);
        tvIndication.setTextColor(getThemeColor(R.attr.primaryColor));
        tvIndication.setGravity(Gravity.CENTER);
        tvIndication.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.outfit_bold));
        tvIndication.setPadding(0, dpToPx(15), 0, 0);

        mainLayout.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                handleTutorialSwipe(grid, initialGrid, new int[]{0, 0, 0, 2, 0, 0, 0, 4, 0, 0, 0, 2, 0, 0, 0, 0});
            }

            @Override
            public void onSwipeLeft() {
                handleTutorialSwipe(grid, initialGrid, new int[]{2, 0, 0, 0, 4, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0});
            }

            @Override
            public void onSwipeTop() {
                handleTutorialSwipe(grid, initialGrid, new int[]{0, 2, 4, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
            }

            @Override
            public void onSwipeBottom() {
                handleTutorialSwipe(grid, initialGrid, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 4, 2});
            }
        });

        mainLayout.addView(grid);
        mainLayout.addView(tvIndication);
        container.addView(mainLayout);
    }

    private void handleTutorialSwipe(GridLayout grid, int[] initialGrid, int[] newState) {
        applyStateToGrid(grid, newState);

        stopCurrentAnimation();
        currentAnimation = () -> applyStateToGrid(grid, initialGrid);
        tutoHandler.postDelayed(currentAnimation, 1000);
    }

    private void buildStep3(FrameLayout container) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        TextView t1 = createMiniCell();
        TextView t2 = createMiniCell();
        TextView arrow = new TextView(this);
        arrow.setText(" → ");
        arrow.setTextSize(24f);
        arrow.setTextColor(getThemeColor(R.attr.textColor));
        TextView tResult = createMiniCell();

        row.addView(t1);
        row.addView(t2);
        row.addView(arrow);
        row.addView(tResult);
        container.addView(row);

        int[][] fusions = {{2, 4}, {4, 8}, {8, 16}, {16, 32}, {32, 64}};

        currentAnimation = new Runnable() {
            int step = 0;

            @Override
            public void run() {
                int baseVal = fusions[step][0];
                int resultVal = fusions[step][1];

                styleMiniCell(t1, baseVal);
                styleMiniCell(t2, baseVal);
                t1.setScaleX(1);
                t1.setScaleY(1);
                t2.setScaleX(1);
                t2.setScaleY(1);

                tResult.setText("");
                tResult.setBackgroundColor(Color.TRANSPARENT);

                t1.animate().translationX(dpToPx(10)).setDuration(200).start();
                t2.animate().translationX(-dpToPx(10)).setDuration(200).withEndAction(() -> {
                    t1.setTranslationX(0);
                    t2.setTranslationX(0);
                    styleMiniCell(t1, 0);
                    styleMiniCell(t2, 0);

                    styleMiniCell(tResult, resultVal);
                    tResult.setScaleX(1.3f);
                    tResult.setScaleY(1.3f);
                    tResult.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                }).start();

                step = (step + 1) % fusions.length;
                tutoHandler.postDelayed(this, 1800);
            }
        };
        tutoHandler.post(currentAnimation);
    }

    private void buildStep4(FrameLayout container) {
        GridLayout grid = createMiniGrid();
        container.addView(grid);

        int[] baseGrid = {4, 0, 8, 2, 0, 16, 0, 4, 2, 0, 4, 0, 8, 32, 0, 2};
        applyStateToGrid(grid, baseGrid);

        int[] emptySpots = {1, 4, 6, 9, 11, 14};

        currentAnimation = new Runnable() {
            int spotIndex = 0;

            @Override
            public void run() {
                for (int spot : emptySpots) updateMiniGridCell(grid, spot, 0);

                int target = emptySpots[spotIndex];
                int newVal = Math.random() > 0.5 ? 4 : 2;
                updateMiniGridCell(grid, target, newVal);

                View v = grid.getChildAt(target);
                v.setScaleX(0);
                v.setScaleY(0);
                v.animate().scaleX(1).scaleY(1).setDuration(300).start();

                spotIndex = (spotIndex + 1) % emptySpots.length;
                tutoHandler.postDelayed(this, 1200);
            }
        };
        tutoHandler.post(currentAnimation);
    }

    private void buildStep5(FrameLayout container) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        LinearLayout colWin = new LinearLayout(this);
        colWin.setOrientation(LinearLayout.VERTICAL);
        colWin.setGravity(Gravity.CENTER);

        TextView titleWin = new TextView(this);
        titleWin.setText("VICTOIRE");
        titleWin.setTextSize(10f);
        titleWin.setTextColor(getThemeColor(R.attr.primaryColor));
        titleWin.setGravity(Gravity.CENTER);
        titleWin.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView cellWin = new TextView(this);
        LinearLayout.LayoutParams winParams = new LinearLayout.LayoutParams(dpToPx(54), dpToPx(54));
        winParams.setMargins(0, dpToPx(6), 0, 0);
        cellWin.setLayoutParams(winParams);
        cellWin.setGravity(Gravity.CENTER);
        cellWin.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.outfit_black));
        styleMiniCell(cellWin, 2048);

        colWin.addView(titleWin);
        colWin.addView(cellWin);

        TextView vs = new TextView(this);
        vs.setText("  VS  ");
        vs.setTextSize(16f);
        vs.setTextColor(getThemeColor(R.attr.textColor3));
        vs.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.outfit_bold));

        LinearLayout colLoss = new LinearLayout(this);
        colLoss.setOrientation(LinearLayout.VERTICAL);
        colLoss.setGravity(Gravity.CENTER);

        TextView titleLoss = new TextView(this);
        titleLoss.setText("DÉFAITE");
        titleLoss.setTextSize(10f);
        titleLoss.setTextColor(getThemeColor(R.attr.dangerColor));
        titleLoss.setGravity(Gravity.CENTER);
        titleLoss.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        GridLayout gridLoss = new GridLayout(this);
        gridLoss.setColumnCount(4);
        gridLoss.setRowCount(4);
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        gridParams.setMargins(0, dpToPx(6), 0, 0);
        gridLoss.setLayoutParams(gridParams);

        int[] lossData = {2, 4, 8, 2, 16, 2, 4, 32, 4, 8, 2, 4, 8, 4, 16, 8};
        for (int val : lossData) {
            TextView c = new TextView(this);
            c.setGravity(Gravity.CENTER);
            c.setText(String.valueOf(val));
            c.setTextSize(10f);
            c.setTextColor(Color.WHITE);
            c.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.outfit_black));

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(TileTheme.getBackgroundColor(this, val));
            bg.setCornerRadius(dpToPx(3));
            c.setBackground(bg);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dpToPx(20);
            params.height = dpToPx(20);
            params.setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            c.setLayoutParams(params);
            gridLoss.addView(c);
        }

        colLoss.addView(titleLoss);
        colLoss.addView(gridLoss);

        row.addView(colWin);
        row.addView(vs);
        row.addView(colLoss);
        container.addView(row);
    }

    private GridLayout createMiniGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setRowCount(4);
        grid.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        for (int i = 0; i < 16; i++) {
            grid.addView(createMiniCell());
        }
        return grid;
    }

    private TextView createMiniCell() {
        TextView cell = new TextView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = dpToPx(48);
        params.height = dpToPx(48);
        params.setMargins(dpToPx(3), dpToPx(3), dpToPx(3), dpToPx(3));
        cell.setLayoutParams(params);
        cell.setGravity(Gravity.CENTER);
        cell.setTypeface(ResourcesCompat.getFont(this, R.font.outfit_black));
        styleMiniCell(cell, 0);
        return cell;
    }

    private void styleMiniCell(TextView cell, int value) {
        if (value > 0) {
            cell.setText(String.valueOf(value));
            cell.setTextSize(value > 100 ? 14f : 18f);
        } else {
            cell.setText("");
        }
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(6));
        bg.setColor(TileTheme.getBackgroundColor(this, value));
        cell.setBackground(bg);
        cell.setTextColor(TileTheme.getTextColor(this, value));
    }

    private void updateMiniGridCell(GridLayout grid, int index, int value) {
        TextView cell = (TextView) grid.getChildAt(index);
        styleMiniCell(cell, value);
    }

    private void applyStateToGrid(GridLayout grid, int[] state) {
        for (int i = 0; i < 16; i++) updateMiniGridCell(grid, i, state[i]);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private int getThemeColor(int attrResId) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attrResId, typedValue, true);
        return typedValue.data;
    }
}