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

public class UserCreateAlbumTask extends AsyncTask<Void, Void, String> {

    private final String _albumName;
    private final String _url;
    private AlbumsActivity _act;
    private static final String SUCCESS = "Success";
    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";

    UserCreateAlbumTask(String albumName, String url, AlbumsActivity act) {
        _albumName = albumName;
        _url = url;
        _act = act;
    }

    @Override
    protected String doInBackground(Void... params) {
        String response = "";
        try {
            URL url = new URL("http://sigma03.ist.utl.pt:8350/createAlbum");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("albumName", _albumName);
            postDataParams.put("url", _url);


            //TODO see what each of this properties do
            //conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("Content-Type", "application/json");
            //conn.setRequestProperty("Accept", "application/json");
            //conn.setRequestProperty("connection", "Keep-Alive");
            //conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);

            conn.setRequestProperty("Authorization", NetworkHandler.readToken(_act));

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
        _act.setmCreateAlb(null);

        if(response != null && response.equals(SUCCESS)){
            Toast.makeText(_act, "You created an album! Gl finding it", Toast.LENGTH_LONG).show();
        }
        else if(response.equals(NEED_AUTHENTICATION)) {
            Toast.makeText(_act, "Not properly authenticated. Login again.", Toast.LENGTH_LONG).show();
            //Logout and start login
            Intent logoutData = new Intent(_act.getApplicationContext(), LoginActivity.class);
            _act.startActivity(logoutData);
            _act.finish();
        }
        else {
            Toast.makeText(_act, "Error creating albums", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCancelled() {
        _act.setmCreateAlb(null);
    }

}