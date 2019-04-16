package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Button;

public class NewAlbumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_album);

        ImageButton mFinishView = findViewById(R.id.buttonFinish);
        mFinishView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlbum();
            }
        });

        Button mAddUserView = findViewById(R.id.buttonAddUser);
        mAddUserView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

    }

    private void createAlbum(){

    }
}
