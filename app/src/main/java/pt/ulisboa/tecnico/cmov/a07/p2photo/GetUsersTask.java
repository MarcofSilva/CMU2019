package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.DropboxAuthenticationHandler;

public class GetUsersTask extends AsyncTask<Void, Void, String> {

    private FindUsersActivity _activity;
    private String _albumName;

    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";


    GetUsersTask(FindUsersActivity activity, String albumName){
        _activity = activity;
        _albumName = albumName;
    }

    @Override
    protected String doInBackground(Void... params) {
        String response = "";
        try {
            URL url = new URL(_activity.getString(R.string.serverAddress) + "/getUsers?albumName=" + _albumName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setDoOutput(false);

            conn.setRequestProperty("Authorization", SessionHandler.readToken(_activity));

            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = SessionHandler.convertStreamToString(in);

            //TODO dummy
            //return DUMMY_LIST

            return response;

        } catch (Exception e) {
            Log.e("MYDEBUG", "Exception: " + e.getMessage());
        }
        return response;
    }

    @Override
    protected void onPostExecute(final String usersStr) {
        if(usersStr == null){
            Toast.makeText(_activity, "Error: Getting users from server", Toast.LENGTH_SHORT).show();
        }
        else if(usersStr.equals(NEED_AUTHENTICATION)){
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
        else {
            String[] response = usersStr.split(",");
            String permitted = response[0];
            String notPermitted = response[1];
            for(String s : permitted.split(";")) {
                if (!s.equals("")) {
                    _activity.addUsersPermitted(s);
                    _activity.addUser(s);
                }
            }
            for(String s : notPermitted.split(";")) {
                if (!s.equals("")) {
                    _activity.addUser(s);
                }
            }
            _activity.addAllusersCustom();

        }
    }

    @Override
    protected void onCancelled() {
    }
}