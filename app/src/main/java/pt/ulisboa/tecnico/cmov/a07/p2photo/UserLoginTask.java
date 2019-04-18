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

public class UserLoginTask extends AsyncTask<Void, Void, String> {
    private static final String LOGIN_SUCCESS = "Success";
    private static final String LOGIN_UNKNOWN_USER = "UnknownUser";
    private static final String LOGIN_INCORRECT_PASSWORD = "IncorrectPassword";
    private static final String LOGIN_ERROR = "Error";
    private static final String USERNAME_EXTRA = "username";

    private final String mUsername;
    private final String mPassword;
    private LoginActivity _act;

    UserLoginTask(String username, String password, LoginActivity act) {
        mUsername = username;
        mPassword = password;
        _act = act;
    }

    @Override
    protected String doInBackground(Void... params) {




        // TODO: attempt authentication against a network service.

            /*try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return LOGIN_ERROR;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mUsername)) {
                    // Account exists, return true if the password matches.
                    if(pieces[1].equals(mPassword))
                        return LOGIN_SUCCESS;
                    else
                        return LOGIN_INCORRECT_PASSWORD;
                }
            }
            return LOGIN_UNKNOWN_USER;*/




        //TODO to use with server

        String response = null;
        try {
            URL url = new URL("http://sigma03.ist.utl.pt:8350/login");
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

            String token = response.split(" ")[1];
            NetworkHandler.writeTokenFile(token, _act);

            response = response.split(" ")[0];

        } catch (Exception e) {
            Log.e("MYDEBUG", "Exception: " + e.getMessage());
        }

        return response;
    }

    @Override
    protected void onPostExecute(final String response) {
        _act.setmAuthTask(null);
        _act.showProgress(false);

        //TODO
        Toast.makeText(_act, response, Toast.LENGTH_LONG).show();


        if (response == null || response.equals(LOGIN_ERROR)) { //LOGIN_ERROR is returned or something else not expected
            Toast.makeText(_act, "Something went wrong, try again later", Toast.LENGTH_LONG);
        }
        else if(response.equals(LOGIN_SUCCESS)) {
            Intent loginData = new Intent(_act.getApplicationContext(), AlbumsActivity.class);
            loginData.putExtra(USERNAME_EXTRA, mUsername);
            _act.startActivity(loginData);
            _act.finish();
        }
        else if(response.equals(LOGIN_UNKNOWN_USER)) {
            _act.getmUsernameView().setError(_act.getApplicationContext().getString(R.string.error_unknown_username));
            _act.getmUsernameView().requestFocus();
        }
        else if(response.equals(LOGIN_INCORRECT_PASSWORD)) {
            _act.getmPasswordView().setError(_act.getApplicationContext().getString(R.string.error_incorrect_password));
            _act.getmPasswordView().requestFocus();
        }
    }

    @Override
    protected void onCancelled() {
        _act.setmAuthTask(null);
        _act.showProgress(false);
    }
}