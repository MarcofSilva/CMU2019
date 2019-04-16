package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class NewAlbumActivity extends AppCompatActivity {

    List<String> _users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_album);

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, _users);

        ListView listView = (ListView) findViewById(R.id.listViewNAUsers);
        listView.setAdapter(itemsAdapter);

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
        EditText mUsernameView = findViewById(R.id.textAlbumName);
        Album newAlbum = new Album(mUsernameView.getText().toString(),_users);

    }
}
