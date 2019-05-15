package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.content.Context.MODE_PRIVATE;

//TODO Rename this class to SessionHandler
public class SessionHandler {

    private static final String CREDENTIALS_STORAGE = "credentials";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String USER_ID = "user_id";

    //Storage filename
    private static final String TOKEN_FILENAME = "AuthToken";

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    //Swap internal storage for shared preferences
    static void writeTokenAndUsername(String token, String username, Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(CREDENTIALS_STORAGE, MODE_PRIVATE);
        prefs.edit().putString(ACCESS_TOKEN, token).apply();
        prefs.edit().putString(USER_ID, username).apply();
    }

    public static String readToken(Activity activity) {
        //TODO missing security
        SharedPreferences prefs = activity.getSharedPreferences(CREDENTIALS_STORAGE, MODE_PRIVATE);
        String token = prefs.getString(ACCESS_TOKEN, null);
        if (token == null)
            return "";
        else
            return token;
    }

    public static String readTUsername(Activity activity) {
        //TODO missing security
        SharedPreferences prefs = activity.getSharedPreferences(CREDENTIALS_STORAGE, MODE_PRIVATE);
        String username = prefs.getString(USER_ID, null);
        if (username == null)
            return "";
        else
            return username;
    }

    public static void cleanSessionCredentials(Activity activity) {
        //Remove session token of storage
        SharedPreferences prefs = activity.getSharedPreferences(CREDENTIALS_STORAGE, Context.MODE_PRIVATE);
        prefs.edit().remove(ACCESS_TOKEN).apply();
    }
}
