package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetUsersTask extends AsyncTask<Void, Void, String> {

    private FindUsersActivity _activity;

    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";


    public GetUsersTask(FindUsersActivity activity){
        _activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        String response = "";
        try {
            URL url = new URL(_activity.getString(R.string.serverAddress) + "/getUsers");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");


            //TODO see what each of this properties do
            //conn.setRequestProperty("accept", "*/*");
            //conn.setRequestProperty("Content-Type", "application/json");
            //conn.setRequestProperty("Accept", "application/");
            //conn.setRequestProperty("connection", "Keep-Alive");
            //conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(false);

            conn.setRequestProperty("Authorization", NetworkHandler.readToken(_activity));

            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = NetworkHandler.convertStreamToString(in);

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
            //Logout and start login
            Intent logoutData = new Intent(_activity.getApplicationContext(), LoginActivity.class);
            _activity.startActivity(logoutData);
            _activity.finish();
        }
        else {
            for(String s : usersStr.split(";")){
                _activity.addUsers(s);
            }
            _activity.addAllusersCustom();
        }
    }

    @Override
    protected void onCancelled() {
    }
}