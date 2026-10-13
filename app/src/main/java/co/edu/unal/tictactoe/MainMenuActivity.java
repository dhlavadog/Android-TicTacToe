
package co.edu.unal.tictactoe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button singlePlayerButton =
                findViewById(R.id.single_player_button);

        Button onlineButton =
                findViewById(R.id.online_button);

        singlePlayerButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainMenuActivity.this,
                    AndroidTicTacToeActivity.class
            );

            // Sin extras: inicia el modo individual.
            startActivity(intent);
        });

        onlineButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainMenuActivity.this,
                    LobbyActivity.class
            );

            startActivity(intent);
        });
    }
}
