package pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.fileproperties.PropertyField;
import com.dropbox.core.v2.fileproperties.PropertyFieldTemplate;
import com.dropbox.core.v2.fileproperties.PropertyGroup;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.a07.p2photo.AlbumsActivity;

public class DropboxCreateFolderTask extends AsyncTask<String, Void, String> {

    private final AppCompatActivity mActivity;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;
    private final ProgressDialog dialog;

    public DropboxCreateFolderTask(AppCompatActivity act, DbxClientV2 dbxClient, Callback callback) {
        mActivity = act;
        mDbxClient = dbxClient;
        mCallback = callback;
        dialog = new ProgressDialog(mActivity);
    }


    public interface Callback {
        void onFolderCreated(String catalogURL);
    }


    @Override
    protected void onPreExecute() {
        /*dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading");
        dialog.show();*/
    }

    @Override
    protected void onPostExecute(String catalogURL) {
        super.onPostExecute(catalogURL);
        if (mException != null) { //onError with exception
            onError(mException);
        } else if (catalogURL == null) { //onError no exception
            onError(null);
        } else { //onComplete;
            mCallback.onFolderCreated(catalogURL);

            //dialog.dismiss();

            String message = "Album created in the cloud";
            Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();

            // Reload the folder
            if(mActivity.getClass().equals(AlbumsActivity.class))
                ((AlbumsActivity) mActivity).loadData();
        }
    }

    private void onError(Exception e) {
        dialog.dismiss();

        Log.e("Error", "Failed to upload file.", e);
        Toast.makeText(mActivity, "An error has occurred", Toast.LENGTH_SHORT).show();
    }

    //Already have checked if name os album exist
    @Override
    protected String doInBackground(String... params) {
        try {
            String remoteFolder = "/" + params[0];
            String albumsCreator = params[1];

            remoteFolder += ":" + albumsCreator;
            CreateFolderResult folderResult = mDbxClient.files().createFolderV2(remoteFolder);

            InputStream inputStream = new ByteArrayInputStream("".getBytes());
            FileMetadata catalogMetadata = mDbxClient.files().uploadBuilder(remoteFolder + "/PhotosCatalog.txt")
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream);

            String catalogUrl = mDbxClient.sharing().createSharedLinkWithSettings(catalogMetadata.getPathLower()).getUrl();
            return catalogUrl.substring(0, catalogUrl.length()-1) + "1";

        } catch (DbxException | IOException e) {
            mException = e;
        }
        return null;
    }

    /*@Override
    protected FolderMetadata doInBackground(String... params) {
        try {
            String remoteFolder = "/" + params[0];
            CreateFolderResult folderResult = mDbxClient.files().createFolderV2(remoteFolder);
            //InputStream inputStream = new FileInputStream("");
            InputStream inputStream = new ByteArrayInputStream((params[1] + "\n").getBytes()); //First line of the catalog contains the Album creator
            mDbxClient.files().uploadBuilder(remoteFolder + "/" + "PhotosCatalog.txt")
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream);
            return folderResult.getMetadata();
        } catch (DbxException | IOException e) {
            mException = e;
        }
        return null;
    }*/
}