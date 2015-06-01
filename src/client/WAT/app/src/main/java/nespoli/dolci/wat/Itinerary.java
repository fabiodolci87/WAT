package nespoli.dolci.wat;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is called to take a look to the itinerary (only by a normal user)
 *
 * @author Mirko
 */
public class Itinerary extends ListActivity {


    private static final String READ_ITINERARY_URL = "http://www.watlocate.altervista.org/webservice/itinerary.php";
    private static final String TAG_POSTS = "posts";
    private static final String TAG_NAME = "groupname";
    private static final String TAG_ORA = "hour";
    private static final String TAG_OBJECT = "object";
    public static final String PREFS_NAME = "MyPrefsFile";
    private static String groupID;
    private ProgressDialog pDialog;
    private JSONArray mItinerary = null;
    private ArrayList<HashMap<String, String>> mItineraryList;
    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        groupID = settings.getString("groupID", "xxx");
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadItinerary().execute();
    }

    public void updateJSONdata() {

        mItineraryList = new ArrayList<>();

        try {
            //add groupID to the HTTP POST request
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("groupname", groupID));

            JSONObject json = jsonParser.makeHttpRequest(
                    READ_ITINERARY_URL, "POST", params);
            mItinerary = json.getJSONArray(TAG_POSTS);

            for (int i = 0; i < mItinerary.length(); i++) {
                JSONObject c = mItinerary.getJSONObject(i);

                String name = c.getString(TAG_NAME);
                String ora2 = c.getString(TAG_ORA);
                String ora = ora2.substring(0,5);
                String object = c.getString(TAG_OBJECT);

                HashMap<String, String> map = new HashMap<String, String>();

                map.put(TAG_NAME, name);
                map.put(TAG_ORA, ora);
                map.put(TAG_OBJECT, object);

                mItineraryList.add(map);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateList() {
        ListAdapter adapter = new SimpleAdapter(this, mItineraryList,
                R.layout.single_act_itinerary, new String[]{TAG_ORA,
                TAG_OBJECT}, new int[]{R.id.title, R.id.message});

        setListAdapter(adapter);

        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                //insert commands to do when item is clicked
            }
        });
    }

    public class LoadItinerary extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Itinerary.this);
            pDialog.setMessage("Loading Itinerary...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            updateJSONdata();
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            updateList();
        }
    }
}
