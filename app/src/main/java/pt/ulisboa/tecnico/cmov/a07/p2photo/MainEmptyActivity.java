package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.Dropbox_AlbumsActivity;
import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.Security.KeyManager;
import pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.WifiDirect_AlbumsActivity;

/**
 * Activity to redirect the app to the login or the main activity
 */
public class MainEmptyActivity extends AppCompatActivity {
    private GetMyKeysTask mGetUsersTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_chooser);
    }

    public GetMyKeysTask getmGetUsersTask() {
        return this.mGetUsersTask;
    }
    public void setmGetUsersTask(GetMyKeysTask mGetUsersTask) {
        this.mGetUsersTask = mGetUsersTask;
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
            String username = SessionHandler.readTUsername(this);
            KeyManager.readKeyPair(username,this);

            mGetUsersTask = new GetMyKeysTask(username,this);
            mGetUsersTask.execute((Void) null);
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


class GetMyKeysTask extends AsyncTask<Void, Void, String> {
    //server response types to login attempt
    private static final String SUCCESS = "Success";


    private final String mUsername;
    private MainEmptyActivity _activity;

    GetMyKeysTask(String username, MainEmptyActivity act) {
        mUsername = username;
        _activity = act;
    }

    @Override
    protected String doInBackground(Void... params) {

        String response = null;
        try {
            URL url = new URL(_activity.getString(R.string.serverAddress) + "/getMyKeys?username=" + mUsername);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setDoOutput(false);

            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = SessionHandler.convertStreamToString(in);

            String[] nameKeysPairs= response.split(",");

            for(String s : nameKeysPairs){
                if(s.length() == 0){
                    break;
                }
                String[] namekeySplit = s.split(";");
                byte[] key = KeyManager.decryptAlbumKey(KeyManager.hexStringToBytes(namekeySplit[1]));
                KeyManager.addAlbumKey(namekeySplit[0], key);
            }

            response = SUCCESS;

        } catch (Exception e) {
            Log.e("MYDEBUG", "Exception: " + e.getMessage());
        }

        return response;
    }

    @Override
    protected void onPostExecute(final String response) {
        _activity.setmGetUsersTask(null);

        if (response.equals(SUCCESS) ) {
        }
        else {
            Toast.makeText(_activity, "Something went wrong, try again later", Toast.LENGTH_LONG);

        }
    }

    @Override
    protected void onCancelled() {
        _activity.setmGetUsersTask(null);
    }
}
