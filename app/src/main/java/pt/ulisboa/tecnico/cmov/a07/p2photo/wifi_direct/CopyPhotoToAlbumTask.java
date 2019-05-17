package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pt.ulisboa.tecnico.cmov.a07.p2photo.InsideAlbumActivity;
import pt.ulisboa.tecnico.cmov.a07.p2photo.SessionHandler;
import pt.ulisboa.tecnico.cmov.a07.p2photo.UriHelper;

public class CopyPhotoToAlbumTask extends AsyncTask<String, Void, File> {

    private String ALBUMS_BASE_PATH;

    private final InsideAlbumActivity mActivity;
    private Exception mException;

    public CopyPhotoToAlbumTask(InsideAlbumActivity act) {
        mActivity = act;
        //ALBUMS_BASE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity);
        ALBUMS_BASE_PATH = mActivity.getFilesDir() + "/P2PHOTO/" + SessionHandler.readTUsername(mActivity);
    }

    @Override
    protected void onPreExecute() {
        Toast.makeText(mActivity, "Uploading your photo", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostExecute(File copiedImage) {
        super.onPostExecute(copiedImage);
        if (mException != null) { //onError with exception
            onError(mException);
        } else if (copiedImage == null) { //onError no exception
            onError(null);
        } else { //onUploadComplete;

            String message = copiedImage.getName() + " added to the album";
            Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();

            // Reload the folder
            mActivity.loadData();
        }
    }

    private void onError(Exception e) {
        Log.e("Error", "Failed to upload file.", e);
        Toast.makeText(mActivity, "An error has occurred", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected File doInBackground(String... params) {
        String localUri = params[0];
        File originImage = UriHelper.getFileForUri(mActivity.getApplicationContext(), Uri.parse(localUri));

        if (originImage != null) {
            String destFolderPath = ALBUMS_BASE_PATH + "/" + params[1];

            String fileName = originImage.getName();

            File copiedImage = new File(destFolderPath + "/" + fileName);

            copyPhoto(originImage, copiedImage, fileName, destFolderPath);

            String catalogPath = destFolderPath + "/PhotosCatalog.txt";

            try {
                File catalogFile = new File(catalogPath);
                if (!catalogFile.exists()) {
                    catalogFile.createNewFile();
                }
                OutputStream outputStream = new FileOutputStream(catalogFile);

                //Write image address in catalog
                FileWriter writer = new FileWriter(catalogFile, true);
                writer.append(fileName).append("\n");
                writer.flush();
                writer.close();

                return copiedImage;
            } catch (IOException e) {
                mException = e;
            }
        }
        return null;
    }

    private void copyPhoto(File originImage, File addedImage, String filename, String albumBaseFolder) {
        try {
            addedImage = createNewPhoto(addedImage, filename, albumBaseFolder);

            InputStream in = new FileInputStream(originImage);
            OutputStream out = new FileOutputStream(addedImage);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

        } catch (Exception e) {
            Log.e("Exceptions", "Exception " + e + ": " + e.getMessage());
        }
    }

    private File createNewPhoto(File image, String filename, String albumBaseFolder) throws IOException {
        File newImage = image;
        boolean result = image.createNewFile();
        if(!result) {
            String[] filenameSplitted = filename.split(".");
            String newFilename = filenameSplitted[0] + "(copy)." + filenameSplitted[1];
            newImage = new File(albumBaseFolder + "/" + newFilename);
            return createNewPhoto(newImage, newFilename, albumBaseFolder);
        }
        return newImage;
    }
}