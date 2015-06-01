package nespoli.dolci.wat;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
 * This activity is used by user and permits
 * to write a comment and post it on the database
 *
 * @author Fabio
 */

public class AddComment extends Activity {

    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String POST_COMMENT_URL = "http://www.watlocate.altervista.org/webservice/addComment.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static String groupID, username;
    private static String name_pic = null;
    private boolean gallgone = false;
    private EditText title, message;
    private Button mSubmit, mLoadGall;
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        title = (EditText) findViewById(R.id.title);
        message = (EditText) findViewById(R.id.message);
        mSubmit = (Button) findViewById(R.id.submit);
        mLoadGall = (Button) findViewById(R.id.buttonLoadPicture);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        username = settings.getString("username", "utente");
        groupID = settings.getString("groupID", "xxx");
    }

    protected void onResume() {
        super.onResume();

        mSubmit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                new PostComment().execute();
            }
        });

        mLoadGall.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                gallgone = true;
                Intent load_pic = new Intent(AddComment.this, InsertPic.class);
                startActivity(load_pic);
            }
        });

    }

    /**
     * This class opens an HTTP connection towards the server and
     * permits to insert comments into the SQL database
     */

    class PostComment extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AddComment.this);
            pDialog.setMessage("Posting Comment...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            int success;
            String post_title = title.getText().toString();
            String post_message = message.getText().toString();
            name_pic = null;

            if (gallgone == true) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                String fileTemp = settings.getString("file_name", "name_pic");
                name_pic = fileTemp.substring(fileTemp.lastIndexOf("/") + 1);
            }

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("title", post_title));
                params.add(new BasicNameValuePair("message", post_message));
                params.add(new BasicNameValuePair("groupID", groupID));
                params.add(new BasicNameValuePair("name_pic", name_pic));

                Log.d("request!", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        POST_COMMENT_URL, "POST", params);

                Log.d("Post Comment attempt", json.toString());

                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Comment Added!", json.toString());
                    finish();
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Comment Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(AddComment.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }
}

