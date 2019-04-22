package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {

    private static final String DROPBOX_CREDENTIALS_STORAGE = "dropbox_credentials";
    private static final String DROPBOX_ACCESS_TOKEN = "dropbox_access_token";
    private static final String DROPBOX_USER_ID = "dropbox_user_id";

    //server response types to login attempt
    private static final String SUCCESS = "Success";
    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";

    private final String mUsername;
    private AlbumsActivity _activity;

    UserLogoutTask(String username, AlbumsActivity act) {
        mUsername = username;
        _activity = act;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            URL url = new URL(_activity.getString(R.string.serverAddress) + "/logout");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("username", mUsername);

            //TODO see what each of this properties do
            //conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("Content-Type", "application/json");
            //conn.setRequestProperty("Accept", "application/json");
            //conn.setRequestProperty("connection", "Keep-Alive");
            //conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);

            conn.setRequestProperty("Authorization", NetworkHandler.readToken(_activity));

            OutputStream os = conn.getOutputStream();
            os.write(postDataParams.toString().getBytes());
            os.close();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String response = NetworkHandler.convertStreamToString(in);

            if(response != null && response.equals(SUCCESS) || response.equals(NEED_AUTHENTICATION)){
                NetworkHandler.writeTokenAndUsername("", "", _activity);
                return true;
            }
            return false;

        } catch (Exception e) {
            Log.e("MyDebug", "Exception: " + e.getMessage());
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        _activity.setmLogout(null);

        if(success){
            Log.d("Debug Cenas", "Should stop my service" );
            //_activity.stopService();
            Toast.makeText(_activity, "Logout successful", Toast.LENGTH_LONG);

            //Remove dropbox token, as a way to make every user that is login in to login to their dropbox account, instead of already having a dropbox account associated from previous logins
            SharedPreferences prefs = _activity.getSharedPreferences(DROPBOX_CREDENTIALS_STORAGE, Context.MODE_PRIVATE);
            prefs.edit().remove(DROPBOX_ACCESS_TOKEN).apply();

            Intent logoutData = new Intent(_activity.getApplicationContext(), LoginActivity.class);
            _activity.startActivity(logoutData);
            _activity.finish();
        }
    }

    @Override
    protected void onCancelled() {
        _activity.setmLogout(null);
    }

}
