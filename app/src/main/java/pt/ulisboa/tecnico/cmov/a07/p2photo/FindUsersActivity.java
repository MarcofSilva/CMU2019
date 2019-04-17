package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class FindUsersActivity extends AppCompatActivity {

    private List<String> users = new ArrayList<>();
    private List<String> usersToShowList = new ArrayList<>();
    private ArrayAdapter<String> usersShowAdapter;
    private GetUsersTask mGetTask = null;

    private String DUMMY_LIST = "Marco123;Joao1;Matilde2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);

        usersShowAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usersToShowList);

        ListView listView = findViewById(R.id.lvShowUsers);
        listView.setAdapter(usersShowAdapter);

        EditText text = findViewById(R.id.findUser_search);
        text.addTextChangedListener(textWatcher);

        mGetTask = new GetUsersTask();
        mGetTask.execute((Void) null);

        usersShowAdapter.addAll(users);
    }

    private TextWatcher textWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
            EditText editUpdated = (EditText) s ;
            String nameUpdated = editUpdated.getText().toString();
            List<String> addUsers = new ArrayList<>();

            usersShowAdapter.clear();
            for(String u : users){
                if(u.startsWith(nameUpdated)){
                    //usersShowAdapter.add(u);
                    addUsers.add(u);
                }
            }
            usersShowAdapter.addAll(addUsers);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    };

    public class GetUsersTask extends AsyncTask<Void, Void, String> {

        public GetUsersTask() {
            super();
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = "";
            try {
                /*URL url = new URL("http://sigma03.ist.utl.pt:8350/getUsers");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");*/


                //TODO see what each of this properties do
                //conn.setRequestProperty("accept", "*/*");
                /*conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = Network.convertStreamToString(in);

                if(response == null){
                    //TODO error
                }*/
                response = DUMMY_LIST;
                return response;

            } catch (Exception e) {
                Log.e("MYDEBUG", "Exception: " + e.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(final String usersStr) {
            for(String s : usersStr.split(";")){
                users.add(s);
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
