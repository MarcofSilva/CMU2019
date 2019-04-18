package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.Intent;
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
            URL url = new URL("http://sigma03.ist.utl.pt:8350/logout");
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
                NetworkHandler.writeTokenFile("", _activity);
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
            Toast.makeText(_activity, "Logout successful", Toast.LENGTH_LONG);
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
