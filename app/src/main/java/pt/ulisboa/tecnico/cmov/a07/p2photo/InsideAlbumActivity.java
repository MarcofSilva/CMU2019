package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class InsideAlbumActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inside_album);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab_inside_album);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(InsideAlbumActivity.this);
                final EditText input = new EditText(InsideAlbumActivity.this);
                input.setHint("Album's name");
                dialogBuilder
                        .setPositiveButton("Add Photos", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(InsideAlbumActivity.this,"Picker for photos needs to be implemented", Toast.LENGTH_LONG).show();

                                //TODO
                            }
                        })
                        .setNeutralButton("Add Users", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(getApplicationContext(), FindUsersActivity.class));
                            }
                        })
                        ;
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        });
    }
}
