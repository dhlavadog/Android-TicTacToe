package co.edu.unal.tictactoe;

import java.util.Random;

public class TicTacToeGame {

    private char mBoard[] = {
            OPEN_SPOT, OPEN_SPOT, OPEN_SPOT,
            OPEN_SPOT, OPEN_SPOT, OPEN_SPOT,
            OPEN_SPOT, OPEN_SPOT, OPEN_SPOT
    };
    public static final int BOARD_SIZE = 9;

    public static final char HUMAN_PLAYER = 'X';
    public static final char COMPUTER_PLAYER = 'O';
    public static final char OPEN_SPOT = ' ';

    private Random mRand;

    // The computer's difficulty levels
    public enum DifficultyLevel {Easy, Harder, Expert};

    // Current difficulty level
    private DifficultyLevel mDifficultyLevel = DifficultyLevel.Expert;

    public DifficultyLevel getDifficultyLevel() {
        return mDifficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        mDifficultyLevel = difficultyLevel;
    }

    public TicTacToeGame() {

        // Seed the random number generator
        mRand = new Random();
    }



    private void displayBoard()	{
        System.out.println();
        System.out.println(mBoard[0] + " | " + mBoard[1] + " | " + mBoard[2]);
        System.out.println("-----------");
        System.out.println(mBoard[3] + " | " + mBoard[4] + " | " + mBoard[5]);
        System.out.println("-----------");
        System.out.println(mBoard[6] + " | " + mBoard[7] + " | " + mBoard[8]);
        System.out.println();
    }

    public void clearBoard(){
        for(int i = 0; i < BOARD_SIZE; i++){
            mBoard[i] = OPEN_SPOT;
        }
    }

    public void setMove(char player, int location){
        if (mBoard[location] == OPEN_SPOT){
            mBoard[location] = player;
        }
    }

    public char getBoardOccupant(int location) {
        return mBoard[location];
    }


    // Check for a winner.  Return
    //  0 if no winner or tie yet
    //  1 if it's a tie
    //  2 if X won
    //  3 if O won
    public int checkForWinner() {

        // Check horizontal wins
        for (int i = 0; i <= 6; i += 3)	{
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i + 1] == HUMAN_PLAYER &&
                    mBoard[i + 2]== HUMAN_PLAYER)
                return 2;
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i + 1]== COMPUTER_PLAYER &&
                    mBoard[i + 2] == COMPUTER_PLAYER)
                return 3;
        }

        // Check vertical wins
        for (int i = 0; i <= 2; i++) {
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i + 3] == HUMAN_PLAYER &&
                    mBoard[i + 6]== HUMAN_PLAYER)
                return 2;
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i + 3] == COMPUTER_PLAYER &&
                    mBoard[i + 6]== COMPUTER_PLAYER)
                return 3;
        }

        // Check for diagonal wins
        if ((mBoard[0] == HUMAN_PLAYER &&
                mBoard[4] == HUMAN_PLAYER &&
                mBoard[8] == HUMAN_PLAYER) ||
                (mBoard[2] == HUMAN_PLAYER &&
                        mBoard[4] == HUMAN_PLAYER &&
                        mBoard[6] == HUMAN_PLAYER))
            return 2;
        if ((mBoard[0] == COMPUTER_PLAYER &&
                mBoard[4] == COMPUTER_PLAYER &&
                mBoard[8] == COMPUTER_PLAYER) ||
                (mBoard[2] == COMPUTER_PLAYER &&
                        mBoard[4] == COMPUTER_PLAYER &&
                        mBoard[6] == COMPUTER_PLAYER))
            return 3;

        // Check for tie
        for (int i = 0; i < BOARD_SIZE; i++) {
            // If we find a number, then no one has won yet
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER)
                return 0;
        }

        // If we make it through the previous loop, all places are taken, so it's a tie
        return 1;
    }



    public int getComputerMove() {
        int move = -1;

        switch (mDifficultyLevel) {
            case Easy:
                move = getRandomMove();
                break;

            case Harder:
                move = getWinningMove();

                if (move == -1)
                    move = getRandomMove();
                break;

            case Expert:
                move = getWinningMove();

                if (move == -1)
                    move = getBlockingMove();

                if (move == -1)
                    move = getRandomMove();
                break;
        }

        return move;
    }

    private int getRandomMove() {
        int move;

        do {
            move = mRand.nextInt(BOARD_SIZE);
        } while (mBoard[move] == HUMAN_PLAYER ||
                mBoard[move] == COMPUTER_PLAYER);

        return move;
    }

    private int getWinningMove() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] != HUMAN_PLAYER &&
                    mBoard[i] != COMPUTER_PLAYER) {

                char curr = mBoard[i];
                mBoard[i] = COMPUTER_PLAYER;

                if (checkForWinner() == 3) {
                    mBoard[i] = curr;
                    return i;
                }
                else
                    mBoard[i] = curr;
            }
        }
        return -1;
    }

    private int getBlockingMove() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] != HUMAN_PLAYER &&
                    mBoard[i] != COMPUTER_PLAYER) {

                char curr = mBoard[i];
                mBoard[i] = HUMAN_PLAYER;

                if (checkForWinner() == 2) {
                    mBoard[i] = curr;
                    return i;
                }
                else
                    mBoard[i] = curr;
            }
        }

        return -1;
    }


    /**
     * @param args
     */

}
