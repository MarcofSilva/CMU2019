package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetUsersTask extends AsyncTask<Void, Void, String> {

    private FindUsersActivity _act;

    public GetUsersTask(FindUsersActivity act){
        _act = act;
    }

    @Override
    protected String doInBackground(Void... params) {
        String response = "";
        try {
            URL url = new URL("http://sigma03.ist.utl.pt:8350/getUsers");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");


            //TODO see what each of this properties do
            //conn.setRequestProperty("accept", "*/*");
            //conn.setRequestProperty("Content-Type", "application/json");
            //conn.setRequestProperty("Accept", "application/");
            //conn.setRequestProperty("connection", "Keep-Alive");
            //conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(false);

            conn.setRequestProperty("Authorization", NetworkHandler.readToken(_act));

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
            Toast.makeText(_act, "Error: Getting users from server", Toast.LENGTH_SHORT).show();
        }
        for(String s : usersStr.split(";")){
            _act.addUsers(s);
        }
        _act.addAllusersCustom();
    }

    @Override
    protected void onCancelled() {
    }
}