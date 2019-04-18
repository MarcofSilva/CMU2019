package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class FindUsersActivity extends AppCompatActivity {

    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";


    private ArrayList<String> allUsers = new ArrayList<>();
    private ArrayList<String> users = new ArrayList<>();
    private ArrayList<String> usersToShowList = new ArrayList<>();
    private CostumAdapterUsers usersCostumAdapter;
    private GetUsersTask mGetTask = null;

    private EditText searchUserView;

    private String DUMMY_LIST = "Marco123;Joao1;Matilde2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);

        searchUserView = findViewById(R.id.findUser_search);

        usersCostumAdapter = new CostumAdapterUsers(usersToShowList, this );

        ListView listView = findViewById(R.id.lvShowUsers);

        listView.setAdapter(usersCostumAdapter);

        EditText text = findViewById(R.id.findUser_search);
        text.addTextChangedListener(textWatcher);

        TextView doneAlbum = findViewById(R.id.done_btn);
        doneAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //done button behaviour
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout ll = (LinearLayout) view;
                TextView tv= (TextView)ll.getChildAt(0);
                CheckBox cb= (CheckBox)ll.getChildAt(1);

                boolean checked = cb.isChecked();
                if(!checked){
                    cb.setChecked(true);
                    users.add(tv.getText().toString());
                }
                else {
                    cb.setChecked(false);
                    String name = (tv.getText().toString());

                    for(String u : users){
                        if(u.equals(name)){
                            users.remove(u);
                            break;
                        }
                    }
                }
            }
        });
        mGetTask = new GetUsersTask();
        mGetTask.execute((Void) null);
    }

    private TextWatcher textWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
            String nameUpdated = searchUserView.getText().toString();
            ArrayList<String> addUsers = new ArrayList<>();

            usersCostumAdapter.clear();
            for(String user : allUsers){
                //Standardize strings for ignoring case
                String lowUser = user.toLowerCase();
                String lowUpdated = nameUpdated.toLowerCase();

                if(lowUser.startsWith(lowUpdated)){
                    //usersShowAdapter.add(u);
                    addUsers.add(user);
                }
            }
            usersCostumAdapter.addAll(addUsers);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    };

    public class GetUsersTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String response = "";
            try {
                URL url = new URL("http://sigma03.ist.utl.pt:8350/getUsers");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");


                //TODO see what each of this properties do
                //conn.setRequestProperty("accept", "*/*");
                //conn.setRequestProperty("Content-Type", "application/json");
                //conn.setRequestProperty("Accept", "application/");
                //conn.setRequestProperty("connection", "Keep-Alive");
                //conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
                conn.setDoOutput(false);

                conn.setRequestProperty("Authorization", NetworkHandler.readToken(FindUsersActivity.this));

                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = NetworkHandler.convertStreamToString(in);

                //TODO dummy
                //return DUMMY_LIST

            } catch (Exception e) {
                Log.e("MYDEBUG", "Exception: " + e.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(final String usersStr) {
            if(usersStr == null){
                Toast.makeText(FindUsersActivity.this, "Error: Getting users from server", Toast.LENGTH_SHORT).show();
            }
            else if(usersStr.equals(NEED_AUTHENTICATION)) {
                Toast.makeText(FindUsersActivity.this, "Not properly authenticated. Login again.", Toast.LENGTH_LONG).show();
                //Logout and start login
                Intent logoutData = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(logoutData);
                finish();
            }
            else {
                for (String s : usersStr.split(";")) {
                    allUsers.add(s);
                }
                usersCostumAdapter.addAll(allUsers);
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
