package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File; //TODO nudar para o File do dropbox
import java.util.ArrayList;
import java.util.List;


public abstract class InsideAlbumActivity extends AppCompatActivity {
    private static final String USERNAMES_EXTRA = "usernames";
    private static final int PICKPHOTO_REQUEST_CODE = 10;
    private static final int FIND_USERS_REQUEST_CODE = 2;


    protected CustomPhotosAdapter mPhotosAdapter;

    protected String myName;
    protected String creatorName;
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
        creatorName = albumIntent.getStringExtra("albumCreator");

        mAlbumTitleView = findViewById(R.id.inside_AlbumTitle);
        mAlbumTitleView.setText(myName);

        mPhotosAdapter = new CustomPhotosAdapter(this);

        mPhotosGridView = findViewById(R.id.photoGrid_insideAlbum);
        mPhotosGridView.setAdapter(mPhotosAdapter);
        mPhotosGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Previewing the image
                String imagePath = (String) mPhotosAdapter.getItem(position);
                File imageToPreview = new File(imagePath);
                viewFileInExternalApp(imageToPreview);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab_inside_album);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(InsideAlbumActivity.this);
                dialogBuilder
                        .setPositiveButton("Add Photo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                launchPhotoChooser();
                            }
                        })
                        .setNeutralButton("Add Users", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(creatorName.equals(SessionHandler.readTUsername(InsideAlbumActivity.this))) {
                                    Intent addUsersIntent = new Intent(getApplicationContext(), FindUsersActivity.class);
                                    addUsersIntent.putExtra("albumName", myName);
                                    startActivityForResult(addUsersIntent, FIND_USERS_REQUEST_CODE);
                                }
                                else {
                                    Toast.makeText(InsideAlbumActivity.this, "Can´t add users to an album not created by you", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        ;
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public abstract void loadData();

    protected abstract void uploadPhotos(Intent data);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Result of photo chooser
        if (requestCode == PICKPHOTO_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //Upload to dropbox or add to storage
            uploadPhotos(data);
        }
        // Result of users chooser
        else if(requestCode == FIND_USERS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> usernames = data.getStringArrayListExtra(USERNAMES_EXTRA);
            ContextClass context = (ContextClass) getApplicationContext();
            String appMode = context.getAppMode();
            mAddUsersToAlbum = new AddUsersToAlbumTask(usernames, myName , appMode,this);
            mAddUsersToAlbum.execute((Void) null);
        }
    }

    private void launchPhotoChooser() {
        // Launch intent to pick photos for upload
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); //Change to true if want the ability to select multiple
        intent.setType("image/*");
        startActivityForResult(intent, PICKPHOTO_REQUEST_CODE);
    }

    private void viewFileInExternalApp(File result) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = result.getName().substring(result.getName().indexOf(".") + 1);
        String mimeType = mime.getMimeTypeFromExtension(ext);

        File f = result;
        String a = getApplicationContext().getPackageName() + ".provider";


        Uri apkURI;

        //ContextClass contextClass = (ContextClass) getApplicationContext();
        //if(contextClass.getAppMode().equals(getString(R.string.AppModeDropBox))) {
        //    apkURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".providerDropbox", result);
        //}
        //else { //AppMode_WifiDirect
            apkURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", result);
        //}
        intent.setDataAndType(apkURI, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Check for a handler first to avoid a crash
        PackageManager manager = getPackageManager();
        List<ResolveInfo> resolveInfo = manager.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            startActivity(intent);
        }
    }


    // Getters and Setters -------------

    public String getAlbumName() {
        return myName;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public AddUsersToAlbumTask getmAddUsersToAlbum() {
        return mAddUsersToAlbum;
    }

    public void setAddUsersToAlbum(AddUsersToAlbumTask AddUsersToAlbum) {
        this.mAddUsersToAlbum = AddUsersToAlbum;
    }


    //Adapter for photos grid view
    protected class CustomPhotosAdapter extends BaseAdapter {

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

    protected class ImageViewHolder {

        final ImageView imageView;

        public ImageViewHolder(View view) {
            imageView = view.findViewById(R.id.photo);
        }
    }
}