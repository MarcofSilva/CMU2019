package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.a07.p2photo.SessionHandler;

/*
 * Async task to list items in a folder
 */
class ListFolderTask extends AsyncTask<String, Void, ArrayList<String>> {

    private String ALBUMS_BASE_PATH;

    private final AppCompatActivity mActivity;
    private final Callback mCallback;

    public interface Callback {
        void onDataLoaded(ArrayList<String> albumsInfo);
    }

    ListFolderTask(AppCompatActivity activity, Callback callback) {
        mActivity = activity;
        mCallback = callback;
        //ALBUMS_BASE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity);
        ALBUMS_BASE_PATH = mActivity.getFilesDir() + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity);
    }

    @Override
    protected void onPostExecute(ArrayList<String> albumsInfo) {
        super.onPostExecute(albumsInfo);

        mCallback.onDataLoaded(albumsInfo);
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {

        ArrayList<String> albumsInfo = new ArrayList<>();

        File baseDirectory = new File(ALBUMS_BASE_PATH);

        if(baseDirectory.exists()) {
            File[] files = baseDirectory.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    albumsInfo.add(file.getName()); // <name>:<creator>
                }
            }
        }


        File cacheDir = new File(mActivity.getCacheDir() + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity));
        if(cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    albumsInfo.add(file.getName()); // <name>:<creator>
                }
            }
        }
        return albumsInfo;

        //TODO get the photos from other users

    }
}