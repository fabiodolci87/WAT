package nespoli.dolci.wat;


import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This activity is used by a the coordinator user and
 * displays the menu of the coordinator and permits to do the basic operation.
 *
 * @author Fabio
 */

public class CoordMenu extends ActionBarActivity implements LocationListener {



    private static final int MIN_DIST = 20; // Defines the minimum distance interval(m) to trigger the change of location
    private static final int MIN_PERIOD = 60000; //Defines the minimum temporal interval (ms) to trigger the update of the location
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String POST_UPDATE_POSITION = "http://www.watlocate.altervista.org/webservice/updateCoordinates.php";
    private static final String READ_BARYCENTER_URL = "http://www.watlocate.altervista.org/webservice/groupLocation.php";
    private static final String READ_NEW_COMMENT_URL = "http://watlocate.altervista.org/webservice/newComment.php";
    private static final String DELETE_URL = "http://watlocate.altervista.org/webservice/delete.php";
    private static final String CALL_ALL_URL = "http://watlocate.altervista.org/webservice/addCoordCall.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_LAT_AVG = "latAVG";
    private static final String TAG_LONG_AVG = "longAVG";
    private static final String TAG_NO_GROUP = "NO-GROUP";
    private double latitude = 0;
    private double longitude = 0;
    private double latitude_group = 0;
    private double longitude_group = 0;
    private String providerId = LocationManager.NETWORK_PROVIDER;
    private LocationManager locationManager = null;
    private String coordinator = "1";
    private Button mMap, mTake, mItinerary, mComments, mUsers, mLeave, mRemove, mCallAll;
    private String username = "";
    private String groupID = "";
    private String oldGroupID = "";
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    ScheduledExecutorService scheduledExecutorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coord_menu);
        ActionBar m_myActionBar = getSupportActionBar();
        m_myActionBar.hide();

        mMap = (Button) findViewById(R.id.button_map_coord);
        mTake = (Button) findViewById(R.id.button_take_coord);
        mItinerary = (Button) findViewById(R.id.button_itinerary_coord);
        mComments = (Button) findViewById(R.id.button_comments_coord);
        mUsers = (Button) findViewById(R.id.button_list_users);
        mLeave = (Button) findViewById(R.id.button_end);
        mRemove = (Button) findViewById(R.id.button_remove);
        mCallAll = (Button) findViewById(R.id.button_call_all);

        //wake up and take fast the position
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
            Intent gpsOptionsIntent = new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
        } else
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_PERIOD, MIN_DIST, this);

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        username = settings.getString("username", "utente");
        groupID = settings.getString("groupID", "gruppo");

        scheduledExecutorService = Executors.newScheduledThreadPool(1);

        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Log.v("PeriodicTimerService", "Awake");
                newComment();
            }
        }, 3, 60, TimeUnit.SECONDS);

    }

    /**
     * Here the menu is initialized when the options button is pressed
     *
     * @param menu the name of the menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_coord_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), HelpActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method called when the activity is resumed on the foreground
     */
    protected void onResume() {
        super.onResume();
        // GPS starts and collect data
        locationManager.requestLocationUpdates(providerId, MIN_PERIOD, MIN_DIST, this);

        if (groupID != TAG_NO_GROUP) {
            new AttemptBarycenter().execute();
            if (latitude != 0 && longitude != 0) {
                new PostPosition().execute();
            }
        }

        //handle button to pass to MAPS
        mMap.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                if (latitude == 0 && longitude == 0) {
                    Toast.makeText(CoordMenu.this, "Wait until position is acquired",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent openMap = new Intent(CoordMenu.this, MapsActivity.class);

                    openMap.putExtra("Latitude", String.valueOf(latitude));
                    openMap.putExtra("Longitude", String.valueOf(longitude));
                    openMap.putExtra("Latitude_group", String.valueOf(latitude_group));
                    openMap.putExtra("Longitude_group", String.valueOf(longitude_group));

                    startActivity(openMap);
                }
            }
        });

        //handle navigation towards the group
        mTake.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                if (latitude == 0 && longitude == 0) {
                    Toast.makeText(CoordMenu.this, "Wait until position is acquired",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (latitude_group == 0 && longitude_group == 0) {
                        Toast.makeText(CoordMenu.this, "Wait group position",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Intent take_group = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + latitude_group + "," + longitude_group + "&mode=w"));
                        startActivity(take_group);
                    }
                }
            }
        });

        //itinerary button
        mItinerary.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                Intent itinerary = new Intent(CoordMenu.this, ItineraryCoord.class);
                startActivity(itinerary);
            }
        });

        //read comments button
        mComments.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                Intent read_comments = new Intent(CoordMenu.this, ReadComments.class);
                startActivity(read_comments);
            }
        });

        //list user button
        mUsers.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                Intent seeUsers = new Intent(CoordMenu.this, ListOfUser.class);
                startActivity(seeUsers);
            }
        });

        //exit button
        mLeave.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                alertMessageQuit();
            }
        });

        //remove data button
        mRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                alertMessageDelete();
            }
        });

        //call all button
        mCallAll.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                new CallEveryone().execute();
            }
        });

    }

    /**
     * popup when delete is pressed (Are you sure? Y/N)
     */
    public void alertMessageDelete() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        new RemoveGroup().execute();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to delete all group data?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    /**
     * popup when quit is pressed (Are you sure? Y/N)
     */
    public void alertMessageQuit() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        oldGroupID = groupID;
                        groupID = "NO-GROUP";
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(CoordMenu.this);
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putString("groupID", TAG_NO_GROUP);
                        edit.commit();
                        //post null group (to remove the user from the group)
                        new PostPosition().execute();
                        Toast.makeText(CoordMenu.this, "LEAVING " + oldGroupID,
                                Toast.LENGTH_SHORT).show();
                        Intent leave = new Intent(CoordMenu.this, JoinCreate.class);
                        scheduledExecutorService.shutdownNow();
                        finish();
                        startActivity(leave);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to leave the group?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }


    /**
     * When the location is changed the system posts the new location to the server
     * and retrieves the updated position of the barycenter of the group
     *
     * @param location this parameter contains the location acquired by the
     *                 location provider (GPS-NETWORK)
     */
    @Override

    public void onLocationChanged(Location location) {
        updateGUI(location);
        //Send position and retrive barycenter
        if (groupID != TAG_NO_GROUP) {
            new AttemptBarycenter().execute();
            new PostPosition().execute();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(CoordMenu.this, "Location " + providerId + " enabled!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(CoordMenu.this, "Location " + providerId + " disabled!", Toast.LENGTH_SHORT).show();
    }

    private void updateGUI(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onBackPressed() {
        alertMessageQuit();
    }

    /**
     * Method that checks if a new message is posted on the group, return
     * a notification when something new is found
     */
    void newComment() {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("groupID", groupID));
        int success;

        try {
            Log.v("PeriodicTimerService", "newMSG");

            JSONObject json = jsonParser.makeHttpRequest(READ_NEW_COMMENT_URL, "POST", params);
            success = json.getInt(TAG_SUCCESS);
            if (success == 1) {

                Intent intent1 = new Intent(CoordMenu.this, ReadComments.class);
                PendingIntent pIntent = PendingIntent.getActivity(CoordMenu.this, 0, intent1, 0);

                Notification noti = new Notification.Builder(CoordMenu.this)
                        .setContentTitle("New Comments available")
                        .setContentText("Gruppo = " + groupID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setVibrate(new long[]{100, 500, 100, 500})
                        .setContentIntent(pIntent).build();
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                noti.flags |= Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify(0, noti);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class PostPosition extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            int success;
            try {

                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("groupID", groupID));
                params.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
                params.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));
                params.add(new BasicNameValuePair("coordinator", coordinator));
                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(POST_UPDATE_POSITION, "POST", params);

                // full json response
                Log.d("Post Position attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Position Added!", json.toString());
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Position Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class AttemptBarycenter extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CoordMenu.this);
            pDialog.setMessage("Attempting retrieve group location...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;

            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("groupID", groupID));
                params.add(new BasicNameValuePair("username", username));
                Log.d("request!", "starting");

                // getting barycenter by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(READ_BARYCENTER_URL, "POST", params);

                Log.d("Fetching group attempt", json.toString());

                //upon successful login, save username:
                success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    Log.d("fetching location OK", json.toString());
                    latitude_group = json.getDouble(TAG_LAT_AVG);
                    longitude_group = json.getDouble(TAG_LONG_AVG);
                    return "barycenter ok";
                } else {
                    Log.d("fetching location fail", json.getString(TAG_MESSAGE));
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
                Toast.makeText(CoordMenu.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }

    class RemoveGroup extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CoordMenu.this);
            pDialog.setMessage("Attempting delete group hystory...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... args) {

            int success;

            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("groupID", groupID));
                Log.d("request!", "starting");

                // remove group hystory by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(DELETE_URL, "POST", params);

                // check your log for json response
                Log.d("Fetching group attempt", json.toString());

                //upon successful login, save username:
                success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    Log.d("delete OK", json.toString());
                    return "DELETE ok";
                } else {
                    Log.d("delete fail", json.getString(TAG_MESSAGE));
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
                Toast.makeText(CoordMenu.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }

    class CallEveryone extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CoordMenu.this);
            pDialog.setMessage("Calling everyone...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            int success;
            String latCall = Double.toString(latitude);
            String longCall = Double.toString(longitude);

            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("longitude", longCall));
                params.add(new BasicNameValuePair("latitude", latCall));
                params.add(new BasicNameValuePair("groupID", groupID));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        CALL_ALL_URL, "POST", params);

                Log.d("Call All attempt", json.toString());

                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Call Executed!", json.toString());
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Call Failure!", json.getString(TAG_MESSAGE));
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
                Toast.makeText(CoordMenu.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }
}
