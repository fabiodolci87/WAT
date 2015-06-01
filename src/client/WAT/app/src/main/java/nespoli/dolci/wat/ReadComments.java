package nespoli.dolci.wat;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
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
 * This activity is used to read the comments of the groupID
 *
 * @author Fabio
 */

public class ReadComments extends ListActivity {

    private static final String READ_COMMENTS_URL = "http://www.watlocate.altervista.org/webservice/comments.php";
    private static final String TAG_TITLE = "title";
    private static final String TAG_POSTS = "posts";
    private static final String TAG_USERNAME = "username";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_PIC = "name_pic";
    public static final String PREFS_NAME = "MyPrefsFile";
    private static String groupID;
    private static String linkimg;
    private ProgressDialog pDialog;
    private JSONArray mComments = null;
    private ArrayList<HashMap<String, String>> mCommentList,mTempComment;
    private HashMap<String, String> tempMap = null;
    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_comments);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        groupID = settings.getString("groupID", "xxx");
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * When the activity comes back to the foreground the list of the comments is loaded
     */
    @Override
    protected void onResume() {
        super.onResume();
        new LoadComments().execute();
    }

    public void addComment(View v) {
        Intent i = new Intent(ReadComments.this, AddComment.class);
        startActivity(i);
    }

    //Retrieves json data of comments
    public void updateJSONdata() {

        mCommentList = new ArrayList<>();
        mTempComment = new ArrayList<>();

        try {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("groupID", groupID));

            JSONObject json = jsonParser.makeHttpRequest(
                    READ_COMMENTS_URL, "POST", params);
            mComments = json.getJSONArray(TAG_POSTS);

            // looping through all posts according to the json object returned
            for (int i = 0; i < mComments.length(); i++) {
                JSONObject c = mComments.getJSONObject(i);

                //gets the content of each tag
                String title = c.getString(TAG_TITLE);
                String content = c.getString(TAG_MESSAGE);
                String username = c.getString(TAG_USERNAME);
                String immagine = c.getString(TAG_PIC);

                HashMap<String, String> map = new HashMap<String, String>();

                map.put(TAG_TITLE, title);
                map.put(TAG_MESSAGE, content);
                map.put(TAG_USERNAME, username);
                map.put(TAG_PIC, immagine);

                // adding HashList to ArrayList
                mCommentList.add(map);

                HashMap<String, String> map2 = new HashMap<String, String>();

                map2.put(TAG_TITLE, title);
                map2.put(TAG_MESSAGE, content);
                map2.put(TAG_USERNAME, username);
                if (immagine != "null" && immagine.length() != 0)
                    map2.put(TAG_PIC, "PHOTO " + "\u2714");
                else
                    map2.put(TAG_PIC, "");
                {}

                // adding HashList to ArrayList
                mTempComment.add(map2);
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method update the list of the comments requesting the up to date list
     * to the server. If an image is present in the comment it will be downloaded
     * and showed to the user.
     */
    private void updateList() {


        ListAdapter adapter = new SimpleAdapter(this, mTempComment,
                R.layout.single_post, new String[]{TAG_TITLE, TAG_MESSAGE,
                TAG_USERNAME, TAG_PIC}, new int[]{R.id.title, R.id.message,
                R.id.username, R.id.textView5});


        setListAdapter(adapter);
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                tempMap = mCommentList.get(position);
                linkimg = tempMap.get(TAG_PIC);
                if (linkimg != "null" && linkimg.length() != 0) {
                    Intent intent = new Intent(ReadComments.this, PicFromUrl.class);
                    intent.putExtra("nome_pic", linkimg);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public class LoadComments extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ReadComments.this);
            pDialog.setMessage("Loading Comments...");
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