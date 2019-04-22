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

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UserRegisterTask extends AsyncTask<Void, Void, String> {

    private final String mUsername;
    private final String mPassword;
    private RegisterActivity _activity;
    private static final int REQUEST_REGISTER_CODE = 1;
    private static final String USERNAME_EXTRA = "username";
    private static final String PASSWORD_EXTRA = "password";

    //server response types to request attempt TODO this strings should correspond to the ones sent by the server after login attempt
    private static final String REGISTER_SUCCESS = "Success";
    private static final String REGISTER_USERNAME_ALREADY_EXISTS = "UsernameAlreadyExists";
    private static final String REGISTER_ERROR = "Error";

    UserRegisterTask(String username, String password, RegisterActivity act) {
        mUsername = username;
        mPassword = password;
        _activity = act;
    }

    @Override
    protected String doInBackground(Void... params) {



        // TODO: attempt authentication against a network service. while not having server

            /*try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return REGISTER_ERROR;
            }

            int i;
            for (i = 0; i < LoginActivity.DUMMY_CREDENTIALS.length && !LoginActivity.DUMMY_CREDENTIALS[i].equals(""); i++) {
                if(LoginActivity.DUMMY_CREDENTIALS[i].equals(mUsername)) {
                    return REGISTER_USERNAME_ALREADY_EXISTS;
                }
            }
            LoginActivity.DUMMY_CREDENTIALS[i] = "" + mUsername + ":" + mPassword;
            return REGISTER_SUCCESS;*/



        // TODO: register the new account here. For server

        String response = null;
        try {
            URL url = new URL(_activity.getString(R.string.serverAddress) + "/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("username", mUsername);
            postDataParams.put("password", mPassword);

            //TODO see what each of this properties do
            //conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("Content-Type", "application/json");
            //conn.setRequestProperty("Accept", "application/json");
            //conn.setRequestProperty("connection", "Keep-Alive");
            //conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            os.write(postDataParams.toString().getBytes());
            os.close();

            // read the response TODO
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = NetworkHandler.convertStreamToString(in);

        } catch (Exception e) {
            Log.e("MYDEBUG", "Exception: " + e.getMessage());
        }

        return response;

    }

    @Override
    protected void onPostExecute(final String response) {
        _activity.setmAuthTask(null);
        _activity.showProgress(false);

        //TODO
        Toast.makeText(_activity, response, Toast.LENGTH_LONG).show();

        if (response == null || response.equals(REGISTER_ERROR)){ //REGISTER_ERROR is returned or something else not expected
            Toast.makeText(_activity, "Something went wrong, try again later", Toast.LENGTH_LONG);
        }
        else if (response.equals(REGISTER_SUCCESS)) {
            //TODO send the information to the login page and make login automaticly
            Intent accountData = new Intent();
            accountData.putExtra(USERNAME_EXTRA, mUsername);
            accountData.putExtra(PASSWORD_EXTRA, mPassword);
            _activity.setResult(_activity.RESULT_OK, accountData);
            _activity.finish();
        }
        else if(response.equals(REGISTER_USERNAME_ALREADY_EXISTS)) {
            _activity.getmUsernameView().setError(_activity.getString(R.string.error_username_taken));
            _activity.getmUsernameView().requestFocus();
        }
    }

    @Override
    protected void onCancelled() {
        _activity.setmAuthTask(null);
        _activity.showProgress(false);
    }
}