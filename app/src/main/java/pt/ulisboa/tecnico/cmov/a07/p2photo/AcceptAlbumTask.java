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

public class AcceptAlbumTask extends AsyncTask<Void, Void, String> {

    private String _userAlbum;
    private String _albumName;
    private String _dropboxUrl;
    private String _accepted;
    private AlbumsActivity _act;

    private static final String SUCCESS = "Success";
    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";
    private static final String ERROR = "Error";

    //server response types to request attempt TODO this strings should correspond to the ones sent by the server after login attempt

    AcceptAlbumTask(String userAlbum, String albumName, String dropboxUrl, String accepted, AlbumsActivity act) {
        _userAlbum = userAlbum;
        _albumName = albumName;
        _dropboxUrl = dropboxUrl;
        _accepted = accepted;
        _act = act;
    }

    @Override
    protected String doInBackground(Void... params) {

        String response = null;
        try {
            URL url = new URL(_act.getString(R.string.serverAddress) + "/acceptInvitation");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("userAlbum", _userAlbum);
            postDataParams.put("albumName", _albumName);
            postDataParams.put("dropboxURL", _dropboxUrl);
            postDataParams.put("accepted", _accepted);
            Log.d("Debug Cenas","JSONArgs: user: " + _userAlbum + " album " + _albumName + " dropbox " + _dropboxUrl + " accepted " + _accepted);


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
            Log.e("MYDEBUG", "Exception: " + e.getMessage());
        }

        return response;

    }

    @Override
    protected void onPostExecute(final String response) {
        _act.setmAcceptALb(null);

        if(response != null && response.equals(SUCCESS)){

            Toast.makeText(_act, "Sending catalog URL for album " + _albumName, Toast.LENGTH_LONG).show();
        }
        else if(response.equals(NEED_AUTHENTICATION)) {
            Toast.makeText(_act, "Not properly authenticated. Login again.", Toast.LENGTH_LONG).show();
            //Logout and start login
            Intent logoutData = new Intent(_act.getApplicationContext(), LoginActivity.class);
            _act.startActivity(logoutData);
            _act.finish();
        }
        else { //Include receiving Error message from the server
            Toast.makeText(_act, "Error adding users to album", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCancelled() {
    }
}
