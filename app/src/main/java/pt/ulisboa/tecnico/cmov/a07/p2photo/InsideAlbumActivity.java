package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Add Photos", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //TODO start activity for adding photos
                            }
                        })
                        .setAction("Add Users", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent addUsersIntent = new Intent(getApplicationContext(), InsideAlbumActivity.class);
                                startActivity(addUsersIntent);
                            }
                        })
                        .show();
            }
        });
    }
}
