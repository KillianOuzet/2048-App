package com.example.a2048_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.a2048_app.DbDao.GameDao;
import com.example.a2048_app.DbEntity.Game;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.google.android.material.color.MaterialColors;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.OutputStream;

public class StatsFragment extends Fragment {

    private GameDao gameDao;
    private TextView textViewBestScore;
    private TextView textViewGridSizeBestScore;

    private TextView textViewNbGamePlayed;

    private TextView textViewNbVictories;

    private TextView textViewNbDefeats;

    private TextView textViewNbCoups;

    private int currentModeId = 1;
    private int currentTaille = 4;

    private LiveData<Game> currentScoreLiveData;

    private LiveData<Integer> nbGamePlayedLiveData;

    private LiveData<Integer> nbVictoriesLiveData;

    private LiveData<Integer> nbDefeatsLiveData;

    private LiveData<Integer> nbCoupsLiveData;

    private TextView tvPctVictoires, tvPct1024, tvPct512, tvPctDefaites;
    private LinearProgressIndicator progressVictoires, progress1024, progress512, progressDefaites;

    private LiveData<Integer> nb1024LiveData;
    private LiveData<Integer> nb512LiveData;

    private int countTotalGames = 0;
    private int countVictoires = 0;
    private int count1024 = 0;
    private int count512 = 0;
    private int countDefaites = 0;

    private ChipGroup chipGroupMilestones;
    private LiveData<Integer> maxTileLiveData;

    private TextView textViewShareScore;
    private TextView textViewShareSubtitle;

    private int currentBestScore = 0;
    private int currentMaxTile = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        MaterialButtonToggleGroup modeToggleGroup = view.findViewById(R.id.toggleGroupMode);
        MaterialButtonToggleGroup tailleToggleGroup = view.findViewById(R.id.toggleGroupTaille);
        textViewBestScore = view.findViewById(R.id.textViewBestScore);
        textViewGridSizeBestScore = view.findViewById(R.id.textViewGridSizeBestScore);
        textViewNbGamePlayed = view.findViewById(R.id.textViewNbGamePlayed);
        textViewNbVictories = view.findViewById(R.id.textViewNbVictories);
        textViewNbDefeats = view.findViewById(R.id.textViewNbDefeats);
        textViewNbCoups = view.findViewById(R.id.textViewNbCoups);
        chipGroupMilestones = view.findViewById(R.id.chipGroupMilestones);
        textViewShareScore = view.findViewById(R.id.textViewShareScore);
        textViewShareSubtitle = view.findViewById(R.id.textViewShareSubtitle);

        tvPctVictoires = view.findViewById(R.id.tv_pct_victoires);
        progressVictoires = view.findViewById(R.id.progress_victoires);

        tvPct1024 = view.findViewById(R.id.tv_pct_1024);
        progress1024 = view.findViewById(R.id.progress_1024);

        tvPct512 = view.findViewById(R.id.tv_pct_512);
        progress512 = view.findViewById(R.id.progress_512);

        tvPctDefaites = view.findViewById(R.id.tv_pct_defaites);
        progressDefaites = view.findViewById(R.id.progress_defaites);

        chipGroupMilestones = view.findViewById(R.id.chipGroupMilestones);

        SharedPreferences prefs = requireActivity().getSharedPreferences("2048_settings", Context.MODE_PRIVATE);

        AppDatabase db = AppDatabase.getInstance(view.getContext());
        gameDao = db.gameDao();

        currentModeId = prefs.getInt("stats_page_mode_selected", 1);
        currentTaille = prefs.getInt("stats_page_taille_selected", 4);

        if (currentModeId == 1) modeToggleGroup.check(R.id.toggleButtonClassique);
        else if (currentModeId == 2) modeToggleGroup.check(R.id.toggleButtonMultijoueur);
        else if (currentModeId == 3) modeToggleGroup.check(R.id.toggleButtonDefi);

        if (currentTaille == 3) tailleToggleGroup.check(R.id.toggleButton3x3);
        else if (currentTaille == 4) tailleToggleGroup.check(R.id.toggleButton4x4);
        else if (currentTaille == 5) tailleToggleGroup.check(R.id.toggleButton5x5);
        else if (currentTaille == 6) tailleToggleGroup.check(R.id.toggleButton6x6);

        updateBestScoreDisplay();

        modeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.toggleButtonClassique) currentModeId = 1;
                else if (checkedId == R.id.toggleButtonMultijoueur) currentModeId = 2;
                else if (checkedId == R.id.toggleButtonDefi) currentModeId = 3;

                prefs.edit().putInt("stats_page_mode_selected", currentModeId).apply();
                updateBestScoreDisplay();
            }
        });

        tailleToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.toggleButton3x3) currentTaille = 3;
                else if (checkedId == R.id.toggleButton4x4) currentTaille = 4;
                else if (checkedId == R.id.toggleButton5x5) currentTaille = 5;
                else if (checkedId == R.id.toggleButton6x6) currentTaille = 6;

                prefs.edit().putInt("stats_page_taille_selected", currentTaille).apply();
                updateBestScoreDisplay();
            }
        });

        View cardSharePreviewBox = view.findViewById(R.id.cardSharePreviewBox);
        View btnShareScreenshot = view.findViewById(R.id.btnShareScreenshot);
        View btnShareGeneral = view.findViewById(R.id.btnShareGeneral);
        View btnShareMessage = view.findViewById(R.id.btnShareMessage);
        View btnShareCopy = view.findViewById(R.id.btnShareCopy);

        btnShareCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Score 2048", generateShareText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "Texte copié dans le presse-papiers !", Toast.LENGTH_SHORT).show();
        });

        btnShareGeneral.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, generateShareText());
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, "Partager mon score via...");
            startActivity(shareIntent);
        });

        btnShareMessage.setOnClickListener(v -> {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:"));
            smsIntent.putExtra("sms_body", generateShareText());
            startActivity(smsIntent);
        });

        btnShareScreenshot.setOnClickListener(v -> {
            takeAndSaveScreenshot(cardSharePreviewBox);
        });

        return view;
    }

    private String generateShareText() {
        return "J'ai marqué " + currentBestScore + " points au jeu 2048 (Tuile max : " + currentMaxTile + ") sur une grille " + currentTaille + "x" + currentTaille + " ! Peux-tu battre mon record ?";
    }

    private void takeAndSaveScreenshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "2048_Score_" + System.currentTimeMillis() + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/2048");

        Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try {
            if (uri != null) {
                OutputStream out = requireContext().getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
                Toast.makeText(requireContext(), "Capture sauvegardée dans la galerie 📸", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBestScoreDisplay() {
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
        if (maxTileLiveData != null) {
            maxTileLiveData.removeObservers(getViewLifecycleOwner());
        }

        String modeName = "Classique";
        if (currentModeId == 2) modeName = "Multijoueur";
        else if (currentModeId == 3) modeName = "Défi";

        String finalModeName = modeName;

        currentScoreLiveData = gameDao.getBestScoreByGridSizeAndMode(currentTaille, currentModeId);
        nbGamePlayedLiveData = gameDao.getNbGamePlayedByGridSizeAndMode(currentTaille, currentModeId);
        nbVictoriesLiveData = gameDao.getNbVictoriesByGridSizeAndMode(currentTaille, currentModeId);
        nbDefeatsLiveData = gameDao.getNbDefeatsByGridSizeAndMode(currentTaille, currentModeId);
        nbCoupsLiveData = gameDao.getNbCoupsByGridSizeAndMode(currentTaille, currentModeId);
        nb1024LiveData = gameDao.getNbReached1024ByGridSizeAndMode(currentTaille, currentModeId);
        nb512LiveData = gameDao.getNbReached512ByGridSizeAndMode(currentTaille, currentModeId);
        maxTileLiveData = gameDao.getMaxTileByGridSizeAndMode(currentTaille, currentModeId);

        currentScoreLiveData.observe(getViewLifecycleOwner(), game -> {
            if (game != null) {
                currentBestScore = game.getScore();
                textViewBestScore.setText(String.valueOf(game.getScore()));
                textViewGridSizeBestScore.setText(String.format("Grille %d×%d · Mode %s", game.getGridSize(), game.getGridSize(), finalModeName));
            } else {
                currentBestScore = 0;
                textViewBestScore.setText("0");
                textViewGridSizeBestScore.setText(String.format("Aucun score pour la grille %d×%d (%s)", currentTaille, currentTaille, finalModeName));
            }
            refreshSharePreview();
        });

        nbGamePlayedLiveData.observe(getViewLifecycleOwner(), nb -> {
            countTotalGames = (nb != null) ? nb : 0;
            textViewNbGamePlayed.setText(String.valueOf(countTotalGames));
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

        nbCoupsLiveData.observe(getViewLifecycleOwner(), nb -> {
            textViewNbCoups.setText(nb != null ? formatCompactNumber(nb) : "0");
        });

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

    private void refreshSharePreview() {
        textViewShareScore.setText(String.valueOf(currentBestScore));
        textViewShareSubtitle.setText(String.format("pts · Tuile max : %d · %d parties", currentMaxTile, countTotalGames));
    }

    private void populateMilestones(int highestTile) {
        chipGroupMilestones.removeAllViews();

        float density = getResources().getDisplayMetrics().density;
        int paddingH = (int) (14 * density + 0.5f);
        int paddingV = (int) (6 * density + 0.5f);

        int maxTarget = Math.max(4096, highestTile);

        int colorOk = MaterialColors.getColor(chipGroupMilestones, R.attr.badgeTextColor, android.graphics.Color.BLACK);
        int colorNo = MaterialColors.getColor(chipGroupMilestones, R.attr.textColor3, android.graphics.Color.GRAY);

        for (int i = 2; i <= maxTarget; i *= 2) {
            TextView tv = new TextView(requireContext());
            tv.setText(String.valueOf(i));
            tv.setTextSize(12f);

            tv.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.outfit_bold));
            tv.setPadding(paddingH, paddingV, paddingH, paddingV);

            if (i <= highestTile) {
                tv.setBackgroundResource(R.drawable.bg_milestone_ok);
                // On applique la couleur récupérée dynamiquement
                tv.setTextColor(colorOk);
            } else {
                tv.setBackgroundResource(R.drawable.bg_milestone_no);
                // On applique la couleur récupérée dynamiquement
                tv.setTextColor(colorNo);
            }

            chipGroupMilestones.addView(tv);
        }
    }

    private void refreshProgressBars() {
        updateSingleProgressBar(progressVictoires, tvPctVictoires, countVictoires);
        updateSingleProgressBar(progress1024, tvPct1024, count1024);
        updateSingleProgressBar(progress512, tvPct512, count512);
        updateSingleProgressBar(progressDefaites, tvPctDefaites, countDefaites);
    }

    private void updateSingleProgressBar(com.google.android.material.progressindicator.LinearProgressIndicator progressIndicator, TextView textView, int count) {
        if (countTotalGames == 0) {
            progressIndicator.setProgress(0);
            textView.setText("0%");
        } else {
            int percentage = (count * 100) / countTotalGames;

            progressIndicator.setProgress(percentage);
            textView.setText(percentage + "%");
        }
    }

    private String formatCompactNumber(int number) {
        if (number >= 1000000) {
            int m = number / 1000000;
            int decimal = (number % 1000000) / 100000;
            return decimal == 0 ? m + " M" : m + "," + decimal + " M";
        } else if (number >= 1000) {
            int k = number / 1000;
            int decimal = (number % 1000) / 100;
            return decimal == 0 ? k + " k" : k + "," + decimal + " k";
        } else {
            return String.valueOf(number);
        }
    }
}