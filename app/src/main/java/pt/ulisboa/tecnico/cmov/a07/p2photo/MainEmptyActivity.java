package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.FileOutputStream;

/**
 * Activity with no display to redirect the app to the login or the main activity
 */
public class MainEmptyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent activityIntent;

        //Start login/register or main activity depending on the login being already done and still be valid or not.
        if (isLoggedIn()) { //TODO check for login already done
            activityIntent = new Intent(this, AlbumsActivity.class);
        }
        else {
            activityIntent = new Intent(this, LoginActivity.class);
        }

        startActivity(activityIntent);
        finish();
    }

    //TODO should we ask the server to see if our token is really logged in?
    private boolean isLoggedIn() {
        try {
            if (NetworkHandler.readToken(MainEmptyActivity.this).equals("")) {
                return false;
            }
        } catch (Exception e) {
            Log.e("MyDebug", "Exception " + e + ": " + e.getMessage());
        }

        return true;
    }
}
