package com.example.a2048_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2048_app.DbEntity.Game;
import com.example.a2048_app.DbEntity.GameWithPlayer;
import com.example.a2048_app.DbEntity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adaptateur pour le RecyclerView affichant le tableau des scores.
 * Son rôle est de faire le lien entre la liste de données (GameWithPlayer)
 * et les vues XML individuelles (item_score_row) à afficher à l'écran.
 */
public class ScoresAdapter extends RecyclerView.Adapter<ScoresAdapter.ScoreViewHolder> {

    private List<GameWithPlayer> games = new ArrayList<>();

    public ScoresAdapter(Context context) {
        // Constructeur
    }

    /**
     * Met à jour la liste des scores et rafraîchit l'affichage.
     * notifyDataSetChanged() indique au RecyclerView qu'il doit se redessiner entièrement.
     */
    public void setGames(List<GameWithPlayer> games) {
        this.games = games;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // On "gonfle" (inflate) le layout XML d'une ligne de score pour en créer une vue Java
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score_row, parent, false);
        return new ScoreViewHolder(view);
    }

    /**
     * Remplit les composants d'une ligne de score avec les données d'une partie.
     * Cette méthode est appelée à chaque fois qu'une ligne devient visible à l'écran.
     */
    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        GameWithPlayer gameWithPlayer = games.get(position);
        Game game = gameWithPlayer.game;
        Player player = gameWithPlayer.player;
        int rank = position + 1; // Le rang commence à 1 (position + 1)

        // --- Gestion visuelle du podium ---
        switch (rank) {
            case 1:
                holder.tvRank.setText("🥇");
                break;
            case 2:
                holder.tvRank.setText("🥈");
                break;
            case 3:
                holder.tvRank.setText("🥉");
                break;
            default:
                holder.tvRank.setText(String.valueOf(rank));
                break;
        }

        // Attribution d'un avatar ludique en fonction de la position dans le classement
        String[] avatars = {"😎", "🎮", "🦊", "🐉", "⭐", "🔥", "💎", "🚀", "🎯", "👑"};
        holder.tvAvatar.setText(position < avatars.length ? avatars[position] : "🎮");

        // Formatage des informations de jeu
        holder.tvGameInfo.setText("Tuile max : " + game.getBiggestTile() + " · " + game.getNbMove() + " coups");

        // Formatage du score avec séparateur de milliers pour la lisibilité (ex: 10 000)
        holder.tvScore.setText(String.format(Locale.getDefault(), "%,d", game.getScore()));

        holder.tvBiggestTile.setText(String.valueOf(game.getBiggestTile()));

        // Gestion du pseudo (affiche "Anonyme" si le joueur n'a pas saisi de nom)
        holder.tvPlayerName.setText(player != null ? player.getName() : "Anonyme");
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    /**
     * Le ViewHolder est une boîte qui contient les références vers les TextView d'une ligne.
     * Cela évite d'appeler findViewById() de manière répétée, ce qui optimise
     * considérablement la fluidité du défilement (scrolling).
     */
    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvAvatar, tvPlayerName, tvGameInfo, tvScore, tvBiggestTile;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvPlayerName = itemView.findViewById(R.id.tv_player_name);
            tvGameInfo = itemView.findViewById(R.id.tv_game_info);
            tvScore = itemView.findViewById(R.id.tv_score);
            tvBiggestTile = itemView.findViewById(R.id.tv_biggest_tile);
        }
    }
}