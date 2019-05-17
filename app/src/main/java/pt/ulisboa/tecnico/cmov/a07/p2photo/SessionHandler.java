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
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.Security.KeyManager;

import static android.content.Context.MODE_PRIVATE;

public class SessionHandler {

    private static final String CREDENTIALS_STORAGE = "credentials";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String PRIVATE_KEY = "private_key";
    private static final String PUBLIC_KEY = "public_key";

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

    public static void writeKeyPair(KeyPair kp, String username, Activity activity){
        writePubKey(kp.getPublic(), username, activity);
        writePrivKey(kp.getPrivate(),username, activity);
    }

    public static KeyPair readKeyPair(String username, Activity activity){
        PublicKey pubkey = readPubKey(username, activity);
        PrivateKey prikey = readPrivKey(username, activity);
        return new KeyPair(pubkey, prikey);
    }

    public static void writePrivKey(PrivateKey key, String username, Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(CREDENTIALS_STORAGE, MODE_PRIVATE);
        prefs.edit().putString(username + ":" + PRIVATE_KEY, KeyManager.byteArrayToHexString(key.getEncoded())).apply();
    }

    public static PrivateKey readPrivKey(String username, Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(CREDENTIALS_STORAGE, MODE_PRIVATE);
        String token = prefs.getString(username + ":" + PRIVATE_KEY, null);
        if (token != null){
            try {
                return KeyManager.byteArrayToPrivKey(KeyManager.hexStringToBytes(token));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        else {
            return null;
        }
    }

    public static void writePubKey(PublicKey key, String username, Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(CREDENTIALS_STORAGE, MODE_PRIVATE);
        prefs.edit().putString(username + ":" + PUBLIC_KEY, KeyManager.byteArrayToHexString(key.getEncoded())).apply();
    }

    public static PublicKey readPubKey(String username, Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(CREDENTIALS_STORAGE, MODE_PRIVATE);
        String token = prefs.getString(username + ":" + PUBLIC_KEY, null);
        if (token != null){
            try {
                return KeyManager.byteArrayToPubKey(KeyManager.hexStringToBytes(token));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        else {
            return null;
        }
    }

    public static String readToken(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(CREDENTIALS_STORAGE, MODE_PRIVATE);
        String token = prefs.getString(ACCESS_TOKEN, null);
        if (token == null)
            return "";
        else
            return token;
    }

    public static String readTUsername(Activity activity) {
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
