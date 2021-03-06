package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class FindUsersActivity extends AppCompatActivity {

    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";
    private static final String USERNAMES_EXTRA = "usernames";

    private static final int FIND_USERS_REQUEST_CODE = 2;


    private ArrayList<String> permittedUsers = new ArrayList<>();
    private ArrayList<String> allUsers = new ArrayList<>();
    private ArrayList<String> usersToAdd = new ArrayList<>();
    private ArrayList<String> usersToShowList = new ArrayList<>();
    private CustomAdapterUsers usersCustomAdapter;
    String albumName = "";
    private GetUsersTask mGetTask = null;

    private EditText searchUserView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);

        searchUserView = findViewById(R.id.findUser_search);

        usersCustomAdapter = new CustomAdapterUsers(usersToShowList, this );

        ListView listView = findViewById(R.id.lvShowUsers);

        listView.setAdapter(usersCustomAdapter);

        EditText text = findViewById(R.id.findUser_search);
        text.addTextChangedListener(textWatcher);

        TextView doneLink = findViewById(R.id.done_btn);
        doneLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent usernamesIntent = new Intent();
                usernamesIntent.putStringArrayListExtra(USERNAMES_EXTRA, usersToAdd);
                setResult(RESULT_OK, usernamesIntent);
                finish();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout ll = (LinearLayout) view;
                TextView tv= (TextView)ll.getChildAt(0);
                CheckBox cb= (CheckBox)ll.getChildAt(1);

                boolean checked = cb.isChecked();
                boolean enabled = cb.isEnabled();
                if(enabled) {
                    if (!checked) {
                        cb.setChecked(true);
                        usersToAdd.add(tv.getText().toString());
                    } else {
                        cb.setChecked(false);
                        String name = (tv.getText().toString());

                        for (String u : usersToAdd) {
                            if (u.equals(name)) {
                                usersToAdd.remove(u);
                                break;
                            }
                        }
                    }
                }
                else{
                    if (!checked){
                        cb.setChecked(true);
                    }
                }
            }
        });
        albumName = getIntent().getStringExtra("albumName");
        mGetTask = new GetUsersTask(this, albumName);
        mGetTask.execute((Void) null);
    }

    private TextWatcher textWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
            String nameUpdated = searchUserView.getText().toString();
            ArrayList<String> addUsers = new ArrayList<>();

            usersCustomAdapter.clear();
            for(String user : allUsers){
                //Standardize strings for ignoring case
                String lowUser = user.toLowerCase();
                String lowUpdated = nameUpdated.toLowerCase();

                if(lowUser.startsWith(lowUpdated)){
                    //usersShowAdapter.add(u);
                    addUsers.add(user);
                }
            }
            usersCustomAdapter.addAllUsers(addUsers);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    };

    public void addUsersPermitted(String s){
        permittedUsers.add(s);
    }
    public void addUser(String s){
        allUsers.add(s);
    }

    public void addAllusersCustom(){
        usersCustomAdapter.addAllUsers(allUsers);
        usersCustomAdapter.addAllPermitted(permittedUsers);
    }
}