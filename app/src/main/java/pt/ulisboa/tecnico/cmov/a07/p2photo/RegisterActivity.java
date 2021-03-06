package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

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

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A register screen that offers login via username/password.
 */
public class RegisterActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView1;
    private EditText mPasswordView2;
    private View mProgressView;
    private View mRegisterFormView;
    private TextView mSignInLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the register form.
        mUsernameView = findViewById(R.id.register_username);
        populateAutoComplete();

        mPasswordView1 = findViewById(R.id.register_password);

        mPasswordView2 = findViewById(R.id.register_confirm_password);
        mPasswordView2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSignUpButton = findViewById(R.id.username_sign_up_button);
        mUsernameSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();

            }
        });

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);

        //Click in link to start login activity
        mSignInLink = findViewById(R.id.link_sign_in);
        mSignInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
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
    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView1.setError(null);
        mPasswordView2.setError(null);

        // Store values at the time of the register attempt.
        String username = mUsernameView.getText().toString();
        String password1 = mPasswordView1.getText().toString();
        String password2 = mPasswordView2.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid confirmed password.
        if (TextUtils.isEmpty(password2)) {
            mPasswordView2.setError(getString(R.string.error_field_required));
            focusView = mPasswordView2;
            cancel = true;
        } else if (!password2.equals(password1)) {
            mPasswordView2.setError(getString(R.string.error_retyped_password_different));
            focusView = mPasswordView2;
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(password1)) {
            mPasswordView1.setError(getString(R.string.error_field_required));
            focusView = mPasswordView1;
            cancel = true;
        } else if (!isPasswordValid(password1)) {
            focusView = mPasswordView1;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            ContextClass context = (ContextClass) getApplicationContext();
            String appMode = context.getAppMode();
            mAuthTask = new UserRegisterTask(username, password2, appMode,this);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username) {
        if(username.matches("[^a-zA-Z]*[a-zA-Z].*")) {
            if(username.contains(",") || username.contains(";")) {
                mUsernameView.setError(getString(R.string.error_illegal_chars_username));
                return false;
            }
            return true;
        }
        mUsernameView.setError(getString(R.string.error_invalid_username));
        return false;
    }

    private boolean isPasswordValid(String password) {
        if(!password.matches("[^a-z]*[a-z].*")){
            mPasswordView1.setError(getString(R.string.error_invalid_password_small));
            return false;
        }
        else if (!(password.length() > 3) ){
            mPasswordView1.setError(getString(R.string.error_invalid_password_len));
            return false;
        }
        else if(!password.matches("[^A-Z]*[A-Z].*")){
            mPasswordView1.setError(getString(R.string.error_invalid_password_capital));
            return false;
        }
        else if(!password.matches("[^\\d]*\\d.*")){
            mPasswordView1.setError(getString(R.string.error_invalid_password_number));
            return false;
        }
        else if(!password.matches("[^.-_@]*[.-_@].*") ){
            mPasswordView1.setError(getString(R.string.error_invalid_password_symbol));
            return false;
        }
        else {
            return true;
        }
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

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
                new ArrayAdapter<>(RegisterActivity.this,
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

    public AutoCompleteTextView getmUsernameView() {
        return mUsernameView;
    }

    public void setmUsernameView(AutoCompleteTextView mUsernameView) {
        this.mUsernameView = mUsernameView;
    }

    public UserRegisterTask getmAuthTask() {
        return mAuthTask;
    }

    public void setmAuthTask(UserRegisterTask mAuthTask) {
        this.mAuthTask = mAuthTask;
    }

}


/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class UserRegisterTask extends AsyncTask<Void, Void, String> {

    private final String mUsername;
    private final String mPassword;
    private RegisterActivity _activity;
    private static final int REQUEST_REGISTER_CODE = 1;
    private static final String USERNAME_EXTRA = "username";
    private static final String PASSWORD_EXTRA = "password";

    //server response types to request attempt
    private static final String REGISTER_SUCCESS = "Success";
    private static final String REGISTER_USERNAME_ALREADY_EXISTS = "UsernameAlreadyExists";
    private static final String REGISTER_ERROR = "Error";
    private String _appMode;
    KeyPair keyPair = null;


    UserRegisterTask(String username, String password, String appMode, RegisterActivity act) {
        mUsername = username;
        mPassword = password;
        _activity = act;
        _appMode = appMode;
    }

    @Override
    protected String doInBackground(Void... params) {
        if(_appMode.equals(_activity.getApplicationContext().getString(R.string.AppModeDropBox))) {
            keyPair = KeyManager.generateKeyPair();
        }
        // Register the new account
        String response = null;
        try {
            URL url = new URL(_activity.getString(R.string.serverAddress) + "/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("username", mUsername);
            postDataParams.put("password", mPassword);
            String pk = "";
            if(_appMode.equals(_activity.getApplicationContext().getString(R.string.AppModeDropBox))) {
                pk = KeyManager.byteArrayToHexString(keyPair.getPublic().getEncoded());
            }
            postDataParams.put("publicKey",pk);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(postDataParams.toString().getBytes());
            os.close();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = SessionHandler.convertStreamToString(in);

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

        if (response == null || response.equals(REGISTER_ERROR)){ //REGISTER_ERROR is returned or something else not expected
            Toast.makeText(_activity, "Something went wrong, try again later", Toast.LENGTH_LONG);
        }
        else if (response.equals(REGISTER_SUCCESS)) {
            // Send information to the login page and make login automatically
            if(_appMode.equals(_activity.getApplicationContext().getString(R.string.AppModeDropBox))){
                KeyManager.writeKeyPair(mUsername, _activity);
            }
            Intent accountData = new Intent();
            accountData.putExtra(USERNAME_EXTRA, mUsername);
            accountData.putExtra(PASSWORD_EXTRA, mPassword);
            _activity.setResult(_activity.RESULT_OK, accountData);
            _activity.finish();
        }
        else if(response.equals(REGISTER_USERNAME_ALREADY_EXISTS)) {
            _activity.getmUsernameView().setError(_activity.getString(R.string.error_username_taken));
            _activity.getmUsernameView().requestFocus();
        }
    }

    @Override
    protected void onCancelled() {
        _activity.setmAuthTask(null);
        _activity.showProgress(false);
    }
}