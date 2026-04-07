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

public class ScoresAdapter extends RecyclerView.Adapter<ScoresAdapter.ScoreViewHolder> {

    private List<GameWithPlayer> games = new ArrayList<>();
    private final Context context;

    public ScoresAdapter(Context context) {
        this.context = context;
    }

    public void setGames(List<GameWithPlayer> games) {
        this.games = games;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score_row, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        GameWithPlayer gameWithPlayer = games.get(position);
        Game game = gameWithPlayer.game;
        Player player = gameWithPlayer.player;
        int rank = position + 1;

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

        String[] avatars = {"😎", "🎮", "🦊", "🐉", "⭐", "🔥", "💎", "🚀", "🎯", "👑"};
        holder.tvAvatar.setText(position < avatars.length ? avatars[position] : "🎮");

        // Infos partie
        holder.tvGameInfo.setText("Tuile max : " + game.getBiggestTile() + " · " + game.getNbMove() + " coups");
        holder.tvScore.setText(String.format(Locale.getDefault(), "%,d", game.getScore()));
        holder.tvBiggestTile.setText(String.valueOf(game.getBiggestTile()));

        holder.tvPlayerName.setText(player != null ? player.getName() : "Anonyme");
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

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
