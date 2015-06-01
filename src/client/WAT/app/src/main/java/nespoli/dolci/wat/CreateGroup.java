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
 * Activity that permits to a coordinator to create/rejoin the group
 *
 * @author Fabio
 */
public class CreateGroup extends Activity implements View.OnClickListener {

    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String URL_CREATE_GROUP = "http://www.watlocate.altervista.org/webservice/createGroup.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    JSONParser jsonParser = new JSONParser();
    private EditText group;
    private Button mCreate;
    private String username;
    private String groupID = "";
    private String coordinator = "1";
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        mCreate = (Button) findViewById(R.id.button_create);
        mCreate.setOnClickListener(this);
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

    public void onClick(View v) {
        group = (EditText) findViewById(R.id.groupCreated);
        groupID = group.getText().toString();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CreateGroup.this);
        username = sp.getString("username", "");

        //save preferences on a file
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", username);
        editor.putString("groupID", groupID);
        editor.commit();

        new sendNewGroup().execute();
    }

    class sendNewGroup extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CreateGroup.this);
            pDialog.setMessage("Adding group...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            int success;
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("groupID", groupID));
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("coordinator", coordinator));

                Log.d("request!", "starting");

                // post new group by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        URL_CREATE_GROUP, "POST", params);

                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Adding groupID ok", json.toString());

                    // save user data
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(CreateGroup.this);
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putString("username", username);
                    edit.commit();
                    // if groupID exists load the CoordMenu activity
                    Intent i = new Intent(CreateGroup.this, CoordMenu.class);
                    startActivity(i);
                    finish();

                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Adding Group Failure!", json.getString(TAG_MESSAGE));
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
                Toast.makeText(CreateGroup.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }
}
