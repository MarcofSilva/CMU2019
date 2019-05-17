package pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
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

        new DropboxListPhotosTask(DropboxClientFactory.getClient(), this, new DropboxListPhotosTask.Callback() {
            @Override
            public void onDataLoaded(ArrayList<String> paths) {
                mPhotosAdapter.clear();
                for (String photoPath : paths) {
                    Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photoPath), THUMBSIZE, THUMBSIZE);
                    mPhotosAdapter.add(photoPath, thumbImage);
                }
            }

            @Override
            public void onError(Exception e) {

                Log.e("Error", "Failed to list folder.", e);
                Toast.makeText(getApplicationContext(), "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(mDropPath);
    }

    @Override
    protected void uploadPhotos(Intent data) {
        String fileUri = data.getData().toString();
        new DropboxUploadFileTask(this, getApplicationContext(), DropboxClientFactory.getClient()).execute(fileUri, mDropPath);
    }

}
