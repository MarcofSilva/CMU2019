package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.Activity;
import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NetworkHandler {

    //Storage filename
    private static final String TOKEN_FILENAME = "AuthToken";

    static String convertStreamToString(InputStream is) {
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

    static void writeTokenFile(String string, Activity activity) throws Exception {
        FileOutputStream fos = activity.openFileOutput(TOKEN_FILENAME, Context.MODE_PRIVATE);
        fos.write(string.getBytes());
        fos.close();
    }

    static String readToken(Activity activity) {
        //TODO missing security
        FileInputStream fis = null;
        String token;
        try {
            fis = activity.openFileInput(TOKEN_FILENAME);
            token = NetworkHandler.convertStreamToString(fis);
            fis.close();
        } catch (Exception e) {
            return "";
        }
        return token;
    }
}
