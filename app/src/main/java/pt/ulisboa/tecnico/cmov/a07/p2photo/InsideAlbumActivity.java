package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class InsideAlbumActivity extends AppCompatActivity {

    private static final String USERNAMES_EXTRA = "usernames";
    private String myName;

    private static final int PICKPHOTO_REQUEST_CODE = 10;
    private static final int FIND_USERS_REQUEST_CODE = 2;

    private AddUsersToAlbumTask mAddUsersToAlbum = null;

    private TextView mAlbumTitleView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inside_album);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myName = getIntent().getStringExtra("myName");
        mAlbumTitleView = findViewById(R.id.inside_AlbumTitle);
        mAlbumTitleView.setText(myName);

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
                                //TODO the photos selected should be added to the album
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICKPHOTO_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            //TODO
        }
        else if(requestCode == FIND_USERS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> usernames = data.getStringArrayListExtra(USERNAMES_EXTRA);
            mAddUsersToAlbum = new AddUsersToAlbumTask(usernames, myName ,this);
            mAddUsersToAlbum.execute((Void) null);
        }
    }

    private void launchPhotoChooser() {
        // Launch intent to pick photos for upload
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, PICKPHOTO_REQUEST_CODE);
    }

    public AddUsersToAlbumTask getmAddUsersToAlbum() {
        return mAddUsersToAlbum;
    }

    public void setmAddUsersToAlbum(AddUsersToAlbumTask AddUsersToAlbum) {
        this.mAddUsersToAlbum = AddUsersToAlbum;
    }
}
