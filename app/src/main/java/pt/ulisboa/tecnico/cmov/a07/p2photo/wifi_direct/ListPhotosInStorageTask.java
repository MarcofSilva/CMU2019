package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;


import java.io.File;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.a07.p2photo.SessionHandler;

/*
 * Async task to list items in a folder
 */
public class ListPhotosInStorageTask extends AsyncTask<String, Void, ArrayList<String>> {

    private String ALBUM_BASE_FOLDER;


    private final WifiDirect_InsideAlbumActivity mActivity;
    private final ListPhotosInStorageTask.Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(ArrayList<String> result);

        void onError(Exception e);
    }

    public ListPhotosInStorageTask(WifiDirect_InsideAlbumActivity act, ListPhotosInStorageTask.Callback callback) {
        mActivity = act;
        mCallback = callback;
        //ALBUM_BASE_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity);
        ALBUM_BASE_FOLDER = mActivity.getFilesDir() + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity);
    }

    @Override
    protected void onPostExecute(ArrayList<String> paths) {
        super.onPostExecute(paths);

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(paths);
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected ArrayList<String> doInBackground(String... params) {

        String albumName = params[0];
        String creatorName = params[1];

        String albumFolderPath = ALBUM_BASE_FOLDER + "/" + albumName + ":" + creatorName;

        //TODO inside threads should downloading and parsing catalogs at the same time that this thread would returning the images to the UI that already as been downloaded
        //In continuation of this idea the thread should return the images one by one as their are downloaded, instead of at the end (onProgress)
        ArrayList<String> imagesPaths = new ArrayList<>();
        ArrayList<String> photosUrls = new ArrayList<>();
        try {

            //-------- Get photos from our own device --------
            //TODO Add a verification for error in accessing the album's directory
            File currentAlbumDirectory = new File(albumFolderPath);
            ArrayList<String> mPhotoPathsList = imageReader(currentAlbumDirectory);

            String albumFolderCachePath = mActivity.getCacheDir() + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity) + "/" + albumName + ":" + creatorName;
            File albumFolderCachePathFile = new File(albumFolderCachePath);
            mPhotoPathsList.addAll(imageReader(albumFolderCachePathFile));

            //-------- Get photos from other users devices ---
            // TODO stuff from other devices, need to know how the wifi direct will work first


            return imagesPaths = mPhotoPathsList;
        } catch (Exception e) {
            mException = e;
        }
        return null;
    }

    private ArrayList<String> imageReader(File base) {
        ArrayList<String> res = new ArrayList<>();
        File[] files = base.listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    res.addAll(imageReader(file));
                } else if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
                    res.add(file.getPath());
                }
            }
        }
        return res;
    }
}
