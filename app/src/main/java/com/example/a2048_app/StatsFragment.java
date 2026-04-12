package com.example.a2048_app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.a2048_app.DbDao.GameDao;
import com.example.a2048_app.DbEntity.Game;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.OutputStream;

/**
 * Fragment gérant l'affichage des statistiques détaillées et le partage des scores.
 * Il utilise de nombreux LiveData pour observer les agrégats de données (comptages, moyennes)
 * fournis par la base de données Room.
 */
public class StatsFragment extends Fragment {

    private GameDao gameDao;

    // Layouts pour la gestion de l'état vide
    private LinearLayout layoutStatsContent;
    private LinearLayout layoutEmptyState;

    // Vues pour les records et compteurs
    private TextView textViewBestScore, textViewGridSizeBestScore;
    private TextView textViewNbGamePlayed, textViewNbVictories, textViewNbDefeats, textViewNbCoups;

    // Barres de progression pour les ratios (Victoires, Paliers de tuiles)
    private TextView tvPctVictoires, tvPct1024, tvPct512, tvPctDefaites;
    private LinearProgressIndicator progressVictoires, progress1024, progress512, progressDefaites;

    private ChipGroup chipGroupMilestones;
    private TextView textViewShareScore, textViewShareSubtitle;

    // États locaux pour le calcul et le partage
    private int currentModeId = 1, currentTaille = 4;
    private int countTotalGames = 0, countVictoires = 0, count1024 = 0, count512 = 0, countDefaites = 0;
    private int currentBestScore = 0, currentMaxTile = 0;

    // Liste des LiveData pour observation (nécessaire pour nettoyer les observateurs lors des changements de filtres)
    private LiveData<Game> currentScoreLiveData;
    private LiveData<Integer> nbGamePlayedLiveData, nbVictoriesLiveData, nbDefeatsLiveData, nbCoupsLiveData;
    private LiveData<Integer> nb1024LiveData, nb512LiveData, maxTileLiveData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        initViews(view);
        setupDatabase(view);
        setupFilters(view);
        setupShareButtons(view);

        return view;
    }

    private void initViews(View view) {
        layoutStatsContent = view.findViewById(R.id.layout_stats_content);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        textViewBestScore = view.findViewById(R.id.textViewBestScore);
        textViewGridSizeBestScore = view.findViewById(R.id.textViewGridSizeBestScore);
        textViewNbGamePlayed = view.findViewById(R.id.textViewNbGamePlayed);
        textViewNbVictories = view.findViewById(R.id.textViewNbVictories);
        textViewNbDefeats = view.findViewById(R.id.textViewNbDefeats);
        textViewNbCoups = view.findViewById(R.id.textViewNbCoups);
        tvPctVictoires = view.findViewById(R.id.tv_pct_victoires);
        progressVictoires = view.findViewById(R.id.progress_victoires);
        tvPct1024 = view.findViewById(R.id.tv_pct_1024);
        progress1024 = view.findViewById(R.id.progress_1024);
        tvPct512 = view.findViewById(R.id.tv_pct_512);
        progress512 = view.findViewById(R.id.progress_512);
        tvPctDefaites = view.findViewById(R.id.tv_pct_defaites);
        progressDefaites = view.findViewById(R.id.progress_defaites);
        chipGroupMilestones = view.findViewById(R.id.chipGroupMilestones);
        textViewShareScore = view.findViewById(R.id.textViewShareScore);
        textViewShareSubtitle = view.findViewById(R.id.textViewShareSubtitle);
    }

    private void setupDatabase(View view) {
        gameDao = AppDatabase.getInstance(view.getContext()).gameDao();
    }

    /**
     * Initialise les boutons de filtres (Mode et Taille) et restaure les derniers choix.
     */
    private void setupFilters(View view) {
        MaterialButtonToggleGroup modeToggleGroup = view.findViewById(R.id.toggleGroupMode);
        MaterialButtonToggleGroup tailleToggleGroup = view.findViewById(R.id.toggleGroupTaille);
        SharedPreferences prefs = requireActivity().getSharedPreferences("2048_settings", Context.MODE_PRIVATE);

        currentModeId = prefs.getInt("stats_page_mode_selected", 1);
        currentTaille = prefs.getInt("stats_page_taille_selected", 4);

        // Sélection visuelle initiale
        if (currentModeId == 1) modeToggleGroup.check(R.id.toggleButtonClassique);
        if (currentTaille == 4) tailleToggleGroup.check(R.id.toggleButton4x4); // etc...

        updateStatisticsDisplay();

        modeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.toggleButtonClassique) currentModeId = 1;
                else if (checkedId == R.id.toggleButtonMultijoueur) currentModeId = 2;
                else if (checkedId == R.id.toggleButtonDefi) currentModeId = 3;
                prefs.edit().putInt("stats_page_mode_selected", currentModeId).apply();
                updateStatisticsDisplay();
            }
        });

        tailleToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.toggleButton3x3) currentTaille = 3;
                else if (checkedId == R.id.toggleButton4x4) currentTaille = 4;
                else if (checkedId == R.id.toggleButton5x5) currentTaille = 5;
                else if (checkedId == R.id.toggleButton6x6) currentTaille = 6;
                prefs.edit().putInt("stats_page_taille_selected", currentTaille).apply();
                updateStatisticsDisplay();
            }
        });
    }

    /**
     * Configure les différentes méthodes de partage (Texte, SMS, Image).
     */
    private void setupShareButtons(View view) {
        View cardSharePreviewBox = view.findViewById(R.id.cardSharePreviewBox);

        view.findViewById(R.id.btnShareCopy).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Score 2048", generateShareText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "Texte copié !", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnShareGeneral).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, generateShareText());
            startActivity(Intent.createChooser(intent, "Partager via..."));
        });

        view.findViewById(R.id.btnShareScreenshot).setOnClickListener(v -> takeAndSaveScreenshot(cardSharePreviewBox));
    }

    private String generateShareText() {
        return "Score 2048 : " + currentBestScore + " pts (Tuile : " + currentMaxTile + ") sur grille " + currentTaille + "x" + currentTaille + ". Peux-tu faire mieux ?";
    }

    /**
     * Transforme une vue XML en image Bitmap et l'enregistre dans la galerie de l'utilisateur.
     * Utilise le MediaStore pour assurer la compatibilité avec les versions récentes d'Android.
     */
    private void takeAndSaveScreenshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "2048_Record_" + System.currentTimeMillis() + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/2048");

        Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try (OutputStream out = (uri != null) ? requireContext().getContentResolver().openOutputStream(uri) : null) {
            if (out != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(requireContext(), "Capture sauvegardée ! \uD83D\uDCF8", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erreur lors de la capture", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Orchestre la récupération de toutes les statistiques selon les filtres.
     * Les données sont rafraîchies automatiquement dès que la base Room change.
     */
    private void updateStatisticsDisplay() {
        removeOldObservers();

        String modeName = (currentModeId == 1) ? "Classique" : (currentModeId == 2) ? "Multijoueur" : "Défi";

        // Initialisation des LiveData depuis le DAO
        currentScoreLiveData = gameDao.getBestScoreByGridSizeAndMode(currentTaille, currentModeId);
        nbGamePlayedLiveData = gameDao.getNbGamePlayedByGridSizeAndMode(currentTaille, currentModeId);
        nbVictoriesLiveData = gameDao.getNbVictoriesByGridSizeAndMode(currentTaille, currentModeId);
        nbDefeatsLiveData = gameDao.getNbDefeatsByGridSizeAndMode(currentTaille, currentModeId);
        nbCoupsLiveData = gameDao.getNbCoupsByGridSizeAndMode(currentTaille, currentModeId);
        nb1024LiveData = gameDao.getNbReached1024ByGridSizeAndMode(currentTaille, currentModeId);
        nb512LiveData = gameDao.getNbReached512ByGridSizeAndMode(currentTaille, currentModeId);
        maxTileLiveData = gameDao.getMaxTileByGridSizeAndMode(currentTaille, currentModeId);

        // Observation des résultats
        currentScoreLiveData.observe(getViewLifecycleOwner(), game -> {
            currentBestScore = (game != null) ? game.getScore() : 0;
            textViewBestScore.setText(String.valueOf(currentBestScore));
            textViewGridSizeBestScore.setText(String.format("Grille %d×%d · %s", currentTaille, currentTaille, modeName));
            refreshSharePreview();
        });

        nbGamePlayedLiveData.observe(getViewLifecycleOwner(), nb -> {
            countTotalGames = (nb != null) ? nb : 0;
            textViewNbGamePlayed.setText(String.valueOf(countTotalGames));

            // Gestion de l'UI si aucune partie n'est enregistrée
            layoutStatsContent.setVisibility(countTotalGames == 0 ? View.GONE : View.VISIBLE);
            layoutEmptyState.setVisibility(countTotalGames == 0 ? View.VISIBLE : View.GONE);

            refreshProgressBars();
            refreshSharePreview();
        });

        nbVictoriesLiveData.observe(getViewLifecycleOwner(), nb -> {
            countVictoires = (nb != null) ? nb : 0;
            textViewNbVictories.setText(String.valueOf(countVictoires));
            refreshProgressBars();
        });

        nbDefeatsLiveData.observe(getViewLifecycleOwner(), nb -> {
            countDefaites = (nb != null) ? nb : 0;
            textViewNbDefeats.setText(String.valueOf(countDefaites));
            refreshProgressBars();
        });

        nbCoupsLiveData.observe(getViewLifecycleOwner(), nb -> textViewNbCoups.setText(nb != null ? formatCompactNumber(nb) : "0"));

        nb1024LiveData.observe(getViewLifecycleOwner(), nb -> {
            count1024 = (nb != null) ? nb : 0;
            refreshProgressBars();
        });

        nb512LiveData.observe(getViewLifecycleOwner(), nb -> {
            count512 = (nb != null) ? nb : 0;
            refreshProgressBars();
        });

        maxTileLiveData.observe(getViewLifecycleOwner(), maxTile -> {
            currentMaxTile = (maxTile != null) ? maxTile : 0;
            populateMilestones(currentMaxTile);
            refreshSharePreview();
        });
    }

    private void removeOldObservers() {
        if (currentScoreLiveData != null)
            currentScoreLiveData.removeObservers(getViewLifecycleOwner());
        if (nbGamePlayedLiveData != null)
            nbGamePlayedLiveData.removeObservers(getViewLifecycleOwner());
        if (nbVictoriesLiveData != null)
            nbVictoriesLiveData.removeObservers(getViewLifecycleOwner());
        if (nbDefeatsLiveData != null) nbDefeatsLiveData.removeObservers(getViewLifecycleOwner());
        if (nbCoupsLiveData != null) nbCoupsLiveData.removeObservers(getViewLifecycleOwner());
        if (nb1024LiveData != null) nb1024LiveData.removeObservers(getViewLifecycleOwner());
        if (nb512LiveData != null) nb512LiveData.removeObservers(getViewLifecycleOwner());
        if (maxTileLiveData != null) maxTileLiveData.removeObservers(getViewLifecycleOwner());
    }

    private void refreshSharePreview() {
        textViewShareScore.setText(String.valueOf(currentBestScore));
        textViewShareSubtitle.setText(String.format("pts · Tuile max : %d · %d parties", currentMaxTile, countTotalGames));
    }

    /**
     * Génère dynamiquement des badges (Chips) pour chaque palier de tuile (2, 4, 8... 2048...).
     * Les badges sont colorés si le joueur a déjà atteint ce palier.
     */
    private void populateMilestones(int highestTile) {
        chipGroupMilestones.removeAllViews();
        int maxTarget = Math.max(2048, highestTile);
        int colorOk = MaterialColors.getColor(chipGroupMilestones, R.attr.badgeTextColor, android.graphics.Color.BLACK);
        int colorNo = MaterialColors.getColor(chipGroupMilestones, R.attr.textColor3, android.graphics.Color.GRAY);

        for (int i = 2; i <= maxTarget; i *= 2) {
            TextView tv = new TextView(requireContext());
            tv.setText(String.valueOf(i));
            tv.setTextSize(12f);
            tv.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.outfit_bold));
            tv.setPadding(30, 15, 30, 15);

            boolean reached = i <= highestTile;
            tv.setBackgroundResource(reached ? R.drawable.bg_milestone_ok : R.drawable.bg_milestone_no);
            tv.setTextColor(reached ? colorOk : colorNo);

            chipGroupMilestones.addView(tv);
        }
    }

    private void refreshProgressBars() {
        updateSingleProgressBar(progressVictoires, tvPctVictoires, countVictoires);
        updateSingleProgressBar(progress1024, tvPct1024, count1024);
        updateSingleProgressBar(progress512, tvPct512, count512);
        updateSingleProgressBar(progressDefaites, tvPctDefaites, countDefaites);
    }

    private void updateSingleProgressBar(LinearProgressIndicator indicator, TextView tv, int count) {
        int percentage = (countTotalGames == 0) ? 0 : (count * 100) / countTotalGames;
        indicator.setProgress(percentage);
        tv.setText(percentage + "%");
    }

    /**
     * Utilitaire pour afficher les grands nombres de manière lisible (ex: 1.5 k au lieu de 1500).
     */
    private String formatCompactNumber(int number) {
        if (number >= 1000000)
            return (number / 1000000) + "." + ((number % 1000000) / 100000) + " M";
        if (number >= 1000) return (number / 1000) + "." + ((number % 1000) / 100) + " k";
        return String.valueOf(number);
    }
}