package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.dropbox.core.android.Auth;


// Base class for Activities that require auth tokens
// Will redirect to auth flow if needed
public abstract class DropboxActivity extends AppCompatActivity {

    private static final String DROPBOX_CREDENTIALS_STORAGE = "dropbox_credentials";
    private static final String DROPBOX_ACCESS_TOKEN = "dropbox_access_token";
    private static final String DROPBOX_USER_ID = "dropbox_user_id";

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(DROPBOX_CREDENTIALS_STORAGE, MODE_PRIVATE);
        String accessToken = prefs.getString(DROPBOX_ACCESS_TOKEN, null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString(DROPBOX_ACCESS_TOKEN, accessToken).apply();
                initAndLoadData(accessToken); //TODO eventualmente, algo vai ter de acontecer aqui
            }
        } else {
            initAndLoadData(accessToken); //TODO eventualmente, algo vai ter de acontecer aqui
        }

        String uid = Auth.getUid();
        String storedUid = prefs.getString(DROPBOX_USER_ID, null);
        if (uid != null && !uid.equals(storedUid)) {
            prefs.edit().putString(DROPBOX_USER_ID, uid).apply();
        }
    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        //PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient()); TODO vai ter de ser feito, o que quer que seja, manualmente
        loadData();
    }

    protected abstract void loadData();

    protected boolean hasToken() {
        SharedPreferences prefs = getSharedPreferences(DROPBOX_CREDENTIALS_STORAGE, MODE_PRIVATE);
        String accessToken = prefs.getString(DROPBOX_ACCESS_TOKEN, null);
        return accessToken != null;
    }
}
