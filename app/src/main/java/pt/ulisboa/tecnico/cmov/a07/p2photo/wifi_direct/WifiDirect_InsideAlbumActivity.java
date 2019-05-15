package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.a07.p2photo.InsideAlbumActivity;
import pt.ulisboa.tecnico.cmov.a07.p2photo.UriHelper;

public class WifiDirect_InsideAlbumActivity extends InsideAlbumActivity {

    private String ALBUM_BASE_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/P2PHOTO/" + myName;
    private final int THUMBSIZE = 256;


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void loadData() {

    }

    //Upload photos to smartphone storage
    @Override
    protected void uploadPhotos(Intent data) {
        /*Uri[] imageUris; TODO check adapter because it now is different

        ClipData images = data.getClipData();
        if(images == null) {
            imageUris = new Uri[1];
            imageUris[0] = data.getData();
        }
        else {
            int numPhotos = images.getItemCount();
            imageUris = new Uri[numPhotos];
            for(int i = 0; i < numPhotos; i++)
                imageUris[i] = images.getItemAt(i).getUri();
        }
        for (Uri imageUri : imageUris) {
            if (imageUri != null) {
                File originImage = UriHelper.getFileForUri(getApplicationContext(), imageUri);
                if (originImage != null && originImage.exists()) {
                    //Get filename
                    String filename = originImage.getName();
                    File addedImage = new File(ALBUM_BASE_FOLDER + "/" + filename);

                    copyPhoto(originImage, addedImage, filename);

                    String addedImagePath = addedImage.getPath();
                    Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(addedImagePath), THUMBSIZE, THUMBSIZE);
                    mPhotosAdapter.add(addedImagePath, thumbImage);
                }
            }
        }*/
    }

    private void copyPhoto(File originImage, File addedImage, String filename) {
        try {
            addedImage = createNewPhoto(addedImage, filename);

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

    private File createNewPhoto(File image, String filename) throws IOException {
        File newImage = image;
        boolean result = image.createNewFile();
        if(!result) {
            String[] filenameSplitted = filename.split(".");
            String newFilename = filenameSplitted[0] + "(copy)." + filenameSplitted[1];
            newImage = new File(ALBUM_BASE_FOLDER + "/" + newFilename);
            return createNewPhoto(newImage, newFilename);
        }
        return newImage;
    }

    // TODO
    private ArrayList<String> imageReader(File base) {
        ArrayList<String> res = new ArrayList<>();
        File[] files = base.listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                res.addAll(imageReader(file));
            }
            else if(file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
                res.add(file.getPath());
            }
        }
        return res;
    }
}
