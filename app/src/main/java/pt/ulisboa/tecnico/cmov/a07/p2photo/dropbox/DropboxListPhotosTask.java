package pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Environment;
import android.se.omapi.Session;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import pt.ulisboa.tecnico.cmov.a07.p2photo.InsideAlbumActivity;
import pt.ulisboa.tecnico.cmov.a07.p2photo.SessionHandler;
import pt.ulisboa.tecnico.cmov.a07.p2photo.R;
import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.Security.KeyManager;

/*
 * Async task to list items in a folder
 */
public class DropboxListPhotosTask extends AsyncTask<String, Void, ArrayList<String>> {

    private final DbxClientV2 mDbxClient;
    private final Dropbox_InsideAlbumActivity mActivity;
    private final DropboxListPhotosTask.Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(ArrayList<String> result);

        void onError(Exception e);
    }

    public DropboxListPhotosTask(DbxClientV2 dbxClient, Dropbox_InsideAlbumActivity act, DropboxListPhotosTask.Callback callback) {
        mDbxClient = dbxClient;
        mActivity = act;
        mCallback = callback; }

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
        //TODO inside threads should downloading and parsing catalogs at the same time that this thread would returning the images to the UI that already as been downloaded
        //In continuation of this idea the thread should return the images one by one as their are downloaded, instead of at the end (onProgress)
        DbxDownloader<FileMetadata> downloader = null;
        ArrayList<String> imagesPaths = new ArrayList<>();
        ArrayList<String> photosUrls = new ArrayList<>();
        try {

            final File inCache_basePath = mActivity.getApplicationContext().getCacheDir();

            //Path were the thumbnails downloaded will be stored
            //TODO if secure delete commented
            //String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity) + params[0]; //Get the albums name from the path in the drop
            String path = inCache_basePath + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity) + params[0]; //Get the albums name from the path in the drop
            File folderPath = new File(path);
            if(!folderPath.exists()) {
                folderPath.mkdirs();
            }

            //-------- Get photos from our own cloud --------
            //Get references of files in dropbox
            ListFolderResult listFiles = mDbxClient.files().listFolder(params[0]);
            for(Metadata metadata : listFiles.getEntries()) {
                FileMetadata fileMetadata = (FileMetadata) metadata;
                File file = new File(path, fileMetadata.getName());
                if(fileMetadata.getName().contains("PhotosCatalog")) {
                    /*if(mActivity.creatorName == null) {TODO apagar?
                        //fileMetadata.getPropertyGroups();
                        mDbxClient.files().download(fileMetadata.getPathLower(), fileMetadata.getRev()).download(new ByteArrayOutputStream(4048)).getPropertyGroups().get(0).getFields().get(0).getValue();
                    }*/
                    continue;
                }
                // Only if a no file in the smartphone directory of dowloads already has this name then download it
                else if(!file.exists()) {
                    file.createNewFile();
                    OutputStream outputStream = new FileOutputStream(file);
                    /*downloader = mDbxClient.files().getThumbnailBuilder(fileMetadata.getPathLower())
                            .withFormat(ThumbnailFormat.JPEG)
                            .withSize(ThumbnailSize.W256H256)
                            .start();
                    downloader.download(outputStream);*/
                    // Download the file.
                    mDbxClient.files().download(fileMetadata.getPathLower(), fileMetadata.getRev()).download(outputStream);

                }
                imagesPaths.add(file.getPath());
            }

            //-------- Get photos from other users clouds --------
            //Ask the server for the other users catalog addresses
            URL url = new URL(mActivity.getString(R.string.serverAddress) + "/getCatalogUrls?albumName=" + mActivity.getAlbumName() + "&albumUser=" + mActivity.getCreatorName());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(false);
            conn.setRequestProperty("Authorization", SessionHandler.readToken(mActivity));
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String response = SessionHandler.convertStreamToString(in);

            ArrayList<String> catalogUrls = new ArrayList<>();
            String[] encriptedUrls = response.split(";");
            for(String s : encriptedUrls){
                String catalogDecrypt = KeyManager.decrypt(mActivity.getAlbumName(), s);
                catalogUrls.add(catalogDecrypt);
            }

            for(String index : catalogUrls) {
                URL indexUrl = new URL(index);
                HttpsURLConnection connection = (HttpsURLConnection) indexUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(false);
                InputStream input = new BufferedInputStream(connection.getInputStream());

                File catalogFile = new File(inCache_basePath + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity) + "/RemoteTemporaryCatalogs", "RemotePhotosCatalog.txt");
                File folder = new File(catalogFile.getParent());
                if(!folder.exists()) {
                    folder.mkdirs();
                }
                catalogFile.createNewFile();
                OutputStream output = new FileOutputStream(catalogFile);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;
                while ((len = input.read(buf)) > 0) {
                    output.write(buf, 0, len);
                }
                input.close();
                output.close();

                //Read urls
                BufferedReader reader = new BufferedReader(new FileReader(catalogFile));
                photosUrls.add(reader.readLine());
                reader.close();
            }

            for(String photoUrl : photosUrls) {
                if(photoUrl == null) {
                    break;
                }
                URL indexUrl = new URL(photoUrl);
                HttpsURLConnection connection = (HttpsURLConnection) indexUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(false);
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());

                //Get file name
                String[] urlSplitted = photoUrl.split("\\?")[0].split("/");
                String imageName = urlSplitted[urlSplitted.length - 1];

                File imageFile = new File(inCache_basePath + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity) + params[0], imageName);
                if(!imageFile.exists()) {
                    imageFile.createNewFile();
                    OutputStream output = new FileOutputStream(imageFile);

                    // Copy the bits from instream to outstream
                    byte[] buf = new byte[4096000];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        output.write(buf, 0, len);
                    }
                    inputStream.close();
                    output.close();
                }
                imagesPaths.add(imageFile.getPath());
            }

            return imagesPaths;
        } catch (Exception e) {
            mException = e;
        }
        finally {
            if(downloader != null)
                downloader.close();
        }

        return null;
    }
}
