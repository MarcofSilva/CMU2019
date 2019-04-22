package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.ThumbnailFormat;
import com.dropbox.core.v2.files.ThumbnailSize;
import com.dropbox.core.v2.files.WriteMode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class AlbumsActivity extends DropboxActivity implements NavigationView.OnNavigationItemSelectedListener, ServiceConnection {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private static final String ALBUM_BASE_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/P2PHOTO";


    //Intent extra tag
    private static final String USERNAME_EXTRA = "username";

    //TODO cuidado
    private static final String DUMMYURL = "www.pornhub.com";

    public static final String BROADCAST_ACTION = "pt.ulisboa.tecnico.updating";

    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";

    private UserLogoutTask mLogout = null;
    private CreateAlbumTask mCreateAlb = null;

    private UpdateService myService;
    private MyBroadCastReceiver myBroadCastReceiver;

    // UI references.
    private TextView mUsernameView;
    private CustomAlbumsAdapter mAlbumsAdapter;
    private GridView mAlbumsGridView;
    private DropboxCreateFolderTask mDropboxCreateAlbumTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAlbumsAdapter = new CustomAlbumsAdapter(this);

        mAlbumsGridView = findViewById(R.id.photoGrid_Albums);
        mAlbumsGridView.setAdapter(mAlbumsAdapter);
        mAlbumsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent newAlbumIntent = new Intent(getApplicationContext(), InsideAlbumActivity.class);

                AlbumInfoViewHolder holder = (AlbumInfoViewHolder) view.getTag();
                String clickedAlbum = holder.albumTitle.getText().toString();
                newAlbumIntent.putExtra("myName", clickedAlbum);

                startActivity(newAlbumIntent);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_albums);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AlbumsActivity.this);
                final EditText input = new EditText(AlbumsActivity.this);
                input.setHint("Album's name"); //TODO security issue, se escreverem um nome tipo ../cenas podem explorar diretorias que nao deveriam, pelo menos na storage no smartphone
                dialogBuilder.setTitle("Create New Album")
                        .setView(input)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String albumName = input.getText().toString();

                                if(TextUtils.isEmpty(albumName)) {
                                    Toast.makeText(AlbumsActivity.this, getString(R.string.error_albumName_required), Toast.LENGTH_LONG).show();
                                }
                                else if (mAlbumsAdapter.containsAlbumName(albumName)) {
                                    Toast.makeText(AlbumsActivity.this, "That Album already exists", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    mDropboxCreateAlbumTask = new DropboxCreateFolderTask(AlbumsActivity.this, AlbumsActivity.this, DropboxClientFactory.getClient());
                                    mDropboxCreateAlbumTask.execute(albumName);
                                    //createAlbumInStorage(albumName);

                                    String url = DUMMYURL;
                                    mCreateAlb = new CreateAlbumTask(albumName, url, AlbumsActivity.this);
                                    mCreateAlb.execute((Void) null);
                                }
                                loadData();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();

                            }
                        });
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Get navigation view header reference
        View headerView = navigationView.getHeaderView(0);

        //Put the username in the nav tab
        mUsernameView = headerView.findViewById(R.id.tabUsername);
        String username = NetworkHandler.readTUsername(this);
        mUsernameView.setText(username);


        myBroadCastReceiver = new MyBroadCastReceiver();

        Intent intent= new Intent(this, UpdateService.class);

        Log.d("Debug Cenas", "oncreate: trying to bind service");
        UpdateService._activity = this;
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        Log.d("Debug Cenas", "oncreate: binded service");

        registerMyReceiver();
}

    @Override
    protected void onResume() {
        mayRequestPermission(READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL_STORAGE);
        mayRequestPermission(WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_EXTERNAL_STORAGE);

        super.onResume();

        //Authenticate in dropbox account
        if(!hasToken()) {
            Auth.startOAuth2Authentication(this, getString(R.string.dropbox_app_key));
        }
        else {
            //TODO change this as a way to show the info about the account already logged in dropbox
            //Toast.makeText(AlbumsActivity.this, "", Toast.LENGTH_SHORT).show();
        }
    }

    //TODO still understanding what to do here
    protected void loadData() {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Loading");
        dialog.show();

        new ListDropboxFolderTask(DropboxClientFactory.getClient(), new ListDropboxFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                dialog.dismiss();
                mAlbumsAdapter.clear();
                for (Metadata folder : result.getEntries()) {
                    mAlbumsAdapter.add(folder.getName());
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e("Error", "Failed to list folder.", e);
                Toast.makeText(AlbumsActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(""); //Send "" as the path because we want the base directory, that contains the albums folders


/* using smartphone storage
        File baseDirectory = new File(ALBUM_BASE_FOLDER);

        if(baseDirectory.exists()) {
            File[] files = baseDirectory.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    mAlbumsAdapter.add(file.getName());
                }
            }
        }
*/
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.albums, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_invitation_albums) {
            Intent albumsInvitations = new Intent(AlbumsActivity.this.getApplicationContext(), AlbumsInvitationsActivity.class);
            this.startActivity(albumsInvitations);

        } else if (id == R.id.nav_logout) {
            String username = getIntent().getStringExtra("username");
            mLogout = new UserLogoutTask(username, this);
            mLogout.execute((Void) null);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void stopService(){
        Log.d("Debug Cenas", "Stop: unbiding" );
        unbindService(this);
        stopService(new Intent(this, UpdateService.class));
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        Log.d("Debug Cenas", "onServiceConnected");
        UpdateService.MyBinder b = (UpdateService.MyBinder) binder;
        myService = b.getService();
    }
    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    //TODO probably get this class in a file, review its name and if it works to make all necessary activities use it...maybe a superclass?
    class MyBroadCastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(AlbumsActivity.this, "You have a new invite. Go to Invites Tab", Toast.LENGTH_LONG).show();

            ContextClass contextClass = (ContextClass) getApplicationContext();
            boolean logout = false;
            ArrayList<Integer> indextoRemove = new ArrayList<>();
            for(int i = 0; i < contextClass.getInvites().size(); i++){
                Invite inv = contextClass.getInvite(i);
                if(inv.get_albumName().equals(NEED_AUTHENTICATION) && inv.get_userAlbum().equals(NEED_AUTHENTICATION)){
                    logout = true;
                    indextoRemove.add(i);
                }
            }
            if(logout){
                for(int i : indextoRemove){
                    contextClass.removeInvite(i);
                }
                Toast.makeText(AlbumsActivity.this, "Not properly authenticated. Login again.", Toast.LENGTH_LONG).show();
                //Logout and start login
                stopService();
                Intent logoutData = new Intent(getApplicationContext(), LoginActivity.class);
                AlbumsActivity.this.startActivity(logoutData);
                AlbumsActivity.this.finish();
            }
        }
    }

    private void registerMyReceiver() {
        Log.d("Debug Cenas", "RegisterReceiver: registering MyReceiver");

        try{
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BROADCAST_ACTION);
            registerReceiver(myBroadCastReceiver, intentFilter);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(myBroadCastReceiver);
        unbindService(this);
        Log.d("Debug Cenas", "onDestroy: albums activity");
        super.onDestroy();
    }

    public UserLogoutTask getmLogout() {
        return mLogout;
    }

    public void setmLogout(UserLogoutTask mLogout) {
        this.mLogout = mLogout;
    }

    public CreateAlbumTask getmCreateAlb() {
        return mCreateAlb;
    }

    public void setmCreateAlb(CreateAlbumTask mCreateAlb) {
        this.mCreateAlb = mCreateAlb;
    }

    private boolean mayRequestPermission(final String permission, final int requestCode ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(permission)) {
            Snackbar.make(mUsernameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{permission}, requestCode);
                        }
                    });
        } else {
            requestPermissions(new String[]{permission}, requestCode);
        }
        return false;
    }

    //TODO different thread
    private void createAlbumInStorage(String albumName) {
        //Create directory if it not exist
        File albumFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/P2PHOTO", albumName);
        albumFolder.mkdirs();

        //Add catalog to directory
        File catalog = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/P2PHOTO/" + albumName, "PhotosCatalog.txt");
        try {
            boolean result = catalog.createNewFile();
            if (!result) {
                catalog.delete();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Something went wrong, when creating the catalog file", Toast.LENGTH_LONG).show();
            Log.d("MyDebug", "Exception " + e + ": " + e.getMessage());
        }
    }
}





class CustomAlbumsAdapter extends BaseAdapter {

    private final ArrayList<String> _albumTitle;

    private final Activity _activity;

    public CustomAlbumsAdapter(Activity act, ArrayList<String> albumTitle) {
        this._activity = act;
        _albumTitle = albumTitle;
    }

    public CustomAlbumsAdapter(Activity act) {
        this._activity = act;
        _albumTitle = new ArrayList<>();
    }

    public boolean containsAlbumName(String albumName) {
        return _albumTitle.contains(albumName);
    }

    @Override
    public int getCount() {
        return _albumTitle.size();
    }

    @Override
    public Object getItem(int position) {
        return _albumTitle.get(position);
    }

    public void clear() {
        _albumTitle.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<String> albumTitle) {
        _albumTitle.addAll(albumTitle);
        notifyDataSetChanged();
    }

    public void add(String albumTitle){
        _albumTitle.add(albumTitle);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        AlbumInfoViewHolder viewHolder;

        if( convertView == null) {
            view = _activity.getLayoutInflater().inflate(R.layout.albums_item, parent, false);
            viewHolder = new AlbumInfoViewHolder(view);
            view.setTag(viewHolder); //Store the view holder in the view
        } else {
            view = convertView;
            viewHolder = (AlbumInfoViewHolder) view.getTag();//In this case a view is being "aproveitada" and we can get the view holder from de view
        }

        String title = _albumTitle.get(position);
        viewHolder.albumTitle.setText(title);

        return view;
    }
}

class AlbumInfoViewHolder {

    final TextView albumTitle;

    public AlbumInfoViewHolder(View view) {
        albumTitle = view.findViewById(R.id.album_title);
    }
}




/*
 * Async task to list items in a folder
 */
class ListDropboxFolderTask extends AsyncTask<String, Void, ListFolderResult> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(ListFolderResult result);

        void onError(Exception e);
    }

    public ListDropboxFolderTask(DbxClientV2 dbxClient, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(ListFolderResult result) {
        super.onPostExecute(result);

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(result);
        }
    }

    @Override
    protected ListFolderResult doInBackground(String... params) {
        try {
            return mDbxClient.files().listFolder(params[0]);
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }
}





class DropboxCreateFolderTask extends AsyncTask<String, Void, FolderMetadata> {

    private final AlbumsActivity mActivity;
    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private Exception mException;
    private final ProgressDialog dialog;

    DropboxCreateFolderTask(AlbumsActivity act, Context context, DbxClientV2 dbxClient) {
        mActivity = act;
        mContext = context;
        mDbxClient = dbxClient;
        dialog = new ProgressDialog(mActivity);
    }

    @Override
    protected void onPreExecute() {
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading");
        dialog.show();
    }

    @Override
    protected void onPostExecute(FolderMetadata result) {
        super.onPostExecute(result);
        if (mException != null) { //onError with exception
            onError(mException);
        } else if (result == null) { //onError no exception
            onError(null);
        } else { //onUploadComplete;
            dialog.dismiss();

            String message = result.getName() + " created in the cloud";
            Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();

            // Reload the folder
            mActivity.loadData();
        }
    }

    private void onError(Exception e) {
        dialog.dismiss();

        Log.e("Error", "Failed to upload file.", e);
        Toast.makeText(mActivity, "An error has occurred", Toast.LENGTH_SHORT).show();
    }

    //Already have checked if name os album exist
    @Override
    protected FolderMetadata doInBackground(String... params) {
        try {
            String remoteFolder = "/" + params[0];
            CreateFolderResult folderResult = mDbxClient.files().createFolderV2(remoteFolder);
            //InputStream inputStream = new FileInputStream("");
            InputStream inputStream = new ByteArrayInputStream("".getBytes());
            mDbxClient.files().uploadBuilder(remoteFolder + "/" + "PhotosCatalog.txt")
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream);
            return folderResult.getMetadata();
        } catch (DbxException | IOException e) {
            mException = e;
        }
        return null;
    }
}




