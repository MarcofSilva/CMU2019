package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.DropboxAuthenticationHandler;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public abstract class AlbumsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ServiceConnection {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public static final String BROADCAST_ACTION = "pt.ulisboa.tecnico.updating";
    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";

    protected CreateAlbumTask mCreateAlb = null;
    private UserLogoutTask mLogout = null;
    private UpdateService myService;
    private MyBroadCastReceiver myBroadCastReceiver;

    protected CustomAlbumsAdapter mAlbumsAdapter;

    // UI references.
    private TextView mUsernameView;
    private GridView mAlbumsGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAlbumsAdapter = new CustomAlbumsAdapter(this);

        mAlbumsGridView = findViewById(R.id.photoGrid_Albums);
        mAlbumsGridView.setAdapter(mAlbumsAdapter);

        // The setOnItemClickListener defined in dropbox and wifidirect subclasses...they call a method in this class as a way of start the right insideAlbumActivity (Dropbox or wifiDirect)

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
        String username = SessionHandler.readTUsername(this);
        mUsernameView.setText(username);

        myBroadCastReceiver = new MyBroadCastReceiver(this);

        Intent intent= new Intent(this, UpdateService.class);

        Log.d("Debug Cenas", "oncreate: trying to bind service");
        UpdateService._activity = this;
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        Log.d("Debug Cenas", "oncreate: binded service");

        registerMyReceiver();
    }

    protected void setOnItemClickListenerForAppMode(final Class<?> insideAlbumActivity) {
        //Clicked one album
        if(mAlbumsGridView != null) {
            mAlbumsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent newAlbumIntent;

                    newAlbumIntent = new Intent(getApplicationContext(), insideAlbumActivity);
                    AlbumInfoViewHolder mAlbumInfoViewHolder = (AlbumInfoViewHolder) view.getTag();
                    String clickedAlbum = mAlbumInfoViewHolder.albumTitle.getText().toString();
                    String creator = mAlbumsAdapter.getCreator(position);
                    newAlbumIntent.putExtra("myName", clickedAlbum);
                    newAlbumIntent.putExtra("albumCreator", creator);

                    startActivity(newAlbumIntent);
                }
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        mayRequestPermission(READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL_STORAGE);
        mayRequestPermission(WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_EXTERNAL_STORAGE);
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
            Intent albumsInvitations = new Intent(getApplicationContext(), AlbumsInvitationsActivity.class);
            startActivity(albumsInvitations);

        } else if (id == R.id.nav_logout) {
            String username = getIntent().getStringExtra("username");
            mLogout = new UserLogoutTask(username, this);
            mLogout.execute((Void) null);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Make both appModes implement this
    public abstract void loadData();

    //Service for invitations

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

        private AlbumsActivity _activity;

        public MyBroadCastReceiver(AlbumsActivity act) {
            _activity = act;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ContextClass contextClass = (ContextClass) getApplicationContext();
            boolean logout = false;
            ArrayList<Integer> indextoRemove = new ArrayList<>();
            for(int i = 0; i < contextClass.getInvites().size(); i++){
                Invite inv = contextClass.getInvite(i);
                if(inv.get_albumName().equals(NEED_AUTHENTICATION) && inv.get_userAlbum().equals(NEED_AUTHENTICATION)){
                    logout = true;
                    indextoRemove.add(i);
                    Toast.makeText(getApplicationContext(), "Not properly authenticated. Login again", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "You have a new invite. Go to Invites Tab", Toast.LENGTH_LONG).show();
                }
            }
            if(logout){
                for(int i : indextoRemove){
                    contextClass.removeInvite(i);
                }
                Toast.makeText(getApplicationContext(), "Not properly authenticated. Login again.", Toast.LENGTH_LONG).show();


                //------Clean session tokens before logging out----------
                //App account session
                SessionHandler.cleanSessionCredentials(_activity);

                // Check if appMode is the dropbox one and if so remove the token
                String appModeDropbox = getString(R.string.AppModeDropBox);
                if(contextClass.getAppMode().equals(appModeDropbox)) {
                    //Dropbox specific code(removing dropbox token from storage)
                    DropboxAuthenticationHandler.cleanDropboxCredentials(_activity);
                }

                //Logout and start login
                stopService();
                Intent logoutData = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(logoutData);
                finish();
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
        super.onDestroy();
        unregisterReceiver(myBroadCastReceiver);
        Log.d("Debug Cenas", "onDestroy: albums activity");
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

    protected class CustomAlbumsAdapter extends BaseAdapter {

        private final ArrayList<String> _albumTitle;
        private final ArrayList<String> _albumCreator;

        private final Activity _activity;

        public CustomAlbumsAdapter(Activity act, ArrayList<String> albumInfo) {
            this._activity = act;
            _albumTitle = new ArrayList<>();
            _albumCreator = new ArrayList<>();
            for (String info : albumInfo) {
                String[] albumInfos = info.split(":");
                _albumTitle.add(albumInfos[0]);
                _albumCreator.add(albumInfos[1]);
            }
        }

        public CustomAlbumsAdapter(Activity act) {
            this._activity = act;
            _albumTitle = new ArrayList<>();
            _albumCreator = new ArrayList<>();
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

        public String getCreator(int position) {
            return _albumCreator.get(position);
        }

        public void clear() {
            _albumTitle.clear();
            _albumCreator.clear();
            notifyDataSetChanged();
        }

        public void addAll(ArrayList<String> albumInfo) {
            for (String info : albumInfo) {
                String[] albumInfos = info.split(":");
                _albumTitle.add(albumInfos[0]);
                _albumCreator.add(albumInfos[1]);
            }
            notifyDataSetChanged();
        }

        //argument: albumInfo = album name and album creator
        public void add(String albumInfo){
            String[] albumInfos = albumInfo.split(":");
            _albumTitle.add(albumInfos[0]);
            _albumCreator.add(albumInfos[1]);
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

    protected class AlbumInfoViewHolder {

        final TextView albumTitle;

        public AlbumInfoViewHolder(View view) {
            albumTitle = view.findViewById(R.id.album_title);
        }
    }
}


class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {

    private static final String DROPBOX_CREDENTIALS_STORAGE = "dropbox_credentials";
    private static final String DROPBOX_ACCESS_TOKEN = "dropbox_access_token";
    private static final String DROPBOX_USER_ID = "dropbox_user_id";

    //server response types to login attempt
    private static final String SUCCESS = "Success";
    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";

    private final String mUsername;
    private AlbumsActivity _activity;

    UserLogoutTask(String username, AlbumsActivity act) {
        mUsername = username;
        _activity = act;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            URL url = new URL(_activity.getString(R.string.serverAddress) + "/logout");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("username", mUsername);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.setRequestProperty("Authorization", SessionHandler.readToken(_activity));

            OutputStream os = conn.getOutputStream();
            os.write(postDataParams.toString().getBytes());
            os.close();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String response = SessionHandler.convertStreamToString(in);

            if(response != null && response.equals(SUCCESS) || response.equals(NEED_AUTHENTICATION)){
                SessionHandler.writeTokenAndUsername("", "", _activity);
                return true;
            }
            return false;

        } catch (Exception e) {
            Log.e("MyDebug", "Exception: " + e.getMessage());
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        _activity.setmLogout(null);

        if(success){
            Log.d("Debug Cenas", "Should stop my service" );
            _activity.stopService();
            Toast.makeText(_activity, "Logout successful", Toast.LENGTH_LONG);


            //------Clean session tokens before logging out----------
            //App account session
            SessionHandler.cleanSessionCredentials(_activity);

            // Check if appMode is the dropbox one and if so remove the token
            ContextClass contextClass = (ContextClass) _activity.getApplicationContext();
            String appModeDropbox = _activity.getString(R.string.AppModeDropBox);
            if(contextClass.getAppMode().equals(appModeDropbox)) {
                //Dropbox specific code(removing dropbox token from storage)
                DropboxAuthenticationHandler.cleanDropboxCredentials(_activity);
            }


            Intent logoutData = new Intent(_activity.getApplicationContext(), LoginActivity.class);
            _activity.startActivity(logoutData);
            _activity.finish();
        }
    }

    @Override
    protected void onCancelled() {
        _activity.setmLogout(null);
    }

}





