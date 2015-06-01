package nespoli.dolci.wat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that permits an user to join an existing group
 *
 * @author Fabio
 */

public class JoinGroup extends Activity implements View.OnClickListener {

    private static final String URL_VERIFY_GROUP = "http://www.watlocate.altervista.org/webservice/verifyGroup.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    public static final String PREFS_NAME = "MyPrefsFile";
    private Button mJoin;
    private EditText group;
    private String username;
    private String groupID = "";
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        mJoin = (Button) findViewById(R.id.button_join_final);
        mJoin.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    protected void onPause() {
        super.onPause();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        username = settings.getString("username", "utente");
        groupID = settings.getString("groupID", "gruppo");
    }

    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        username = settings.getString("username", "utente");
        groupID = settings.getString("groupID", "gruppo");
    }

    /**
     * When the button is pressed, HTTP connection towards the server starts
     * in order to check if the group already exists. If it doesn't exists
     * return an error to the user (the user cannot join a group taht doesn't exist)
     *
     * @param v
     */
    public void onClick(View v) {

        group = (EditText) findViewById(R.id.groupJoined);
        groupID = group.getText().toString();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(JoinGroup.this);
        String username = sp.getString("username", "");

        //save preferences on a file
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", username);
        editor.putString("groupID", groupID);
        editor.commit();

        new VerifyGroup().execute();
    }

    class VerifyGroup extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(JoinGroup.this);
            pDialog.setMessage("Veryfing group...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("groupID", groupID));

                Log.d("request!", "starting");
                // gettingverify group by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        URL_VERIFY_GROUP, "POST", params);

                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Verifying groupID ok", json.toString());
                    // save user data
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(JoinGroup.this);
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putString("username", username);
                    edit.commit();
                    // if groupID exists load the usermenu activity
                    Intent i = new Intent(JoinGroup.this, UserMenu.class);
                    startActivity(i);
                    finish();

                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("GroupID Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {

            pDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(JoinGroup.this, file_url, Toast.LENGTH_LONG).show();
            }

        }

    }

}
