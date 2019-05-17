package pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import pt.ulisboa.tecnico.cmov.a07.p2photo.AlbumsActivity;

public class DropboxCreateFolderTask extends AsyncTask<String, Void, String> {

    private final AppCompatActivity mActivity;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public DropboxCreateFolderTask(AppCompatActivity act, DbxClientV2 dbxClient, Callback callback) {
        mActivity = act;
        mDbxClient = dbxClient;
        mCallback = callback;
    }


    public interface Callback {
        void onFolderCreated(String catalogURL) throws Exception;
    }

    @Override
    protected void onPostExecute(String catalogURL) {
        super.onPostExecute(catalogURL);
        if (mException != null) { //onError with exception
            onError(mException);
        } else if (catalogURL == null) { //onError no exception
            onError(null);
        } else { //onComplete;
            try {
                mCallback.onFolderCreated(catalogURL);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //dialog.dismiss();

            String message = "Album created in the cloud";
            Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();

            // Reload the folder
            if(mActivity.getClass().equals(AlbumsActivity.class))
                ((AlbumsActivity) mActivity).loadData();
        }
    }

    private void onError(Exception e) {

        Log.e("Error", "Failed to upload file.", e);
        Toast.makeText(mActivity, "An error has occurred", Toast.LENGTH_SHORT).show();
    }

    //Already have checked if name of album exist
    @Override
    protected String doInBackground(String... params) {
        try {
            String remoteFolder = "/" + params[0];
            String albumsCreator = params[1];

            remoteFolder += ":" + albumsCreator;

            mDbxClient.files().createFolderV2(remoteFolder);

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
}