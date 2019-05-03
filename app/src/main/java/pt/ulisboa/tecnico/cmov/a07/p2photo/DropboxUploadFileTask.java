package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.FeatureGroupInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.paper.Folder;
import com.dropbox.core.v2.sharing.DbxUserSharingRequests;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;

public class DropboxUploadFileTask extends AsyncTask<String, Void, FileMetadata> {

    private final InsideAlbumActivity mActivity;
    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private Exception mException;
    private final ProgressDialog dialog;

    DropboxUploadFileTask(InsideAlbumActivity act, Context context, DbxClientV2 dbxClient) {
        mActivity = act;
        mContext = context;
        mDbxClient = dbxClient;
        dialog = new ProgressDialog(mActivity);
    }

    @Override
    protected void onPreExecute() {
        /*dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading");
        dialog.show();*/
        Toast.makeText(mActivity, "Uploading your photo", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        if (mException != null) { //onError with exception
            onError(mException);
        } else if (result == null) { //onError no exception
            onError(null);
        } else { //onUploadComplete;
            //dialog.dismiss();

            String message = result.getName() + " size " + result.getSize() + " modified " + DateFormat.getDateTimeInstance().format(result.getClientModified());
            Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();

            // Reload the folder
            mActivity.loadData();
        }
    }

    private void onError(Exception e) {
        //dialog.dismiss();

        Log.e("Error", "Failed to upload file.", e);
        Toast.makeText(mActivity, "An error has occurred", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected FileMetadata doInBackground(String... params) {
        String localUri = params[0];
        File localFile = UriHelper.getFileForUri(mContext, Uri.parse(localUri));

        if (localFile != null) {
            String remoteFolderPath = params[1];

            // Note - this is not ensuring the name is a valid dropbox file name
            String remoteFileName = localFile.getName();

            try {
                //Upload image
                InputStream inputStream = new FileInputStream(localFile);
                FileMetadata imageMetadata = mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);


                String catalogPath = remoteFolderPath + "/PhotosCatalog.txt";
                //TODO store the catalog in cache, is instead of this
                File localCatalogDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/P2PHOTO/" + remoteFolderPath);
                // Download the file for the append url and upload again
                if(!localCatalogDir.exists()) {
                    localCatalogDir.mkdirs();
                }
                File catalogFile = new File(localCatalogDir, "/PhotosCatalog.txt");
                catalogFile.createNewFile();
                OutputStream outputStream = new FileOutputStream(catalogFile);
                mDbxClient.files().download(catalogPath).download(outputStream);

                //Write image address in catalog
                String sharedImageUrl = mDbxClient.sharing().createSharedLinkWithSettings(imageMetadata.getPathLower()).getUrl();
                sharedImageUrl = sharedImageUrl.substring(0, sharedImageUrl.length()-1) + "1";
                FileWriter writer = new FileWriter(catalogFile, true);
                writer.append(sharedImageUrl).append("\n");
                writer.flush();
                writer.close();
                InputStream in = new FileInputStream(catalogFile);
                mDbxClient.files().uploadBuilder(remoteFolderPath + "/PhotosCatalog.txt")
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(in);

                return imageMetadata;
            } catch (DbxException | IOException e) {
                mException = e;
            }
        }

        return null;
    }
}
