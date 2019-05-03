package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DropboxDowloadFileTask extends AsyncTask<FileMetadata, Void, File> {

    private final InsideAlbumActivity mActivity;
    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private Exception mException;
    final ProgressDialog dialog;

    DropboxDowloadFileTask(InsideAlbumActivity act, Context context, DbxClientV2 dbxClient) {
        mActivity = act;
        mContext = context;
        mDbxClient = dbxClient;
        dialog = new ProgressDialog(mActivity);
    }

    @Override
    protected void onPreExecute() {
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Downloading");
        dialog.show();
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            onError(mException);
        } else { //onDownloadComplete
            dialog.dismiss();
            if (result != null) {
                mActivity.viewFileInExternalApp(result); //TODO
            }
        }
    }

    private void onError(Exception e) {
        dialog.dismiss();

        Log.e("Error", "Failed to download file.", e);
        Toast.makeText(mActivity,
                "An error has occurred",
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    protected File doInBackground(FileMetadata... params) {
        FileMetadata metadata = params[0];
        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, metadata.getName());

            // Make sure the Downloads directory exists.
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    mException = new RuntimeException("Unable to create directory: " + path);
                }
            } else if (!path.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + path);
                return null;
            }

            // Download the file.
            try (OutputStream outputStream = new FileOutputStream(file)) {
                mDbxClient.files().download(metadata.getPathLower(), metadata.getRev())
                        .download(outputStream);
            }

            // Tell android about the file
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            mContext.sendBroadcast(intent);

            return file;
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}
