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

    private boolean mHumanStarts = true;

    private static final int DIALOG_DIFFICULTY_ID = 0;
    private static final int DIALOG_QUIT_ID = 1;
    private static final int DIALOG_ABOUT_ID = 2;
    private BoardView mBoardView;

    private MediaPlayer mHumanMediaPlayer;
    private MediaPlayer mComputerMediaPlayer;

    private boolean mHumanTurn = true;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_game) {
            startNewGame();
            return true;
        } else if (item.getItemId() == R.id.difficulty) {
            showDialog(DIALOG_DIFFICULTY_ID);
            return true;
        } else if (item.getItemId() == R.id.quit) {
            showDialog(DIALOG_QUIT_ID);
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
        setContentView(R.layout.main);

        mInfoTextView = findViewById(R.id.information);

        mHumanWinsTextView = findViewById(R.id.human_wins);
        mAndroidWinsTextView = findViewById(R.id.android_wins);
        mTiesTextView = findViewById(R.id.ties);

        mGame = new TicTacToeGame();

        mBoardView = findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);

        mHumanWins = 0;
        mAndroidWins = 0;
        mTies = 0;

        updateScore();

        startNewGame();
    }

    private void updateScore() {
        mHumanWinsTextView.setText("Human wins: " + mHumanWins);
        mAndroidWinsTextView.setText("Android wins: " + mAndroidWins);
        mTiesTextView.setText("Ties: " + mTies);
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


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            // Determine which cell was touched
            int col = (int) event.getX()
                    / mBoardView.getBoardCellWidth();

            int row = (int) event.getY()
                    / mBoardView.getBoardCellHeight();

            int pos = row * 3 + col;

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