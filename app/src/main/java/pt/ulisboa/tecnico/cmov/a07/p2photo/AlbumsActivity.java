package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AlbumsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Intent extra tag
    private static final String USERNAME_EXTRA = "username";

    //server response types to login attempt
    private static final String SUCCESS = "Success";
    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";

    //Storage filename
    private static final String TOKEN_FILENAME = "AuthToken";
    private static final String DUMMYURL = "www.pornhub.com";

    private UserLogoutTask mLogout = null;
    private CreateAlbumTask mCreateAlb = null;

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
                                    Toast.makeText(AlbumsActivity.this, getString(R.string.error_albumName_required), Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    String url = DUMMYURL;
                                    mCreateAlb = new CreateAlbumTask(albumName, url);
                                    mCreateAlb.execute((Void) null);
                                }
                                //startActivity(new Intent(getApplicationContext(), InsideAlbumActivity.class));
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
            mLogout = new UserLogoutTask(username);
            mLogout.execute((Void) null);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;

        UserLogoutTask(String username) {
            mUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL("http://sigma03.ist.utl.pt:8350/logout");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("username", mUsername);

                //TODO see what each of this properties do
                //conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("Content-Type", "application/json");
                //conn.setRequestProperty("Accept", "application/json");
                //conn.setRequestProperty("connection", "Keep-Alive");
                //conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
                conn.setDoOutput(true);

                conn.setRequestProperty("Authorization", NetworkHandler.readToken(AlbumsActivity.this));

                OutputStream os = conn.getOutputStream();
                os.write(postDataParams.toString().getBytes());
                os.close();

                InputStream in = new BufferedInputStream(conn.getInputStream());
                String response = NetworkHandler.convertStreamToString(in);

                if(response != null && response.equals(SUCCESS)){
                    NetworkHandler.writeTokenFile("", AlbumsActivity.this);
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
            mLogout= null;

            if(success){
                Toast.makeText(AlbumsActivity.this, "Logout successful", Toast.LENGTH_LONG);
                Intent logoutData = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(logoutData);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mLogout = null;
        }

    }


    public class CreateAlbumTask extends AsyncTask<Void, Void, String> {

        private final String _albumName;
        private final String _url;

        CreateAlbumTask(String albumName, String url) {
            _albumName = albumName;
            _url = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = null;
            try {
                URL url = new URL("http://sigma03.ist.utl.pt:8350/createAlbum");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("albumName", _albumName);
                postDataParams.put("url", _url);


                //TODO see what each of this properties do
                //conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("Content-Type", "application/json");
                //conn.setRequestProperty("Accept", "application/json");
                //conn.setRequestProperty("connection", "Keep-Alive");
                //conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
                conn.setDoOutput(true);

                conn.setRequestProperty("Authorization", NetworkHandler.readToken(AlbumsActivity.this));

                OutputStream os = conn.getOutputStream();
                os.write(postDataParams.toString().getBytes());
                os.close();

                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = NetworkHandler.convertStreamToString(in);

            } catch (Exception e) {
                Log.e("MyDebug", "Exception: " + e.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(final String response) {
            mCreateAlb = null;

            if(response != null && response.equals(SUCCESS)){
                Toast.makeText(AlbumsActivity.this, "You created an album! Gl finding it", Toast.LENGTH_LONG).show();
            }
            else if(response.equals(NEED_AUTHENTICATION)) {
                Toast.makeText(AlbumsActivity.this, "Not properly authenticated. Login again.", Toast.LENGTH_LONG).show();
                //Logout and start login
                Intent logoutData = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(logoutData);
                finish();
            }
            else {
                Toast.makeText(AlbumsActivity.this, "Error creating albums", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mCreateAlb = null;
        }

    }
}
