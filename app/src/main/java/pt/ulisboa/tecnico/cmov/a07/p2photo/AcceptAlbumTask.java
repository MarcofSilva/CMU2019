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

import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.DropboxAuthenticationHandler;
import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.Security.KeyManager;

public class AcceptAlbumTask extends AsyncTask<Void, Void, String> {

    private String _userAlbum;
    private String _albumName;
    private String _dropboxUrl;
    private String _accepted;
    private String _appMode;
    private AlbumsInvitationsActivity _act;

    private static final String SUCCESS = "Success";
    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";
    private static final String ERROR = "Error";


    AcceptAlbumTask(String userAlbum, String albumName, String dropboxUrl, String accepted, String appMode ,AlbumsInvitationsActivity act) {
        _userAlbum = userAlbum;
        _albumName = albumName;
        _dropboxUrl = dropboxUrl;
        _accepted = accepted;
        _appMode = appMode;
        _act = act;
    }

    @Override
    protected String doInBackground(Void... params) {

        String response = null;
        try {
            URL url;
            HttpURLConnection conn;
            if(_appMode.equals(_act.getApplicationContext().getString(R.string.AppModeDropBox)) && _accepted.equals("true")){
                url = new URL(_act.getString(R.string.serverAddress) + "/requestAlbumKey");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("userAlbum", _userAlbum);
                postDataParams.put("albumName", _albumName);
                Log.d("Debug Cenas","JSONArgs: user: " + _userAlbum + " album " + _albumName );


                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                conn.setRequestProperty("Authorization", SessionHandler.readToken(_act));

                OutputStream os = conn.getOutputStream();
                os.write(postDataParams.toString().getBytes());
                os.close();

                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = SessionHandler.convertStreamToString(in);

                byte[] albumKey = KeyManager.decryptAlbumKey(KeyManager.hexStringToBytes(response));
                KeyManager.addAlbumKey(_albumName, albumKey);
                _dropboxUrl = KeyManager.encrypt(_albumName, _dropboxUrl);
            }

            url = new URL(_act.getString(R.string.serverAddress) + "/acceptInvitation");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("userAlbum", _userAlbum);
            postDataParams.put("albumName", _albumName);

            postDataParams.put("dropboxURL", _dropboxUrl);
            postDataParams.put("accepted", _accepted);
            Log.d("Debug Cenas","JSONArgs: user: " + _userAlbum + " album " + _albumName + " dropbox " + _dropboxUrl +" accepted " + _accepted);


            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.setRequestProperty("Authorization", SessionHandler.readToken(_act));

            OutputStream os = conn.getOutputStream();
            os.write(postDataParams.toString().getBytes());
            os.close();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = SessionHandler.convertStreamToString(in);

        } catch (Exception e) {
            Log.e("MYDEBUG", "Exception: " + e.getMessage());
        }

        return response;

    }

    @Override
    protected void onPostExecute(final String response) {
        _act.setmAcceptALb(null);

        if(response != null && response.equals(SUCCESS)){
            Toast.makeText(_act, "Action succeed ", Toast.LENGTH_LONG).show();
        }
        else if(response.equals(NEED_AUTHENTICATION)) {
            Toast.makeText(_act, "Not properly authenticated. Login again.", Toast.LENGTH_LONG).show();


            //------Clean session tokens before logging out----------
            //App account session
            SessionHandler.cleanSessionCredentials(_act);

            // Check if appMode is the dropbox one and if so remove the token
            ContextClass contextClass = (ContextClass) _act.getApplicationContext();
            String appModeDropbox = _act.getString(R.string.AppModeDropBox);
            if(contextClass.getAppMode().equals(appModeDropbox)) {
                //Dropbox specific code(removing dropbox token from storage)
                DropboxAuthenticationHandler.cleanDropboxCredentials(_act);
            }

            //Logout and start login
            //nao deve ser preciso por causa do ondestroy da activity_act.stopService();
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
