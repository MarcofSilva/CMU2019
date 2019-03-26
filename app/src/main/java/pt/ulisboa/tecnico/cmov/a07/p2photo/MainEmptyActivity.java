package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Activity with no display to redirect the app to the login or the main activity
 */
public class MainEmptyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent activityIntent;

        //Start login/register or main activity depending on the login being already done and still be valid or not.
        if (false) { //TODO check for login already done
            //activityIntent = new Intent(this, MainActivity.class);
        }
        else {
            activityIntent = new Intent(this, LoginActivity.class);
        }

        startActivity(activityIntent);
        finish();
    }
}
