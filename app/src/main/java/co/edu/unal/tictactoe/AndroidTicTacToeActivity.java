package co.edu.unal.tictactoe;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.media.MediaPlayer;
import android.os.Handler;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import android.content.Intent;



public class AndroidTicTacToeActivity extends Activity {


    // Represents the internal state of the game
    private TicTacToeGame mGame;

    // Buttons making up the board
    //private Button mBoardButtons[];

    // Various text displayed
    private TextView mInfoTextView;
    private boolean mGameOver;

    private TextView mHumanWinsTextView;
    private TextView mAndroidWinsTextView;
    private TextView mTiesTextView;

    private int mHumanWins;
    private int mAndroidWins;
    private int mTies;

    // Estadísticas independientes del modo online.
    private int mOnlineWins;
    private int mOnlineLosses;
    private int mOnlineTies;

    private boolean mHumanStarts = true;

    private static final int DIALOG_DIFFICULTY_ID = 0;
    private static final int DIALOG_QUIT_ID = 1;
    private static final int DIALOG_ABOUT_ID = 2;
    private BoardView mBoardView;

    private MediaPlayer mHumanMediaPlayer;
    private MediaPlayer mComputerMediaPlayer;

    private boolean mHumanTurn = true;

    private SharedPreferences mPrefs;
    private FirebaseAuth mAuth;
    private String mPlayerId;
    private boolean mOnlineMode = false;
    private String mOnlineGameId;
    private String mOnlinePlayerId;
    private boolean mIsCreator;
    private DatabaseReference mOnlineGameRef;
    private ValueEventListener mOnlineGameListener;
    private char mOnlineSymbol;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);

        // Estas opciones solo corresponden al modo individual.
        menu.findItem(R.id.difficulty).setVisible(!mOnlineMode);
        menu.findItem(R.id.reset_scores).setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.main_menu) {
            Intent intent = new Intent(
                    AndroidTicTacToeActivity.this,
                    MainMenuActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);
            return true;
        }

        if (item.getItemId() == R.id.new_game) {
            if (mOnlineMode) {
                finish();
            } else {
                startNewGame();
            }
            return true;
        }
        else if (item.getItemId() == R.id.difficulty) {
            showDialog(DIALOG_DIFFICULTY_ID);
            return true;
        } else if (item.getItemId() == R.id.reset_scores) {

            SharedPreferences.Editor editor = mPrefs.edit();

            if (mOnlineMode) {
                mOnlineWins = 0;
                mOnlineLosses = 0;
                mOnlineTies = 0;

                editor.putInt("mOnlineWins", 0);
                editor.putInt("mOnlineLosses", 0);
                editor.putInt("mOnlineTies", 0);
            } else {
                mHumanWins = 0;
                mAndroidWins = 0;
                mTies = 0;

                editor.putInt("mHumanWins", 0);
                editor.putInt("mAndroidWins", 0);
                editor.putInt("mTies", 0);
            }

            editor.apply();
            updateScore();

            return true;
        } else if (item.getItemId() == R.id.about) {
            showDialog(DIALOG_ABOUT_ID);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DIFFICULTY_ID:
                return new AlertDialog.Builder(this)
                        .setTitle("Difficulty")
                        .setSingleChoiceItems(
                                new String[]{"Easy", "Harder", "Expert"},
                                mGame.getDifficultyLevel().ordinal(),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mGame.setDifficultyLevel(
                                                TicTacToeGame.DifficultyLevel.values()[which]
                                        );
                                        SharedPreferences.Editor editor = mPrefs.edit();

                                        editor.putInt(
                                                "difficulty",
                                                mGame.getDifficultyLevel().ordinal()
                                        );

                                        editor.apply();
                                        dialog.dismiss();
                                    }
                                })
                        .create();

            case DIALOG_QUIT_ID:
                return new AlertDialog.Builder(this)
                        .setTitle("Quit")
                        .setMessage("Are you sure you want to quit?")
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                        .setNegativeButton("No", null)
                        .create();

            case DIALOG_ABOUT_ID:
                LayoutInflater inflater = getLayoutInflater();
                View aboutView = inflater.inflate(R.layout.about_dialog, null);

                return new AlertDialog.Builder(this)
                        .setTitle("About")
                        .setView(aboutView)
                        .setPositiveButton("OK", null)
                        .create();

            default:
                return null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mPlayerId = currentUser.getUid();
            Log.d("TriquiOnline", "Jugador identificado: " + mPlayerId);
        } else {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                mPlayerId = user.getUid();

                                Log.d(
                                        "TriquiOnline",
                                        "Jugador identificado: " + mPlayerId
                                );
                            }
                        } else {
                            Log.e(
                                    "TriquiOnline",
                                    "Error al iniciar sesión anónima",
                                    task.getException()
                            );
                        }
                    });
        }

        mPrefs = getSharedPreferences("TicTacToePrefs", MODE_PRIVATE);

        setContentView(R.layout.main);

        mOnlineGameId = getIntent().getStringExtra("gameId");
        mOnlinePlayerId = getIntent().getStringExtra("playerId");
        mIsCreator = getIntent().getBooleanExtra("isCreator", false);

        mOnlineMode = mOnlineGameId != null
                && mOnlinePlayerId != null;

        mInfoTextView = findViewById(R.id.information);

        mHumanWinsTextView = findViewById(R.id.human_wins);
        mAndroidWinsTextView = findViewById(R.id.android_wins);
        mTiesTextView = findViewById(R.id.ties);

        mGame = new TicTacToeGame();

        int difficulty = mPrefs.getInt("difficulty", 2);

        mGame.setDifficultyLevel(
                TicTacToeGame.DifficultyLevel.values()[difficulty]
        );

        mBoardView = findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);

        // Recuperar marcadores guardados permanentemente
        mHumanWins = mPrefs.getInt("mHumanWins", 0);
        mAndroidWins = mPrefs.getInt("mAndroidWins", 0);
        mTies = mPrefs.getInt("mTies", 0);

        mOnlineWins = mPrefs.getInt("mOnlineWins", 0);
        mOnlineLosses = mPrefs.getInt("mOnlineLosses", 0);
        mOnlineTies = mPrefs.getInt("mOnlineTies", 0);


        if (mOnlineMode) {
            if (savedInstanceState != null) {
                mGame.setBoardState(
                        savedInstanceState.getCharArray("board")
                );
                mGameOver = savedInstanceState.getBoolean("mGameOver");
                mHumanTurn = savedInstanceState.getBoolean("mHumanTurn");
            } else {
                mGame.clearBoard();
                mGameOver = false;
            }

            mInfoTextView.setText(
                    "Partida en línea. Preparando tablero compartido..."
            );

            mBoardView.invalidate();

        } else if (savedInstanceState == null) {
            startNewGame();

        } else {
            mGame.setBoardState(savedInstanceState.getCharArray("board"));
            mGameOver = savedInstanceState.getBoolean("mGameOver");

            mInfoTextView.setText(
                    savedInstanceState.getCharSequence("info")
            );

            mHumanStarts = savedInstanceState.getBoolean("mHumanStarts");
            mHumanTurn = savedInstanceState.getBoolean("mHumanTurn");

            mBoardView.invalidate();
        }


        updateScore();

        if (mOnlineMode) {
            setupOnlineGame();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mGameOver", mGameOver);

        outState.putCharSequence("info", mInfoTextView.getText());

        outState.putBoolean("mHumanStarts", mHumanStarts);
        outState.putBoolean("mHumanTurn", mHumanTurn);
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences.Editor editor = mPrefs.edit();

        editor.putInt("mHumanWins", mHumanWins);
        editor.putInt("mAndroidWins", mAndroidWins);
        editor.putInt("mTies", mTies);

        editor.putInt("mOnlineWins", mOnlineWins);
        editor.putInt("mOnlineLosses", mOnlineLosses);
        editor.putInt("mOnlineTies", mOnlineTies);

        editor.apply();
    }

    private void updateScore() {
        if (mOnlineMode) {
            mHumanWinsTextView.setText("Victorias: " + mOnlineWins);
            mAndroidWinsTextView.setText("Derrotas: " + mOnlineLosses);
            mTiesTextView.setText("Empates: " + mOnlineTies);
        } else {
            mHumanWinsTextView.setText("Human wins: " + mHumanWins);
            mAndroidWinsTextView.setText("Android wins: " + mAndroidWins);
            mTiesTextView.setText("Ties: " + mTies);
        }
    }

    private void startNewGame() {

        mGame.clearBoard();
        mBoardView.invalidate();

        mGameOver = false;

        // Alternate player turn
        if (mHumanStarts) {
            // Human goes first
            mHumanTurn = true;
            mInfoTextView.setText(R.string.you_go_first);
        } else {
            // Android goes first
            mHumanTurn = false;
            mInfoTextView.setText(R.string.android_turn);

            int move = mGame.getComputerMove();
            setMove(TicTacToeGame.COMPUTER_PLAYER, move);

            mHumanTurn = true;
            mInfoTextView.setText(R.string.your_turn);
        }

        mHumanStarts = !mHumanStarts;
    }

    private void setMove(char player, int location) {
        mGame.setMove(player, location);
        mBoardView.invalidate();

        if (player == TicTacToeGame.HUMAN_PLAYER) {
            if (mHumanMediaPlayer != null) {
                mHumanMediaPlayer.start();
            }
        }
        else if (player == TicTacToeGame.COMPUTER_PLAYER) {
            if (mComputerMediaPlayer != null) {
                mComputerMediaPlayer.start();
            }
        }
    }


    private void setupOnlineGame() {
        mOnlineGameRef = FirebaseDatabase
                .getInstance("https://tictactoeandroid-53171-default-rtdb.firebaseio.com/")
                .getReference("games")
                .child(mOnlineGameId);

        mOnlineSymbol = mIsCreator ? 'X' : 'O';

        mOnlineGameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String board = snapshot.child("board")
                        .getValue(String.class);
                String turn = snapshot.child("turn")
                        .getValue(String.class);
                String status = snapshot.child("status")
                        .getValue(String.class);
                String winner = snapshot.child("winner")
                        .getValue(String.class);

                if (board == null || board.length() != 9) {
                    mInfoTextView.setText("Tablero no disponible.");
                    return;
                }

                char[] previousCells = mGame.getBoardState();
                char[] cells = new char[9];

                for (int i = 0; i < 9; i++) {
                    cells[i] = board.charAt(i) == '-'
                            ? TicTacToeGame.OPEN_SPOT
                            : board.charAt(i);
                }

// Detectar si se ha añadido una nueva jugada.
                char movedPlayer = TicTacToeGame.OPEN_SPOT;

                for (int i = 0; i < 9; i++) {
                    if (previousCells[i] == TicTacToeGame.OPEN_SPOT
                            && cells[i] != TicTacToeGame.OPEN_SPOT) {
                        movedPlayer = cells[i];
                        break;
                    }
                }

// Actualizar el tablero compartido.
                mGame.setBoardState(cells);

// Reproducir el sonido según quién haya jugado.
                if (movedPlayer != TicTacToeGame.OPEN_SPOT) {
                    if (movedPlayer == mOnlineSymbol) {
                        if (mHumanMediaPlayer != null) {
                            mHumanMediaPlayer.start();
                        }
                    } else {
                        if (mComputerMediaPlayer != null) {
                            mComputerMediaPlayer.start();
                        }
                    }
                }

                mBoardView.invalidate();

                if ("finished".equals(status)) {
                    mGameOver = true;

                    registerOnlineResult(winner);

                    if ("tie".equals(winner)) {
                        mInfoTextView.setText("¡Empate!");
                    } else if (String.valueOf(mOnlineSymbol).equals(winner)) {
                        mInfoTextView.setText("¡Has ganado!");
                    } else if ("X".equals(winner) || "O".equals(winner)) {
                        mInfoTextView.setText("Has perdido.");
                    }
                } else if ("playing".equals(status)) {
                    mGameOver = false;

                    if (String.valueOf(mOnlineSymbol).equals(turn)) {
                        mInfoTextView.setText("Tu turno (" + mOnlineSymbol + ")");
                    } else {
                        mInfoTextView.setText("Turno del rival (" + turn + ")");
                    }
                } else {
                    mInfoTextView.setText("Esperando al segundo jugador...");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("TriquiOnline", "Error leyendo partida",
                        error.toException());
                mInfoTextView.setText("Error al cargar la partida.");
            }
        };

        mOnlineGameRef.addValueEventListener(mOnlineGameListener);
    }

    private void registerOnlineResult(String winner) {
        if (mOnlineGameId == null || winner == null) {
            return;
        }

        // Cada partida solo debe contabilizarse una vez.
        String resultKey = "online_result_counted_" + mOnlineGameId;

        if (mPrefs.getBoolean(resultKey, false)) {
            return;
        }

        if ("tie".equals(winner)) {
            mOnlineTies++;
        } else if (String.valueOf(mOnlineSymbol).equals(winner)) {
            mOnlineWins++;
        } else if ("X".equals(winner) || "O".equals(winner)) {
            mOnlineLosses++;
        } else {
            return;
        }

        SharedPreferences.Editor editor = mPrefs.edit();

        editor.putInt("mOnlineWins", mOnlineWins);
        editor.putInt("mOnlineLosses", mOnlineLosses);
        editor.putInt("mOnlineTies", mOnlineTies);
        editor.putBoolean(resultKey, true);
        editor.apply();

        updateScore();
    }



    private void playOnlineMove(int position) {
        if (mOnlineGameRef == null || mGameOver
                || position < 0 || position >= TicTacToeGame.BOARD_SIZE) {
            return;
        }

        mOnlineGameRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData data) {
                String status = data.child("status").getValue(String.class);
                String turn = data.child("turn").getValue(String.class);
                String board = data.child("board").getValue(String.class);

                if (!"playing".equals(status)
                        || !String.valueOf(mOnlineSymbol).equals(turn)
                        || board == null
                        || board.length() != 9
                        || board.charAt(position) != '-') {
                    return Transaction.abort();
                }

                char[] cells = board.toCharArray();
                cells[position] = mOnlineSymbol;

                char[] gameCells = new char[9];

                for (int i = 0; i < 9; i++) {
                    gameCells[i] = cells[i] == '-'
                            ? TicTacToeGame.OPEN_SPOT
                            : cells[i];
                }

                TicTacToeGame checker = new TicTacToeGame();
                checker.setBoardState(gameCells);

                int result = checker.checkForWinner();

                data.child("board").setValue(new String(cells));
                data.child("turn").setValue(
                        mOnlineSymbol == 'X' ? "O" : "X"
                );

                if (result == 0) {
                    data.child("winner").setValue("none");
                } else {
                    data.child("status").setValue("finished");

                    if (result == 1) {
                        data.child("winner").setValue("tie");
                    } else if (result == 2) {
                        data.child("winner").setValue("X");
                    } else {
                        data.child("winner").setValue("O");
                    }
                }

                return Transaction.success(data);
            }

            @Override
            public void onComplete(
                    DatabaseError error,
                    boolean committed,
                    DataSnapshot snapshot
            ) {
                if (error != null) {
                    Log.e("TriquiOnline", "Error guardando jugada",
                            error.toException());
                } else if (!committed) {
                    Log.d("TriquiOnline",
                            "Jugada rechazada: casilla ocupada o turno incorrecto.");
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        mHumanMediaPlayer = MediaPlayer.create(
                getApplicationContext(),
                R.raw.human_move);

        mComputerMediaPlayer = MediaPlayer.create(
                getApplicationContext(),
                R.raw.computer_move);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mHumanMediaPlayer != null) {
            mHumanMediaPlayer.release();
            mHumanMediaPlayer = null;
        }

        if (mComputerMediaPlayer != null) {
            mComputerMediaPlayer.release();
            mComputerMediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (mOnlineGameRef != null && mOnlineGameListener != null) {
            mOnlineGameRef.removeEventListener(mOnlineGameListener);
        }

        super.onDestroy();
    }


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            // Determine which cell was touched
            int col = (int) event.getX()
                    / mBoardView.getBoardCellWidth();

            int row = (int) event.getY()
                    / mBoardView.getBoardCellHeight();

            int pos = row * 3 + col;


            if (mOnlineMode) {
                if (event.getAction() == MotionEvent.ACTION_UP
                        && col >= 0 && col < 3
                        && row >= 0 && row < 3) {
                    playOnlineMove(pos);
                }

                return true;
            }


            if (!mGameOver &&
                    mHumanTurn &&
                    mGame.getBoardOccupant(pos) == TicTacToeGame.OPEN_SPOT) {

                // Human makes a move
                setMove(TicTacToeGame.HUMAN_PLAYER, pos);

                int winner = mGame.checkForWinner();

                if (winner == 0) {

                    // Now it is Android's turn
                    mHumanTurn = false;
                    mInfoTextView.setText(R.string.android_turn);

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {

                            int move = mGame.getComputerMove();

                            setMove(TicTacToeGame.COMPUTER_PLAYER, move);

                            int winner = mGame.checkForWinner();

                            if (winner == 0) {

                                mHumanTurn = true;
                                mInfoTextView.setText(R.string.your_turn);

                            } else if (winner == 1) {

                                mInfoTextView.setText(R.string.tie);
                                mTies++;
                                updateScore();
                                mGameOver = true;

                            } else if (winner == 2) {

                                mInfoTextView.setText(R.string.you_won);
                                mHumanWins++;
                                updateScore();
                                mGameOver = true;

                            } else {

                                mInfoTextView.setText(R.string.android_won);
                                mAndroidWins++;
                                updateScore();
                                mGameOver = true;
                            }
                        }

                    }, 1000);
                }

                else if (winner == 1) {

                    mInfoTextView.setText(R.string.tie);
                    mTies++;
                    updateScore();
                    mGameOver = true;

                } else if (winner == 2) {

                    mInfoTextView.setText(R.string.you_won);
                    mHumanWins++;
                    updateScore();
                    mGameOver = true;

                } else {

                    mInfoTextView.setText(R.string.android_won);
                    mAndroidWins++;
                    updateScore();
                    mGameOver = true;
                }
            }

            // So we aren't notified of continued events
            // when the finger is moved
            return false;
        }

    };
}