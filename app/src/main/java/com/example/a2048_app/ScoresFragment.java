package com.example.a2048_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2048_app.DbEntity.GameWithPlayer;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.List;

public class ScoresFragment extends Fragment {

    private ScoresViewModel viewModel;
    private ScoresAdapter adapter;

    private int currentModeId = 1;
    private int currentTaille = 4;
    private LiveData<List<GameWithPlayer>> currentLeaderboardLiveData;

    private RecyclerView recyclerView;
    private LinearLayout layoutEmptyState;
    private View btnResetScores;

    private TextView textviewClassmentsTitre;

    public ScoresFragment() {
        super(R.layout.fragment_scores);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ScoresViewModel.class);

        recyclerView = view.findViewById(R.id.recycler_scores);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnResetScores = view.findViewById(R.id.btn_reset_scores);
        textviewClassmentsTitre = view.findViewById(R.id.textviewClassmentsTitre);

        adapter = new ScoresAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount - 1; i++) {
                    View child = parent.getChildAt(i);
                    int top = child.getBottom();
                    int bottom = top + 2;

                    Paint paint = new Paint();
                    paint.setColor(getThemeColor(R.attr.borderColor));
                    canvas.drawRect(0, top, parent.getWidth(), bottom, paint);
                }
            }
        });

        btnResetScores.setOnClickListener(v -> {
            viewModel.deleteAll();
        });

        setupFilters(view);
    }

    private void setupFilters(View view) {
        MaterialButtonToggleGroup modeToggleGroup = view.findViewById(R.id.toggleGroupModeScores);
        MaterialButtonToggleGroup tailleToggleGroup = view.findViewById(R.id.toggleGroupTailleScores);

        SharedPreferences prefs = requireActivity().getSharedPreferences("2048_settings", Context.MODE_PRIVATE);
        currentModeId = prefs.getInt("scores_page_mode_selected", 1);
        currentTaille = prefs.getInt("scores_page_taille_selected", 4);

        if (currentModeId == 1) modeToggleGroup.check(R.id.toggleButtonClassiqueScores);
        else if (currentModeId == 2) modeToggleGroup.check(R.id.toggleButtonMultijoueurScores);
        else if (currentModeId == 3) modeToggleGroup.check(R.id.toggleButtonDefiScores);

        if (currentTaille == 3) tailleToggleGroup.check(R.id.toggleButton3x3Scores);
        else if (currentTaille == 4) tailleToggleGroup.check(R.id.toggleButton4x4Scores);
        else if (currentTaille == 5) tailleToggleGroup.check(R.id.toggleButton5x5Scores);
        else if (currentTaille == 6) tailleToggleGroup.check(R.id.toggleButton6x6Scores);

        updateLeaderboardDisplay();

        modeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.toggleButtonClassiqueScores) currentModeId = 1;
                else if (checkedId == R.id.toggleButtonMultijoueurScores) currentModeId = 2;
                else if (checkedId == R.id.toggleButtonDefiScores) currentModeId = 3;

                prefs.edit().putInt("scores_page_mode_selected", currentModeId).apply();
                updateLeaderboardDisplay();
            }
        });

        tailleToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.toggleButton3x3Scores) currentTaille = 3;
                else if (checkedId == R.id.toggleButton4x4Scores) currentTaille = 4;
                else if (checkedId == R.id.toggleButton5x5Scores) currentTaille = 5;
                else if (checkedId == R.id.toggleButton6x6Scores) currentTaille = 6;

                prefs.edit().putInt("scores_page_taille_selected", currentTaille).apply();
                updateLeaderboardDisplay();
            }
        });
    }

    private void updateLeaderboardDisplay() {
        if (currentLeaderboardLiveData != null) {
            currentLeaderboardLiveData.removeObservers(getViewLifecycleOwner());
        }

        currentLeaderboardLiveData = viewModel.getLeaderboard(currentTaille, currentModeId);

        currentLeaderboardLiveData.observe(getViewLifecycleOwner(), games -> {
            if (games == null || games.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);
                btnResetScores.setVisibility(View.GONE); // On cache le bouton reset car il n'y a rien à supprimer
                textviewClassmentsTitre.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
                btnResetScores.setVisibility(View.VISIBLE);
                textviewClassmentsTitre.setVisibility(View.VISIBLE);
            }

            adapter.setGames(games);
        });
    }

    private int getThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
}