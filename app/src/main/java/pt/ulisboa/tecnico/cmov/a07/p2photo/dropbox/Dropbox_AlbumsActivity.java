package pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import pt.ulisboa.tecnico.cmov.a07.p2photo.AlbumsActivity;
import pt.ulisboa.tecnico.cmov.a07.p2photo.CreateAlbumTask;
import pt.ulisboa.tecnico.cmov.a07.p2photo.SessionHandler;
import pt.ulisboa.tecnico.cmov.a07.p2photo.R;

public class Dropbox_AlbumsActivity extends AlbumsActivity {

    private DropboxAuthenticationHandler mDropboxAuthenticationHandler;
    private DropboxCreateFolderTask mDropboxCreateAlbumTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOnItemClickListenerForAppMode(Dropbox_InsideAlbumActivity.class);

        //Instantiate dropbox authenticator
        mDropboxAuthenticationHandler = new DropboxAuthenticationHandler(this);

        // Creation of floating action button for creating an album, subclass calls this
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_albums);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Dropbox_AlbumsActivity.this);
                final EditText input = new EditText(Dropbox_AlbumsActivity.this);
                input.setHint("Album's name"); //TODO security issue, se escreverem um nome tipo ../cenas podem explorar diretorias que nao deveriam, pelo menos na storage no smartphone
                dialogBuilder.setTitle("Create New Album")
                        .setView(input)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String albumName = input.getText().toString();

                                if(TextUtils.isEmpty(albumName)) {
                                    Toast.makeText(Dropbox_AlbumsActivity.this, getString(R.string.error_albumName_required), Toast.LENGTH_LONG).show();
                                }
                                else if (mAlbumsAdapter.containsAlbumName(albumName)) {
                                    Toast.makeText(Dropbox_AlbumsActivity.this, "That Album already exists", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    mDropboxCreateAlbumTask = new DropboxCreateFolderTask(Dropbox_AlbumsActivity.this, DropboxClientFactory.getClient(), new DropboxCreateFolderTask.Callback() {
                                        @Override
                                        public void onFolderCreated(String catalogUrl) {
                                            mCreateAlb = new CreateAlbumTask(albumName, catalogUrl, Dropbox_AlbumsActivity.this);
                                            mCreateAlb.execute((Void) null);
                                        }
                                    });
                                    mDropboxCreateAlbumTask.execute(albumName, SessionHandler.readTUsername(Dropbox_AlbumsActivity.this));
                                    //mDropboxCreateAlbumTask.execute(albumName, SessionHandler.readTUsername(AlbumsActivity.this));
                                    //createAlbumInStorage(albumName);
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        mDropboxAuthenticationHandler.authenticationVerification();
    }

    //Load stuff to show on screen
    public void loadData() {

        new DropboxListFolderTask(DropboxClientFactory.getClient(), new DropboxListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                mAlbumsAdapter.clear();
                for (Metadata folder : result.getEntries()) {
                    mAlbumsAdapter.add(folder.getName());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("Error", "Failed to list folder.", e);
                Toast.makeText(Dropbox_AlbumsActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(""); //Send "" as the path because we want the base directory, that contains the albums folders
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}