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
import java.util.ArrayList;

public class AddUsersToAlbumTask extends AsyncTask<Void, Void, String> {

    //server response types to login attempt
    private static final String SUCCESS = "Success";
    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";
    private static final String ERROR = "Error";

    private final ArrayList<String > _usernamesToAdd;
    private InsideAlbumActivity _activity;

    public AddUsersToAlbumTask(ArrayList<String> usernames, InsideAlbumActivity activity) {
        _usernamesToAdd = usernames;
        _activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        String response = "";
        try {
            URL url = new URL("http://sigma03.ist.utl.pt:8350/addUsersToAlbum");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("usernames", fixStringsInArrayList_json(_usernamesToAdd));



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
        _activity.setmAddUsersToAlbum(null);

        if(response != null && response.equals(SUCCESS)){
            Toast.makeText(_activity, "Sending usernames to server", Toast.LENGTH_LONG).show();
        }
        else if(response.equals(NEED_AUTHENTICATION)) {
            Toast.makeText(_activity, "Not properly authenticated. Login again.", Toast.LENGTH_LONG).show();
            //Logout and start login
            Intent logoutData = new Intent(_activity.getApplicationContext(), LoginActivity.class);
            _activity.startActivity(logoutData);
            _activity.finish();
        }
        else { //Include receiving Error message from the server
            Toast.makeText(_activity, "Error adding users to album", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCancelled() {
        _activity.setmAddUsersToAlbum(null);
    }


    private ArrayList<String> fixStringsInArrayList_json(ArrayList<String> usernames) {
        for(int i = 0; i < usernames.size(); i++) {
            usernames.set(i, "\"" + usernames.get(i) + "\"");
        }
        return usernames;
    }
}