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

/**
 * Fragment gérant l'affichage du tableau des scores (Leaderboard).
 * Il permet à l'utilisateur de filtrer les résultats par taille de grille et par mode
 * tout en offrant une gestion visuelle propre en cas d'absence de données.
 */
public class ScoresFragment extends Fragment {

    private ScoresViewModel viewModel;
    private ScoresAdapter adapter;

    // État actuel des filtres (mémorisé pour les requêtes)
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

        // Initialisation du ViewModel pour la logique de récupération des scores
        viewModel = new ViewModelProvider(this).get(ScoresViewModel.class);

        // Liaison des vues
        recyclerView = view.findViewById(R.id.recycler_scores);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnResetScores = view.findViewById(R.id.btn_reset_scores);
        textviewClassmentsTitre = view.findViewById(R.id.textviewClassmentsTitre);

        // Configuration du RecyclerView et de son Adaptateur
        adapter = new ScoresAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        /**
         * Ajout d'une décoration personnalisée au RecyclerView.
         * Plutôt que d'utiliser un simple divider XML, on dessine manuellement
         * une ligne de séparation entre chaque item pour un rendu visuel plus fin.
         */
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int childCount = parent.getChildCount();
                Paint paint = new Paint();
                paint.setColor(getThemeColor(R.attr.borderColor));

                // On dessine une ligne sous chaque élément sauf le dernier
                for (int i = 0; i < childCount - 1; i++) {
                    View child = parent.getChildAt(i);
                    int top = child.getBottom();
                    int bottom = top + 2; // Épaisseur de 2 pixels
                    canvas.drawRect(0, top, parent.getWidth(), bottom, paint);
                }
            }
        });

        // Action de suppression totale des scores (avec confirmation implicite)
        btnResetScores.setOnClickListener(v -> viewModel.deleteAll());

        setupFilters(view);
    }

    /**
     * Initialise et gère les groupes de boutons de filtrage (Modes et Tailles).
     * Les préférences de l'utilisateur sont sauvegardées dans les SharedPreferences
     * pour conserver les filtres actifs lors de la prochaine ouverture.
     */
    private void setupFilters(View view) {
        MaterialButtonToggleGroup modeToggleGroup = view.findViewById(R.id.toggleGroupModeScores);
        MaterialButtonToggleGroup tailleToggleGroup = view.findViewById(R.id.toggleGroupTailleScores);

        SharedPreferences prefs = requireActivity().getSharedPreferences("2048_settings", Context.MODE_PRIVATE);

        // Chargement des derniers filtres utilisés
        currentModeId = prefs.getInt("scores_page_mode_selected", 1);
        currentTaille = prefs.getInt("scores_page_taille_selected", 4);

        // Mise à jour visuelle des boutons Toggle au démarrage
        updateToggleSelection(modeToggleGroup, tailleToggleGroup);

        updateLeaderboardDisplay();

        // Écouteur pour le changement de Mode
        modeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.toggleButtonClassiqueScores) currentModeId = 1;
                else if (checkedId == R.id.toggleButtonMultijoueurScores) currentModeId = 2;
                else if (checkedId == R.id.toggleButtonDefiScores) currentModeId = 3;

                prefs.edit().putInt("scores_page_mode_selected", currentModeId).apply();
                updateLeaderboardDisplay();
            }
        });

        // Écouteur pour le changement de Taille de grille
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

    /**
     * Synchronise les boutons de sélection (Toggle) avec l'état actuel.
     */
    private void updateToggleSelection(MaterialButtonToggleGroup modeGroup, MaterialButtonToggleGroup tailleGroup) {
        if (currentModeId == 1) modeGroup.check(R.id.toggleButtonClassiqueScores);
        else if (currentModeId == 2) modeGroup.check(R.id.toggleButtonMultijoueurScores);
        else if (currentModeId == 3) modeGroup.check(R.id.toggleButtonDefiScores);

        if (currentTaille == 3) tailleGroup.check(R.id.toggleButton3x3Scores);
        else if (currentTaille == 5) tailleGroup.check(R.id.toggleButton5x5Scores);
        else if (currentTaille == 6) tailleGroup.check(R.id.toggleButton6x6Scores);
        else tailleGroup.check(R.id.toggleButton4x4Scores);
    }

    /**
     * Interroge le ViewModel pour obtenir le LiveData correspondant aux filtres choisis.
     * Gère également l'affichage du "Empty State" si aucun score n'est trouvé.
     */
    private void updateLeaderboardDisplay() {
        // On nettoie l'ancien observateur s'il existe pour éviter les fuites ou les doubles appels
        if (currentLeaderboardLiveData != null) {
            currentLeaderboardLiveData.removeObservers(getViewLifecycleOwner());
        }

        currentLeaderboardLiveData = viewModel.getLeaderboard(currentTaille, currentModeId);

        currentLeaderboardLiveData.observe(getViewLifecycleOwner(), games -> {
            // Gestion de l'UI si la liste est vide (Empty State)
            boolean isEmpty = (games == null || games.isEmpty());

            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            btnResetScores.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            textviewClassmentsTitre.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

            adapter.setGames(games);
        });
    }

    /**
     * Utilitaire pour récupérer une couleur de thème dynamiquement.
     */
    private int getThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
}