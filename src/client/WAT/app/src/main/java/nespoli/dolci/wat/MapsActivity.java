package nespoli.dolci.wat;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Activity that opens a fragment into the app and displays the map
 * opened on the position of the user.
 *
 * @author Fabio
 */
public class MapsActivity extends FragmentActivity {

    LatLng myPosition, groupLocation;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        /**
         * Used to pass data Latitude from the previous activity to this one
         */
        String Latitude_txt = getIntent().getExtras().getString("Latitude");
        /**
         * Used to pass data Longitude from the previous activity to this one
         */
        String Longitude_txt = getIntent().getExtras().getString("Longitude");
        String Latitude_group_txt = getIntent().getExtras().getString("Latitude_group");
        String Longitude_group_txt = getIntent().getExtras().getString("Longitude_group");

        double Latitude = Double.parseDouble(Latitude_txt);
        double Longitude = Double.parseDouble(Longitude_txt);
        double Latitude_group = Double.parseDouble(Latitude_group_txt);
        double Longitude_group = Double.parseDouble(Longitude_group_txt);

        myPosition = new LatLng(Latitude, Longitude);
        groupLocation = new LatLng(Latitude_group, Longitude_group);

        // Getting reference to the SupportMapFragment
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting GoogleMap object from the fragment
        mMap = fm.getMap();

        // Enabling MyLocation Layer of Google Map
        // mMap.setMyLocationEnabled(true);
        mMap.addMarker(new MarkerOptions().position(myPosition).title("YOU").snippet("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        if (groupLocation.latitude != 0 && groupLocation.longitude != 0) {
            mMap.addMarker(new MarkerOptions().position(groupLocation).title("GROUP CENTER").snippet("The group is here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15.0f));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {

            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();

            }
        }
    }

    private void setUpMap() {
    }
}

