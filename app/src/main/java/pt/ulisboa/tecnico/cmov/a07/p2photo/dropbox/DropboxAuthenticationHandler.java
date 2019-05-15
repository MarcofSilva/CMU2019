package pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.dropbox.core.android.Auth;

import pt.ulisboa.tecnico.cmov.a07.p2photo.R;

import static android.content.Context.MODE_PRIVATE;

public class DropboxAuthenticationHandler {

    //Constants related with dropbox credentials and token
    private static final String DROPBOX_CREDENTIALS_STORAGE = "dropbox_credentials";
    private static final String DROPBOX_ACCESS_TOKEN = "dropbox_access_token";
    private static final String DROPBOX_USER_ID = "dropbox_user_id";

    private Activity mActivity;

    DropboxAuthenticationHandler(Activity activity) {
        mActivity = activity;
    }

    void authenticationVerification() {
        //Check dropbox credentials and token of a session
        SharedPreferences prefs = mActivity.getSharedPreferences(DROPBOX_CREDENTIALS_STORAGE, MODE_PRIVATE);
        String accessToken = prefs.getString(DROPBOX_ACCESS_TOKEN, null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString(DROPBOX_ACCESS_TOKEN, accessToken).apply();
                initAndLoadData(accessToken);
            }
        } else {
            initAndLoadData(accessToken);
        }

        String uid = Auth.getUid();
        String storedUid = prefs.getString(DROPBOX_USER_ID, null);
        if (uid != null && !uid.equals(storedUid)) {
            prefs.edit().putString(DROPBOX_USER_ID, uid).apply();
        }

        //Authenticate in dropbox account
        if(!hasToken()) {
            Auth.startOAuth2Authentication(mActivity.getApplicationContext(), mActivity.getString(R.string.dropbox_app_key));
        }
        else {
            //TODO change this as a way to show the info about the account already logged in dropbox
            //Toast.makeText(AlbumsActivity.this, "", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasToken() {
        SharedPreferences prefs = mActivity.getSharedPreferences(DROPBOX_CREDENTIALS_STORAGE, MODE_PRIVATE);
        String accessToken = prefs.getString(DROPBOX_ACCESS_TOKEN, null);
        return accessToken != null;
    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        if(mActivity.getClass().isInstance(Dropbox_AlbumsActivity.class)) {
            Dropbox_AlbumsActivity d_act = (Dropbox_AlbumsActivity) mActivity;
            d_act.loadData();
        }
        else if(mActivity.getClass().isInstance(Dropbox_InsideAlbumActivity.class)) {
            Dropbox_InsideAlbumActivity d_act = (Dropbox_InsideAlbumActivity) mActivity;
            d_act.loadData();
        }
    }

    //Remove token from storage has a way
    public static void cleanDropboxCredentials(Activity mActivity) {
        //Remove dropbox token, as a way to make every user that is logging in to login to their dropbox account, instead of already having a dropbox account associated from previous logins
        SharedPreferences prefs = mActivity.getSharedPreferences(DROPBOX_CREDENTIALS_STORAGE, Context.MODE_PRIVATE);
        prefs.edit().remove(DROPBOX_ACCESS_TOKEN).apply();
    }

}
