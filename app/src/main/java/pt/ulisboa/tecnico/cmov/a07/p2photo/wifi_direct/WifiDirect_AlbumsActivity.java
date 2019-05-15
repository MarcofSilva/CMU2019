package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import pt.ulisboa.tecnico.cmov.a07.p2photo.AlbumsActivity;
import pt.ulisboa.tecnico.cmov.a07.p2photo.InsideAlbumActivity;
import pt.ulisboa.tecnico.cmov.a07.p2photo.R;

public class WifiDirect_AlbumsActivity extends AlbumsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOnItemClickListenerForAppMode(WifiDirect_InsideAlbumActivity.class);

        // Creation of floating action button for creating an album, subclass calls this
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_albums);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(WifiDirect_AlbumsActivity.this);
                final EditText input = new EditText(WifiDirect_AlbumsActivity.this);
                input.setHint("Album's name"); //TODO security issue, se escreverem um nome tipo ../cenas podem explorar diretorias que nao deveriam, pelo menos na storage no smartphone
                dialogBuilder.setTitle("Create New Album")
                        .setView(input)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String albumName = input.getText().toString();

                                if(TextUtils.isEmpty(albumName)) {
                                    Toast.makeText(WifiDirect_AlbumsActivity.this, getString(R.string.error_albumName_required), Toast.LENGTH_LONG).show();
                                }
                                else if (mAlbumsAdapter.containsAlbumName(albumName)) {
                                    Toast.makeText(WifiDirect_AlbumsActivity.this, "That Album already exists", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    //TODO create album in storage for wifi direct
                                    /*mDropboxCreateAlbumTask = new DropboxCreateFolderTask(WifiDirect_AlbumsActivity.this, DropboxClientFactory.getClient(), new DropboxCreateFolderTask.Callback() {
                                        @Override
                                        public void onFolderCreated(String catalogUrl) {
                                            mCreateAlb = new CreateAlbumTask(albumName, catalogUrl, WifiDirect_AlbumsActivity.this);
                                            mCreateAlb.execute((Void) null);
                                        }
                                    });
                                    mDropboxCreateAlbumTask.execute(albumName, SessionHandler.readTUsername(WifiDirect_AlbumsActivity.this));*/
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

    public void loadData() {
        //TODO
        /* using smartphone storage
        File baseDirectory = new File(ALBUM_BASE_FOLDER);

        if(baseDirectory.exists()) {
            File[] files = baseDirectory.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    mAlbumsAdapter.add(file.getName());
                }
            }
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
