package pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import pt.ulisboa.tecnico.cmov.a07.p2photo.InsideAlbumActivity;

public class Dropbox_InsideAlbumActivity extends InsideAlbumActivity {

    private DropboxAuthenticationHandler mDropboxAuthenticationHandler;
    private String mDropPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDropboxAuthenticationHandler = new DropboxAuthenticationHandler(this);
        mDropPath = "/" + myName + ":" + creatorName;
    }


    @Override
    protected void onResume() {
        super.onResume();

        mDropboxAuthenticationHandler.authenticationVerification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void loadData() {
        //TODO see what to do with these spinner
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Loading");
        dialog.show();

        new DropboxListPhotosTask(DropboxClientFactory.getClient(), this, new DropboxListPhotosTask.Callback() {
            @Override
            public void onDataLoaded(ArrayList<String> paths) {
                dialog.dismiss();
                mPhotosAdapter.clear();
                for (String photoPath : paths) {
                    mPhotosAdapter.add(photoPath);
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();
                Log.e("Error", "Failed to list folder.", e);
                Toast.makeText(getApplicationContext(), "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(mDropPath);

        /*
        //TODO Add a verification for error in accessing the album's directory directory
        File currentAlbumDirectory = new File(ALBUM_BASE_FOLDER);
        mPhotoPathsList = imageReader(currentAlbumDirectory);

        //TODO in another thread I guess
        mPhotosAdapter.clear();
        for (String photoPath : mPhotoPathsList) {
            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photoPath), THUMBSIZE, THUMBSIZE);
            mPhotosAdapter.add(photoPath, thumbImage);
        }*/
    }

    @Override
    protected void uploadPhotos(Intent data) {
        String fileUri = data.getData().toString();
        new DropboxUploadFileTask(this, getApplicationContext(), DropboxClientFactory.getClient()).execute(fileUri, mDropPath);
    }

}
