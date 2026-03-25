package com.example.a2048_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {


    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton buttonNewGame = view.findViewById(R.id.newGame_button);

        buttonNewGame.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), GameActivity.class);
            intent.putExtra("grid_size", 4);
            intent.putExtra("game_mode", "classique");
            startActivity(intent);
        });

    }
}