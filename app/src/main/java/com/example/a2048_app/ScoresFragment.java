package com.example.a2048_app;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2048_app.DbEntity.Game;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;

public class ScoresFragment extends Fragment {

    private ScoresViewModel viewModel;
    private ScoresAdapter adapter;

    public ScoresFragment() {
        super(R.layout.fragment_scores);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ScoresViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_scores);
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

        viewModel.getLeaderboard().observe(getViewLifecycleOwner(), games -> {
            adapter.setGames(games);
        });

        view.findViewById(R.id.btn_reset_scores).setOnClickListener(v -> {
            viewModel.deleteAll();
        });

        // Gestion des Onglets
        setupTabs(view);
    }

    private void setupTabs(View view) {
        TextView tabClassic = view.findViewById(R.id.tab_classic);
        TextView tabVersus  = view.findViewById(R.id.tab_versus);
        TextView tabDefi    = view.findViewById(R.id.tab_defi);

        View.OnClickListener tabListener = v -> {
            // Reset tous les onglets
            tabClassic.setBackgroundColor(requireContext().getColor(android.R.color.transparent));
            tabVersus.setBackgroundColor(requireContext().getColor(android.R.color.transparent));
            tabDefi.setBackgroundColor(requireContext().getColor(android.R.color.transparent));

            tabClassic.setTextColor(getThemeColor(R.attr.textColor3));
            tabVersus.setTextColor(getThemeColor(R.attr.textColor3));
            tabDefi.setTextColor(getThemeColor(R.attr.textColor3));

            // Active l'onglet cliqué
            ((TextView) v).setBackgroundColor(getThemeColor(R.attr.primaryColor));
            ((TextView) v).setTextColor(Color.WHITE);
        };

        tabClassic.setOnClickListener(tabListener);
        tabVersus.setOnClickListener(tabListener);
        tabDefi.setOnClickListener(tabListener);
    }

    private int getThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
}