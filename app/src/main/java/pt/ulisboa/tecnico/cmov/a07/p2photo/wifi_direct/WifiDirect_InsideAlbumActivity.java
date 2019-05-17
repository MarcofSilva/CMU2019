package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.a07.p2photo.InsideAlbumActivity;

public class WifiDirect_InsideAlbumActivity extends InsideAlbumActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void loadData() {

        new ListPhotosInStorageTask( this, new ListPhotosInStorageTask.Callback() {
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
        }).execute(myName, creatorName);
    }

    //Upload photos to smartphone storage
    @Override
    protected void uploadPhotos(Intent data) {
        if(data.getData() == null)
            return;

        String fileUri = data.getData().toString();
        new CopyPhotoToAlbumTask(this).execute(fileUri, myName + ":" + creatorName);
    }
}
