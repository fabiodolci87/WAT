package nespoli.dolci.wat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class permits to add an appointment to the initinerary (only from the coordinator side)
 *
 * @author Fabio
 */
public class AddItinerary extends Activity implements View.OnClickListener {

    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String POST_ITINERARY_URL = "http://www.watlocate.altervista.org/webservice/addItinerary.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static String groupID;
    private EditText title, message;
    private Button mSubmit;
    private ProgressDialog pDialog;
    private TimePicker mTime;
    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_itinerary);
        title = (EditText) findViewById(R.id.title);
        message = (EditText) findViewById(R.id.message);
        mSubmit = (Button) findViewById(R.id.submit);
        mSubmit.setOnClickListener(this);
        mTime = (TimePicker) findViewById(R.id.timePicker);
        mTime.setIs24HourView(true);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        groupID = settings.getString("groupID", "xxx");
    }

    @Override
    public void onClick(View v) {
        new PostComment().execute();
    }

    /**
     * This method checks when the back button is pressed and ends the actual activity
     */
    @Override
    public void onBackPressed() {
        finish();
    }

    class PostComment extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AddItinerary.this);
            pDialog.setMessage("Posting Itinerary...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            int success;
            String post_ora = mTime.getCurrentHour() + ":" + mTime.getCurrentMinute() + ":00";
            String post_object = message.getText().toString();

            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("groupname", groupID));
                params.add(new BasicNameValuePair("hour", post_ora));
                params.add(new BasicNameValuePair("object", post_object));

                Log.d("request!", "starting");
                JSONObject json = jsonParser.makeHttpRequest(
                        POST_ITINERARY_URL, "POST", params);
                Log.d("Post Itinerary attempt", json.toString());
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Itinerary Added!", json.toString());
                    finish();
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Itinerary Failure!", json.getString(TAG_MESSAGE));
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
                Toast.makeText(AddItinerary.this, file_url, Toast.LENGTH_LONG).show();
            }
        }

    }


}

