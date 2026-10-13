
package co.edu.unal.tictactoe;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;

public class LobbyActivity extends Activity {

    private static final String TAG = "TriquiOnline";

    private Button mCreateGameButton;
    private ListView mGamesList;
    private TextView mLobbyStatus;

    private FirebaseAuth mAuth;
    private DatabaseReference mGamesRef;
    private String mPlayerId;

    private ArrayAdapter<String> mAdapter;
    private final ArrayList<String> mGameIds = new ArrayList<>();

    private Query mWaitingGamesQuery;
    private ValueEventListener mGamesListener;
    private final ArrayList<String> mGameCreatorIds = new ArrayList<>();
    private DatabaseReference mMyCreatedGameRef;
    private ValueEventListener mMyCreatedGameListener;
    private boolean mOpeningGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        mCreateGameButton = findViewById(R.id.create_game_button);
        mGamesList = findViewById(R.id.games_list);
        mLobbyStatus = findViewById(R.id.lobby_status);

        mAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );

        mGamesList.setAdapter(mAdapter);

        mGamesList.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < mGameIds.size()) {
                joinGame(
                        mGameIds.get(position),
                        mGameCreatorIds.get(position)
                );
            }
        });

        mCreateGameButton.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();

        mGamesRef = FirebaseDatabase
                .getInstance("https://tictactoeandroid-53171-default-rtdb.firebaseio.com/")
                .getReference("games");

        mCreateGameButton.setOnClickListener(v -> createGame());

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mPlayerId = currentUser.getUid();
            prepareLobby();
        } else {
            mLobbyStatus.setText("Identificando jugador...");

            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                mPlayerId = user.getUid();
                                prepareLobby();
                            } else {
                                showError("No se pudo identificar al jugador.");
                            }
                        } else {
                            Log.e(TAG, "Error de autenticación",
                                    task.getException());
                            showError("No se pudo iniciar sesión en Firebase.");
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOpeningGame = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);

        // En el lobby no tienen sentido estas opciones.
        menu.findItem(R.id.new_game).setVisible(false);
        menu.findItem(R.id.difficulty).setVisible(false);
        menu.findItem(R.id.reset_scores).setVisible(false);
        menu.findItem(R.id.about).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_menu) {
            Intent intent = new Intent(
                    LobbyActivity.this,
                    MainMenuActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void prepareLobby() {
        mCreateGameButton.setEnabled(true);
        mLobbyStatus.setText("Conectado. Buscando partidas...");
        Log.d(TAG, "Jugador identificado: " + mPlayerId);

        listenForWaitingGames();
    }

    private void listenForWaitingGames() {
        mWaitingGamesQuery = mGamesRef
                .orderByChild("status")
                .equalTo("waiting");

        mGamesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mAdapter.clear();
                mGameIds.clear();
                mGameCreatorIds.clear();

                for (DataSnapshot gameSnapshot : snapshot.getChildren()) {
                    String gameId = gameSnapshot.getKey();
                    String creatorUid = gameSnapshot
                            .child("creatorUid")
                            .getValue(String.class);

                    if (gameId != null && creatorUid != null) {
                        mGameIds.add(gameId);
                        mGameCreatorIds.add(creatorUid);

                        String shortId = creatorUid.substring(
                                0, Math.min(6, creatorUid.length())
                        );

                        mAdapter.add("Partida de " + shortId
                                + " — esperando jugador");
                    }
                }

                if (mGameIds.isEmpty()) {
                    mLobbyStatus.setText(
                            "No hay partidas disponibles. ¡Crea una!"
                    );
                } else {
                    mLobbyStatus.setText(
                            "Partidas disponibles: " + mGameIds.size()
                    );
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error consultando partidas: "
                        + error.getMessage());
                showError("No se pudieron cargar las partidas.");
            }
        };

        mWaitingGamesQuery.addValueEventListener(mGamesListener);
    }

    private void createGame() {
        if (mPlayerId == null) {
            showError("Espera a que se identifique al jugador.");
            return;
        }

        mCreateGameButton.setEnabled(false);
        mLobbyStatus.setText("Creando partida...");

        DatabaseReference newGameRef = mGamesRef.push();

        Map<String, Object> game = new HashMap<>();
        game.put("creatorUid", mPlayerId);
        game.put("status", "waiting");
        game.put("createdAt", ServerValue.TIMESTAMP);
        game.put("board", "---------");
        game.put("turn", "X");
        game.put("winner", "none");

        newGameRef.setValue(game)
                .addOnSuccessListener(unused -> {
                    mLobbyStatus.setText(
                            "Partida creada. Esperando al segundo jugador."
                    );
                    mCreateGameButton.setEnabled(true);

                    waitForOpponent(newGameRef);
                })
                .addOnFailureListener(error -> {
                    Log.e(TAG, "Error creando partida", error);
                    showError("No se pudo crear la partida: "
                            + error.getLocalizedMessage());
                    mCreateGameButton.setEnabled(true);
                });
    }


    private void waitForOpponent(DatabaseReference gameRef) {
        mMyCreatedGameRef = gameRef;

        mMyCreatedGameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String status = snapshot.child("status")
                        .getValue(String.class);

                String player2Uid = snapshot.child("player2Uid")
                        .getValue(String.class);

                if ("playing".equals(status) && player2Uid != null) {
                    openGame(gameRef.getKey(), true);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error esperando al segundo jugador",
                        error.toException());

                showError("No se pudo comprobar el estado de la partida.");
            }
        };

        gameRef.addValueEventListener(mMyCreatedGameListener);
    }



    private void joinGame(String gameId, String creatorUid) {
        if (mPlayerId == null) {
            showError("Espera a que se identifique al jugador.");
            return;
        }

        if (mPlayerId.equals(creatorUid)) {
            showError("No puedes unirte a tu propia partida.");
            return;
        }

        mCreateGameButton.setEnabled(false);
        mLobbyStatus.setText("Intentando unirse a la partida...");

        DatabaseReference gameRef = mGamesRef.child(gameId);

        gameRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                String status = currentData.child("status")
                        .getValue(String.class);

                String existingPlayer2 = currentData.child("player2Uid")
                        .getValue(String.class);

                String currentCreator = currentData.child("creatorUid")
                        .getValue(String.class);

                // Comprobar que la partida siga disponible.
                if (!"waiting".equals(status)
                        || existingPlayer2 != null
                        || currentCreator == null
                        || mPlayerId.equals(currentCreator)) {
                    return Transaction.abort();
                }

                // Registrar al segundo jugador.
                currentData.child("player2Uid").setValue(mPlayerId);
                currentData.child("status").setValue("playing");

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(
                    DatabaseError error,
                    boolean committed,
                    DataSnapshot currentData
            ) {
                mCreateGameButton.setEnabled(true);

                if (error != null) {
                    Log.e(TAG, "Error al unirse a la partida", error.toException());
                    showError("No se pudo unir: " + error.getMessage());
                } else if (committed) {
                    openGame(gameId, false);
                } else {
                    showError(
                            "La partida ya no está disponible. Actualiza la lista."
                    );
                }
            }
        });
    }


    private void showError(String message) {
        mLobbyStatus.setText(message);
    }

    @Override
    protected void onDestroy() {
        if (mWaitingGamesQuery != null && mGamesListener != null) {
            mWaitingGamesQuery.removeEventListener(mGamesListener);
        }

        if (mMyCreatedGameRef != null
                && mMyCreatedGameListener != null) {
            mMyCreatedGameRef.removeEventListener(
                    mMyCreatedGameListener
            );
        }

        super.onDestroy();
    }


    private void openGame(String gameId, boolean isCreator) {
        if (mOpeningGame || gameId == null || mPlayerId == null) {
            return;
        }

        mOpeningGame = true;

        if (mMyCreatedGameRef != null
                && mMyCreatedGameListener != null) {

            mMyCreatedGameRef.removeEventListener(
                    mMyCreatedGameListener
            );

            mMyCreatedGameListener = null;
            mMyCreatedGameRef = null;
        }

        Intent intent = new Intent(
                LobbyActivity.this,
                AndroidTicTacToeActivity.class
        );

        intent.putExtra("gameId", gameId);
        intent.putExtra("playerId", mPlayerId);
        intent.putExtra("isCreator", isCreator);

        startActivity(intent);
    }







}
