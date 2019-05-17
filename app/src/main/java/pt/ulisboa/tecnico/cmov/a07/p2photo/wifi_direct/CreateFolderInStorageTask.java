package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import pt.ulisboa.tecnico.cmov.a07.p2photo.AlbumsActivity;
import pt.ulisboa.tecnico.cmov.a07.p2photo.SessionHandler;

public class CreateFolderInStorageTask extends AsyncTask<String, Void, Void> {

    private String ALBUMS_BASE_PATH;

    private final AppCompatActivity mActivity;
    private Callback mCallback;
    private Exception mException;

    public CreateFolderInStorageTask(AppCompatActivity act, Callback callback) {
        mActivity = act;
        mCallback = callback;
        //ALBUMS_BASE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity);
        ALBUMS_BASE_PATH = mActivity.getFilesDir() + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity);
    }

    public interface Callback {
        void onFolderCreated();
    }

    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mException != null) { //onError with exception
            Toast.makeText(mActivity, "Something went wrong, when creating the catalog file", Toast.LENGTH_LONG).show();
        } else { //onComplete;
            String message = "Album created in smartphone";
            Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
            mCallback.onFolderCreated();

            // Reload the folder
            if(mActivity.getClass().equals(WifiDirect_AlbumsActivity.class))
                ((WifiDirect_AlbumsActivity) mActivity).loadData();
        }
    }

    //Already have checked if name of album exist
    @Override
    protected Void doInBackground(String... params) {
        // Create album in storage
        try {
            String albumFolderID = params[0];
            String albumsCreator = params[1];
            albumFolderID += ":" + albumsCreator;

            //Create directory if it not exist
            File albumFolder = new File(ALBUMS_BASE_PATH + "/" + albumFolderID);
            if(!albumFolder.exists()) {
                albumFolder.mkdirs();
            }

            File catalogFile = new File(albumFolder.getPath(), albumFolderID + ".txt");
            boolean result = catalogFile.createNewFile();
            if (!result) {
                catalogFile.delete();
            }

        } catch (IOException e) {
            mException = e;
        }
        return null;
    }
}