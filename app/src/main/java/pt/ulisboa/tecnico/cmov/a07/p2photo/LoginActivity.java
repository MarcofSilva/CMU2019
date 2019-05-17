package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.Dropbox_AlbumsActivity;
import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.Security.KeyManager;
import pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.WifiDirect_AlbumsActivity;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    //request code for activity for result, when starting RegisterActivity
    private static final int REQUEST_REGISTER_CODE = 1;
    private static final String USERNAME_EXTRA = "username";
    private static final String PASSWORD_EXTRA = "password";


    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mSignUpLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = findViewById(R.id.login_username);
        populateAutoComplete();

        mPasswordView = findViewById(R.id.login_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSignInButton = findViewById(R.id.username_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mSignUpLink = findViewById(R.id.link_sign_up);
        mSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpForResultIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(signUpForResultIntent, REQUEST_REGISTER_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_REGISTER_CODE && resultCode == RESULT_OK && data != null) {
            mUsernameView.setText(data.getStringExtra(USERNAME_EXTRA));
            mPasswordView.setText(data.getStringExtra(PASSWORD_EXTRA));
            attemptLogin();
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mUsernameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        ContextClass context = (ContextClass) getApplicationContext();
        String appMode = context.getAppMode();
        if(appMode.equals(getApplicationContext().getString(R.string.AppModeDropBox))){
            KeyPair kp = KeyManager.readKeyPair(username,this);
            if(kp == null){
                Toast.makeText(this, "You already registered on other phone. No keys here", Toast.LENGTH_LONG);
                mUsernameView.setError("No keys for this user");
                focusView = mUsernameView;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            context = (ContextClass) getApplicationContext();
            appMode = context.getAppMode();
            mAuthTask = new UserLoginTask(username, password, appMode,this);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username) {
        return username.matches("[^a-zA-Z]*[a-zA-Z].*");
    }

    private boolean isPasswordValid(String password) {
        return password.matches("[^\\d]*\\d.*") &&
                password.matches("[^A-Z]*[A-Z].*") &&
                password.matches("[^a-z]*[a-z].*") &&
                password.matches("[^.-_]*[.-_].*");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Nickname
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> usernames = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            usernames.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addUsernamesToAutoComplete(usernames);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addUsernamesToAutoComplete(List<String> usernameAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, usernameAddressCollection);

        mUsernameView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public UserLoginTask getmAuthTask() {
        return mAuthTask;
    }

    public void setmAuthTask(UserLoginTask mAuthTask) {
        this.mAuthTask = mAuthTask;
    }
    public EditText getmPasswordView() {
        return mPasswordView;
    }

    public void setmPasswordView(EditText mPasswordView) {
        this.mPasswordView = mPasswordView;
    }
    public AutoCompleteTextView getmUsernameView() {
        return mUsernameView;
    }

    public void setmUsernameView(AutoCompleteTextView mUsernameView) {
        this.mUsernameView = mUsernameView;
    }
}


/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */

class UserLoginTask extends AsyncTask<Void, Void, String> {
    //server response types to login attempt
    private static final String LOGIN_SUCCESS = "Success";
    private static final String LOGIN_UNKNOWN_USER = "UnknownUser";
    private static final String LOGIN_INCORRECT_PASSWORD = "IncorrectPassword";
    private static final String LOGIN_ERROR = "Error";


    private final String mUsername;
    private final String mPassword;
    private LoginActivity _activity;
    private String _appMode;

    UserLoginTask(String username, String password, String appMode, LoginActivity act) {
        mUsername = username;
        mPassword = password;
        _activity = act;
        _appMode = appMode;
    }

    @Override
    protected String doInBackground(Void... params) {
        String response = null;
        try {
            URL url = new URL(_activity.getString(R.string.serverAddress) + "/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("username", mUsername);
            postDataParams.put("password", mPassword);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(postDataParams.toString().getBytes());
            os.close();

            // read the response
            String[] responseSplit;
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = SessionHandler.convertStreamToString(in);
            responseSplit = response.split(" ");

            String token = responseSplit[1];
            SessionHandler.writeTokenAndUsername(token, mUsername,_activity);

            if(responseSplit.length > 2){
                String[] nameKey = responseSplit[2].split(",");
                for(String s : nameKey){
                    String[] namekeySplit = s.split(";");
                    byte[] key = KeyManager.decryptAlbumKey(KeyManager.hexStringToBytes(namekeySplit[1]));
                    KeyManager.addAlbumKey(namekeySplit[0], key);
                }
            }

            response = responseSplit[0];

        } catch (Exception e) {
            Log.e("MYDEBUG", "Exception: " + e.getMessage());
        }

        return response;
    }

    @Override
    protected void onPostExecute(final String response) {
        _activity.setmAuthTask(null);
        _activity.showProgress(false);

        Toast.makeText(_activity, response, Toast.LENGTH_LONG).show();

        if (response == null || response.equals(LOGIN_ERROR)) { //LOGIN_ERROR is returned or something else not expected
            Toast.makeText(_activity, "Something went wrong, try again later", Toast.LENGTH_LONG);
        }
        //Login success and starts app's main (AlbumsActivity) activity
        else if(response.equals(LOGIN_SUCCESS)) {
            Intent loginData;

            if(_appMode.equals(_activity.getApplicationContext().getString(R.string.AppModeDropBox))) {
                loginData = new Intent(_activity.getApplicationContext(), Dropbox_AlbumsActivity.class);
            }
            else { //wifiDirect
                loginData = new Intent(_activity.getApplicationContext(), WifiDirect_AlbumsActivity.class);
            }
            _activity.startActivity(loginData);
            _activity.finish();
        }
        else if(response.equals(LOGIN_UNKNOWN_USER)) {
            _activity.getmUsernameView().setError(_activity.getString(R.string.error_unknown_username));
            _activity.getmUsernameView().requestFocus();
        }
        else if(response.equals(LOGIN_INCORRECT_PASSWORD)) {
            _activity.getmPasswordView().setError(_activity.getString(R.string.error_incorrect_password));
            _activity.getmPasswordView().requestFocus();
        }
    }

    @Override
    protected void onCancelled() {
        _activity.setmAuthTask(null);
        _activity.showProgress(false);
    }
}

