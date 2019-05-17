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
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.DropboxAuthenticationHandler;
import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.Security.KeyManager;

public class AddUsersToAlbumTask extends AsyncTask<Void, Void, String> {

    //server response types to login attempt
    private static final String SUCCESS = "Success";
    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";
    private static final String ERROR = "Error";

    private final ArrayList<String > _usernamesToAdd;
    private String _albumName;
    private final String _appMode;
    private InsideAlbumActivity _activity;

    public AddUsersToAlbumTask(ArrayList<String> usernames, String albumName , String appMode, InsideAlbumActivity activity) {
        _usernamesToAdd = usernames;
        _albumName = albumName;
        _appMode = appMode;
        _activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        String response = "";
        try {
            URL url = new URL(_activity.getString(R.string.serverAddress) + "/addUsersToAlbum");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("albumName", _albumName);
            postDataParams.put("usernames", fixStringsInArrayList_json(_usernamesToAdd));

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.setRequestProperty("Authorization", SessionHandler.readToken(_activity));

            OutputStream os = conn.getOutputStream();
            os.write(postDataParams.toString().getBytes());
            os.close();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = SessionHandler.convertStreamToString(in);

            if(response == null || response.equals(NEED_AUTHENTICATION)){
                return response;
            }
            else if(!_appMode.equals(_activity.getApplicationContext().getString(R.string.AppModeDropBox))){
                return SUCCESS;
            }

            String userKeyEncriptPair = "";
            //receives u1;pku1,u2;pku2,
            String[] userKeyPairs = response.split(",");
            for(int i = 0; i < userKeyPairs.length; i++) {
                String[] pair = userKeyPairs[i].split(";");
                PublicKey pk = KeyManager.byteArrayToPubKey(KeyManager.hexStringToBytes(pair[1]));
                byte[] keyEncrypted = KeyManager.encryptAlbumKey(_albumName, pk);
                userKeyEncriptPair += pair[0] + ";" + KeyManager.byteArrayToHexString(keyEncrypted) + ",";
            }

            url = new URL(_activity.getString(R.string.serverAddress) + "/shareSliceEncrypt");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            postDataParams = new JSONObject();
            postDataParams.put("albumName", _albumName);

            postDataParams.put("sliceKeyEnc", userKeyEncriptPair);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.setRequestProperty("Authorization", SessionHandler.readToken(_activity));

            os = conn.getOutputStream();
            os.write(postDataParams.toString().getBytes());
            os.close();

            in = new BufferedInputStream(conn.getInputStream());
            response = SessionHandler.convertStreamToString(in);


        } catch (Exception e) {
            Log.e("MyDebug", "Exception: " + e.getMessage());
        }
        return response;
    }

    @Override
    protected void onPostExecute(final String response) {
        _activity.setAddUsersToAlbum(null);
        if(response.equals(NEED_AUTHENTICATION)) {
            Toast.makeText(_activity, "Not properly authenticated. Login again.", Toast.LENGTH_LONG).show();

            //------Clean session tokens before logging out----------
            //App account session
            SessionHandler.cleanSessionCredentials(_activity);

            // Check if appMode is the dropbox one and if so remove the token
            ContextClass contextClass = (ContextClass) _activity.getApplicationContext();
            String appModeDropbox = _activity.getString(R.string.AppModeDropBox);

            if(contextClass.getAppMode().equals(appModeDropbox)) {
                //Dropbox specific code(removing dropbox token from storage)
                DropboxAuthenticationHandler.cleanDropboxCredentials(_activity);
            }

            //Logout and start login
            Intent logoutData = new Intent(_activity.getApplicationContext(), LoginActivity.class);
            _activity.startActivity(logoutData);
            _activity.finish();
        }

        if(response != null && response.equals(SUCCESS)){
            Toast.makeText(_activity, "Sending usernames to server", Toast.LENGTH_LONG).show();
        }

        else { //Include receiving Error message from the server
            Toast.makeText(_activity, "Error adding users to album", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onCancelled() {
        _activity.setAddUsersToAlbum(null);
    }


    private ArrayList<String> fixStringsInArrayList_json(ArrayList<String> usernames) {
        for(int i = 0; i < usernames.size(); i++) {
            usernames.set(i, "\"" + usernames.get(i) + "\"");
        }
        return usernames;
    }
}