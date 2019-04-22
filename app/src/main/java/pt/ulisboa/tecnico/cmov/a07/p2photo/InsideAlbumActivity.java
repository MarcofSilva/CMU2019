package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.ThumbnailFormat;
import com.dropbox.core.v2.files.ThumbnailSize;

import java.io.File; //TODO nudar para o File do dropbox
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class InsideAlbumActivity extends DropboxActivity {

    private static final String USERNAMES_EXTRA = "usernames";
    private static final int PICKPHOTO_REQUEST_CODE = 10;
    private static final int FIND_USERS_REQUEST_CODE = 2;

    private final int THUMBSIZE = 256;


    private String ALBUM_BASE_FOLDER;

    private ArrayList<String> mPhotoPathsList;
    private CustomPhotosAdapter mPhotosAdapter;

    protected String myName;
    private String mDropPath;
    private AddUsersToAlbumTask mAddUsersToAlbum = null;
    private TextView mAlbumTitleView = null;
    private GridView mPhotosGridView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inside_album);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent albumIntent = getIntent();
        myName = albumIntent.getStringExtra("myName");
        mDropPath = "/" + myName;
        mAlbumTitleView = findViewById(R.id.inside_AlbumTitle);
        mAlbumTitleView.setText(myName);

        ALBUM_BASE_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/P2PHOTO/" + myName;

        mPhotosAdapter = new CustomPhotosAdapter(this);

        mPhotosGridView = findViewById(R.id.photoGrid_insideAlbum);
        mPhotosGridView.setAdapter(mPhotosAdapter);
        mPhotosGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO take care of previewing the image
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab_inside_album);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(InsideAlbumActivity.this);
                dialogBuilder
                        .setPositiveButton("Add Photos", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                launchPhotoChooser();
                            }
                        })
                        .setNeutralButton("Add Users", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent addUsersIntent = new Intent(getApplicationContext(), FindUsersActivity.class);
                                startActivityForResult(addUsersIntent, FIND_USERS_REQUEST_CODE);
                            }
                        })
                        ;
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    protected void loadData() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Loading");
        dialog.show();

        new ListDropboxPhotosTask(DropboxClientFactory.getClient(), new ListDropboxPhotosTask.Callback() {
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
                Toast.makeText(InsideAlbumActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
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

    private void uploadPhotos(String fileUri) {

        new DropboxUploadFileTask(this, this, DropboxClientFactory.getClient()).execute(fileUri, mDropPath);
    }

    private void downloadFile(FileMetadata file) {

        new DropboxDowloadFileTask(this, this, DropboxClientFactory.getClient()).execute(file);
    }

    private void launchPhotoChooser() {
        // Launch intent to pick photos for upload
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); //TODO change to true if want the ability to select multiple
        intent.setType("image/*");
        startActivityForResult(intent, PICKPHOTO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Result of photo chooser
        if (requestCode == PICKPHOTO_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            //TODO smartphone storage
            //addPhotosToAlbum(data);

            //Upload to dropbox
            uploadPhotos(data.getData().toString());
        }
        else if(requestCode == FIND_USERS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> usernames = data.getStringArrayListExtra(USERNAMES_EXTRA);
            mAddUsersToAlbum = new AddUsersToAlbumTask(usernames, myName ,this);
            mAddUsersToAlbum.execute((Void) null);
        }
    }

    //TODO what to do, see this
    protected void viewFileInExternalApp(File result) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = result.getName().substring(result.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);

        intent.setDataAndType(Uri.fromFile(result), type);

        // Check for a handler first to avoid a crash
        PackageManager manager = getPackageManager();
        List<ResolveInfo> resolveInfo = manager.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            startActivity(intent);
        }
    }

    //In smartphone storage
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

    public AddUsersToAlbumTask getmAddUsersToAlbum() {
        return mAddUsersToAlbum;
    }

    public void setmAddUsersToAlbum(AddUsersToAlbumTask AddUsersToAlbum) {
        this.mAddUsersToAlbum = AddUsersToAlbum;
    }

    //In smartphone storage
    /*private void addPhotosToAlbum(Intent data) {
        Uri[] imageUris;

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
        }
    }*/

    //In smartphone storage
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

    //In smartphone storage
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
}




class CustomPhotosAdapter extends BaseAdapter {

    private final List<String> _photosPaths;
    //private final List<Bitmap> _photosThumbnails;

    private final Activity _activity;

    public CustomPhotosAdapter(Activity act, ArrayList<String> photoPaths, ArrayList<Bitmap> photosThumbnails) {
        this._activity = act;
        _photosPaths = photoPaths;
        //_photosThumbnails = photosThumbnails;
    }

    public CustomPhotosAdapter(Activity act) {
        this._activity = act;
        _photosPaths = new ArrayList<>();
        //_photosThumbnails = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return _photosPaths.size();
    }

    @Override
    public Object getItem(int position) {
        return _photosPaths.get(position);
    }

    public void clear() {
        _photosPaths.clear();
        //_photosThumbnails.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<String> photoPaths) {
        _photosPaths.addAll(photoPaths);
        //_photosThumbnails.addAll(photoThumbnail);
        notifyDataSetChanged();
    }

    public void add(String photoPath){
        _photosPaths.add(photoPath);
        //_photosThumbnails.add(photoThumbnail);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        ImageViewHolder viewHolder;

        if( convertView == null) {
            view = _activity.getLayoutInflater().inflate(R.layout.inside_album_item, parent, false);
            viewHolder = new ImageViewHolder(view);
            view.setTag(viewHolder); //Store the view holder in the view
        } else {
            view = convertView;
            viewHolder = (ImageViewHolder) view.getTag();//In this case a view is being "aproveitada" and we can get the view holder from de view
        }

        viewHolder.imageView.setImageURI(Uri.parse(_photosPaths.get(position)));
        //viewHolder.imageView.setImageBitmap(_photosThumbnails.get(position));

        return view;
    }
}

class ImageViewHolder {

    final ImageView imageView;

    public ImageViewHolder(View view) {
        imageView = view.findViewById(R.id.photo);
    }
}





/*
 * Async task to list items in a folder
 */
class ListDropboxPhotosTask extends AsyncTask<String, Void, ArrayList<String>> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(ArrayList<String> result);

        void onError(Exception e);
    }

    public ListDropboxPhotosTask(DbxClientV2 dbxClient, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
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

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        DbxDownloader<FileMetadata> downloader = null;
        ArrayList<String> thumbnailsPaths = new ArrayList<>();
        try {
            //Path were the thumbnails downloaded will be stored
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/P2PHOTO/Thumbnails" + params[0]; //Get the albums name from the path in the drop
            File folderPath = new File(path);
            if(!folderPath.exists()) {
                folderPath.mkdirs();
            }
            ListFolderResult listFiles = mDbxClient.files().listFolder(params[0]);
            for(Metadata fileMetadata : listFiles.getEntries()) {
                File file = new File(path, fileMetadata.getName());
                if(fileMetadata.getName().contains("PhotosCatalog")) {
                    continue;
                }
                else if(!file.exists()) {
                    file.createNewFile();
                    downloader = mDbxClient.files().getThumbnailBuilder(fileMetadata.getPathLower())
                            .withFormat(ThumbnailFormat.JPEG)
                            .withSize(ThumbnailSize.W480H320)
                            .start();
                    OutputStream outputStream = new FileOutputStream(file);
                    downloader.download(outputStream);
                }
                thumbnailsPaths.add(file.getPath());
            }
            return thumbnailsPaths;
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


