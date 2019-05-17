package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;

import javax.crypto.spec.SecretKeySpec;

import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.Dropbox_AlbumsActivity;
import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.Security.KeyManager;
import pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.WifiDirect_AlbumsActivity;

/**
 * Activity to redirect the app to the login or the main activity
 */
public class MainEmptyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_chooser);
    }

    //OnClick
    public void cloudVersion(View view) {
        startNextActivity(Dropbox_AlbumsActivity.class, getString(R.string.AppModeDropBox));
    }

    //OnClick
    public void wifiDirectVersion(View view) {
        startNextActivity(WifiDirect_AlbumsActivity.class, getString(R.string.AppModeWifiDirect));
    }

    private void startNextActivity(Class<?> destClass, String appMode) {
        Intent activityIntent;
        //Start login/register or main activity depending on the login being already done and still be valid or not.
        if (isLoggedIn()) { //Check for login already done
            activityIntent = new Intent(this, destClass);
        }
        else {
            activityIntent = new Intent(this, LoginActivity.class);
        }
        //Save the mode in the context
        ContextClass context = (ContextClass) getApplicationContext();
        context.setAppMode(appMode);
        //activityIntent.putExtra(getString(R.string.AppMode), appMode);

        startActivity(activityIntent);
        finish();
    }

    //Check for login session token already in phone's storage
    private boolean isLoggedIn() {
        try {
            String token = SessionHandler.readToken(MainEmptyActivity.this);
            if (token == null || token.equals("")) {
                return false;
            }
        } catch (Exception e) {
            Log.e("MyDebug", "Exception " + e + ": " + e.getMessage());
        }
        return true;
    }
}
