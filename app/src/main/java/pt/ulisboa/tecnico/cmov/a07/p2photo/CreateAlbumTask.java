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

public class CreateAlbumTask extends AsyncTask<Void, Void, String> {

    //server response types to login attempt
    private static final String SUCCESS = "Success";
    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";

    private final String _albumName;
    private final String _url;
    private AlbumsActivity _activity;

    CreateAlbumTask(String albumName, String url, AlbumsActivity act) {
        _albumName = albumName;
        _url = url;
        _activity = act;
    }

    @Override
    protected String doInBackground(Void... params) {
        String response = "";
        try {
            URL url = new URL(_activity.getString(R.string.serverAddress) + "/createAlbum");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("albumName", _albumName);
            postDataParams.put("albumUrl", _url);


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
            response = NetworkHandler.convertStreamToString(in);

        } catch (Exception e) {
            Log.e("MyDebug", "Exception: " + e.getMessage());
        }
        return response;
    }

    @Override
    protected void onPostExecute(final String response) {
        _activity.setmCreateAlb(null);

        if(response != null && response.equals(SUCCESS)){ //TODO change this
            Toast.makeText(_activity, "You created an album!", Toast.LENGTH_SHORT).show();
        }
        else if(response.equals(NEED_AUTHENTICATION)) {
            Toast.makeText(_activity, "Not properly authenticated. Login again.", Toast.LENGTH_LONG).show();
            //Logout and start login
            Intent logoutData = new Intent(_activity.getApplicationContext(), LoginActivity.class);
            _activity.startActivity(logoutData);
            _activity.finish();
        }
        else {
            Toast.makeText(_activity, "Error creating albums", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCancelled() {
        _activity.setmCreateAlb(null);
    }

}