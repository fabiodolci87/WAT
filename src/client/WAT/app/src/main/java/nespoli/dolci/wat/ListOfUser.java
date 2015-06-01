package nespoli.dolci.wat;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity that shows to the coordinator the list of the users logged to the actual group
 *
 * @author Mirko
 */

public class ListOfUser extends ListActivity {

    private static final String READ_LIST_USER_URL = "http://www.watlocate.altervista.org/webservice/listUser.php";
    private static final String TAG_POSTS = "posts";
    private static final String TAG_USER = "username";
    private static final String TAG_COORD_LAT = "latitude";
    private static final String TAG_COORD_LON = "longitude";
    public static final String PREFS_NAME = "MyPrefsFile";
    private static String groupID;
    private static String latitude_user;
    private static String longitude_user;
    private static String user;
    private static String username;
    private ProgressDialog pDialog;
    private JSONArray mListUser = null;
    private HashMap<String, String> tempMap = null;
    private ArrayList<HashMap<String, String>> mListUserList;
    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_user);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        username = settings.getString("username", "utente");
        groupID = settings.getString("groupID", "gruppo");
    }

    /**
     * When the activity comes back in the foreground the list of the user is refreshed
     */
    @Override
    protected void onResume() {

        super.onResume();
        new LoadListUser().execute();
    }

    public void updateJSONdata() {

        mListUserList = new ArrayList<>();

        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("groupID", groupID));

            JSONObject json = jsonParser.makeHttpRequest(
                    READ_LIST_USER_URL, "POST", params);
            mListUser = json.getJSONArray(TAG_POSTS);

            for (int i = 0; i < mListUser.length(); i++) {
                JSONObject c = mListUser.getJSONObject(i);

                //gets the content of each tag
                String username = c.getString(TAG_USER);
                String latitude = c.getString(TAG_COORD_LAT);
                String longitude = c.getString(TAG_COORD_LON);

                HashMap<String, String> map = new HashMap<String, String>();

                map.put(TAG_USER, username);
                map.put(TAG_COORD_LAT, latitude);
                map.put(TAG_COORD_LON, longitude);

                mListUserList.add(map);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method updates the list of the user loading the complete
     * list of the users from the server
     */
    private void updateList() {
        ListAdapter adapter = new SimpleAdapter(this, mListUserList,
                R.layout.single_act_itinerary, new String[]{TAG_USER
        }, new int[]{R.id.title});

        setListAdapter(adapter);

        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                tempMap = mListUserList.get(position);
                latitude_user = tempMap.get(TAG_COORD_LAT);
                longitude_user = tempMap.get(TAG_COORD_LON);
                user = tempMap.get(TAG_USER);
                if (user.equals(username)) {
                    Toast.makeText(ListOfUser.this, "You know where you are! :-)",
                            Toast.LENGTH_SHORT).show();
                } else {
                    String uriBegin = "geo:" + latitude_user + "," + longitude_user;
                    String query = latitude_user + "," + longitude_user + "(" + user + ")";
                    String encodedQuery = Uri.encode(query);
                    String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                    Uri uri = Uri.parse(uriString);
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });
    }

    public class LoadListUser extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ListOfUser.this);
            pDialog.setMessage("Loading User...");
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
