package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.android.Auth;

public class AlbumsActivity extends DropboxActivity implements NavigationView.OnNavigationItemSelectedListener, ServiceConnection {

    //Intent extra tag
    private static final String USERNAME_EXTRA = "username";

    //TODO cuidado
    private static final String DUMMYURL = "www.pornhub.com";

    public static final String BROADCAST_ACTION = "pt.ulisboa.tecnico.updating";

    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";

    private UserLogoutTask mLogout = null;
    private CreateAlbumTask mCreateAlb = null;

    private AcceptAlbumTask mAcceptALb = null;
    String userName = "";
    String albumName = "";
    String dropboxURL = "";

    private UpdateService myService;
    private MyBroadCastReceiver myBroadCastReceiver;

    // UI references.
    private TextView mUsernameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_albums);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AlbumsActivity.this);
                final EditText input = new EditText(AlbumsActivity.this);
                input.setHint("Album's name");
                dialogBuilder.setTitle("Create New Album")
                        .setView(input)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String albumName = input.getText().toString();

                                if(TextUtils.isEmpty(albumName)) {
                                    Toast.makeText(AlbumsActivity.this, getString(R.string.error_albumName_required), Toast.LENGTH_LONG).show();
                                    //TODO check if this work as a way to not let de box disappear
                                    //dialogBuilder.create().show();
                                }
                                else {
                                    String url = DUMMYURL;
                                    mCreateAlb = new CreateAlbumTask(albumName, url, AlbumsActivity.this);
                                    mCreateAlb.execute((Void) null);
                                }
                                //TODO apagar
                                Intent newAlbumIntent = new Intent(getApplicationContext(), InsideAlbumActivity.class);
                                newAlbumIntent.putExtra("myName",albumName);
                                startActivity(newAlbumIntent);
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
        String username = getIntent().getStringExtra(USERNAME_EXTRA);
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
        //TODO
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

        } else if (id == R.id.nav_slideshow) {

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
            try {
                String newAlbumsIAmIn = intent.getStringExtra("data");
                Log.d("Debug Cenas","receiver: " +  newAlbumsIAmIn);

                if(newAlbumsIAmIn.equals(NEED_AUTHENTICATION)){
                    Toast.makeText(AlbumsActivity.this, "Not properly authenticated. Login again.", Toast.LENGTH_LONG).show();
                    //Logout and start login
                    Intent logoutData = new Intent(AlbumsActivity.this.getApplicationContext(), LoginActivity.class);
                    AlbumsActivity.this.startActivity(logoutData);
                    AlbumsActivity.this.finish();
                }
                String[] responseSplit = newAlbumsIAmIn.split(";");
                userName = responseSplit[0];
                albumName = responseSplit[1];
                
                //TODO eventually to delete
                dropboxURL = "dummyShit";
                Log.d("Debug Cenas","receiver: user: " + userName + " album " + albumName + " dropbox " + dropboxURL);

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AlbumsActivity.this);
                dialogBuilder.setTitle(userName + " wants you to join album " + albumName);
                dialogBuilder
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("Debug Cenas", "Accepted invite");

                                mAcceptALb = new AcceptAlbumTask(userName, albumName, dropboxURL, "true", AlbumsActivity.this);
                                mAcceptALb.execute((Void) null);
                            }
                        })
                        .setNegativeButton("Refuse", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("Debug Cenas", "Rejected invite");

                                mAcceptALb = new AcceptAlbumTask(userName, albumName, dropboxURL, "false", AlbumsActivity.this);
                                mAcceptALb.execute((Void) null);

                            }
                        });
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();

            } catch (Exception e) {
                e.printStackTrace();
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

    public AcceptAlbumTask getmAcceptALb() {
        return mAcceptALb;
    }

    public void setmAcceptALb(AcceptAlbumTask mAcceptALb) {
        this.mAcceptALb = mAcceptALb;
    }
}
