package com.example.a2048_app;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.a2048_app.DbEntity.Game;
import com.example.a2048_app.DbEntity.Player;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Le "Cerveau" de l'interface de jeu.
 * Hériter de AndroidViewModel permet de conserver l'état de la partie (la grille)
 * même si l'activité est détruite et recréée (ex: rotation de l'écran).
 * L'interface graphique (GameActivity) se contente d'observer ce ViewModel.
 */
public class GameViewModel extends AndroidViewModel {

    // Utilisation de MutableLiveData pour que la modification de la grille notifie automatiquement la Vue
    private final MutableLiveData<Grid> currentGrid;

    // Un pool de threads dédié pour exécuter les requêtes de base de données (Room)
    // en arrière-plan, évitant ainsi de figer (freeze) l'interface utilisateur.
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AppDatabase db;

    /**
     * Constructeur utilisé lors du lancement d'une TOUTE NOUVELLE partie.
     */
    public GameViewModel(@NonNull Application application, int gridSize) {
        super(application);
        this.currentGrid = new MutableLiveData<>(new Grid(gridSize));
        db = AppDatabase.getInstance(application);
    }

    /**
     * Constructeur utilisé lors de la RESTAURATION d'une partie (depuis les SharedPreferences).
     */
    public GameViewModel(@NonNull Application application, Grid savedGrid) {
        super(application);
        this.currentGrid = new MutableLiveData<>(savedGrid);
        db = AppDatabase.getInstance(application);
    }

    public LiveData<Grid> getCurrentGrid() {
        return this.currentGrid;
    }

    public void resetGrid(int size) {
        currentGrid.setValue(new Grid(size));
    }

    // --- Gestion des mouvements (Logique Métier) ---
    // Chaque méthode appelle la logique de la classe Grid.
    // Si la grille a effectivement changé, on force la mise à jour du LiveData (setValue)
    // pour déclencher le rafraîchissement visuel dans l'Activity.

    public boolean rightSlide() {
        boolean hasMoved = Objects.requireNonNull(this.currentGrid.getValue()).rightSlide();
        if (hasMoved) {
            this.currentGrid.setValue(this.currentGrid.getValue());
        }
        return hasMoved;
    }

    public boolean leftSlide() {
        boolean hasMoved = Objects.requireNonNull(this.currentGrid.getValue()).leftSlide();
        if (hasMoved) {
            this.currentGrid.setValue(this.currentGrid.getValue());
        }
        return hasMoved;
    }

    public boolean upSlide() {
        boolean hasMoved = Objects.requireNonNull(this.currentGrid.getValue()).upSlide();
        if (hasMoved) {
            this.currentGrid.setValue(this.currentGrid.getValue());
        }
        return hasMoved;
    }

    public boolean downSlide() {
        boolean hasMoved = Objects.requireNonNull(this.currentGrid.getValue()).downSlide();
        if (hasMoved) {
            this.currentGrid.setValue(this.currentGrid.getValue());
        }
        return hasMoved;
    }

    // --- Gestion de la Base de Données (Room) ---

    /**
     * Sauvegarde une partie de manière asynchrone pour ne pas bloquer le Thread Principal (UI Thread).
     */
    public void saveGame(Game game) {
        executor.execute(() -> {
            db.gameDao().insert(game);
        });
    }

    /**
     * Récupère un joueur par son pseudo.
     * Bien que l'exécution se fasse en arrière-plan, l'utilisation de postValue()
     * permet de renvoyer le résultat de manière sécurisée vers le Thread Principal.
     */
    public LiveData<Player> getPlayerByName(String name) {
        MutableLiveData<Player> playerLiveData = new MutableLiveData<>();
        executor.execute(() -> {
            Player player = db.playerDao().getByName(name);
            playerLiveData.postValue(player);
        });
        return playerLiveData;
    }

    public LiveData<List<Player>> getAllPlayers() {
        return db.playerDao().getAll();
    }

    /**
     * Gère la création d'un nouveau joueur ET la sauvegarde de sa partie dans la foulée.
     * Tout s'exécute dans le même bloc asynchrone pour s'assurer que le joueur est créé
     * avant de tenter d'utiliser son ID comme clé étrangère pour la partie.
     */
    public void insertPlayerAndSaveGame(String name, Grid grid, int modeId) {
        executor.execute(() -> {
            long newPlayerId = db.playerDao().insert(new Player(name));
            Log.d("GameViewModel", "Nouveau joueur créé avec l'ID : " + newPlayerId);

            // On utilise bien le modeId passé en paramètre (Classique = 1, Multijoueur = 2, Défi = 3)
            Game game = new Game(grid.getScore(), grid.getSize(), grid.getMaxTile(), grid.getNbMove(), (int) newPlayerId, modeId);
            long gameId = db.gameDao().insert(game);
            Log.d("GameViewModel", "Partie sauvegardée avec l'ID : " + gameId);
        });
    }
}