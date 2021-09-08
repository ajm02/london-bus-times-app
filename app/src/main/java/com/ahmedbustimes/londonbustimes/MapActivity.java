package com.ahmedbustimes.londonbustimes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.Task;

import net.sharewire.googlemapsclustering.Cluster;
import net.sharewire.googlemapsclustering.ClusterManager;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    /**
     * MapActivity is an activity where you can use google maps to view the bus stops in London. For each stop, you can add it to your list in MainActivity,
     * and you can view the bus arrival times (which launches BusesActivity). Layout can be seen with activity_map.xml.
     */

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;

    private boolean locationPermissionGranted = false;
    private ArrayList<Stop> stopMarkerOptions; // All stops in London (names, codes and latlngs)
    private String launcher; // The activity which started this
    private Stop selectedStop; // The currently selected stop marker
    private GoogleMap map; // The map
    private ClusterManager<StopSign> clusterManager; // Manages clusters
    private FusedLocationProviderClient locationProvider; // Location Provider

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        stopMarkerOptions = new ArrayList<Stop>();
        getSupportActionBar().setHomeButtonEnabled(true); // Set back button on the toolbar
        getLocationPermission(); // Get permission to access your location

        launcher = getIntent().getStringExtra("starter_activity");

        if (launcher.equals("main")) {
            getDeviceLocation(); // If launched from MainActivity, display the camera position over your location
        }

        loadStopMarkers(); // Load the stop markers onto the map

        ImageView myLocationButton = (ImageView) findViewById(R.id.map_my_location);
        myLocationButton.setOnClickListener((view) -> getDeviceLocation()); // Set the my location button to display the camera over your location

        ImageView addStopButton = (ImageView) findViewById(R.id.map_add_button);
        // Set the add stop button to add the currently selected stop to list on MainActivity
        addStopButton.setOnClickListener((view) -> {

            if (selectedStop != null) {

                String lineToAdd = selectedStop.getStopName() + "|" + selectedStop.getStopCode() + "\n";
                int result = AddStopActivity.addStop(lineToAdd, selectedStop.getStopCode(), MapActivity.this);

                // If stop was added, success message
                if (result == 2) {
                    String displayText = "Added stop " + selectedStop.getStopName() + " to the list";
                    Toast.makeText(MapActivity.this, displayText, Toast.LENGTH_SHORT).show();
                }

                // If stop already present in the list, mention that
                else if (result == 1) {
                    String displayText = "Stop " + selectedStop.getStopName() + " is already present in the list";
                    Toast.makeText(MapActivity.this, displayText, Toast.LENGTH_SHORT).show();
                }

                // Otherwise, say the stop cannot be added
                else {
                    String displayText = "Failed to add stop";
                    Toast.makeText(MapActivity.this, displayText, Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView showTimesButton = (ImageView) findViewById(R.id.map_times_button);
        // Set the show times button to start BusesActivity to view times for the stop
        showTimesButton.setOnClickListener((view) -> {

            if (selectedStop != null) {

                Intent navBusActivity = new Intent(MapActivity.this, BusesActivity.class);
                // Put the stop details as extra
                navBusActivity.putExtra("stop_code", selectedStop.getStopCode());
                navBusActivity.putExtra("stop_name", selectedStop.getStopName());
                navBusActivity.putExtra("towards", selectedStop.getTowards());
                // Put the launcher activity
                navBusActivity.putExtra("starting_activity", "map");
                // Put the last latlng and zoom of the camera
                String lastPos = map.getCameraPosition().target.toString();
                String lastZoom = ((Float) map.getCameraPosition().zoom).toString();
                navBusActivity.putExtra("last_lat_lng_zoom", lastPos + "|" + lastZoom);
                startActivity(navBusActivity);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (grantResults.length > 0) {

                for (int i : grantResults) {

                    if (i != PackageManager.PERMISSION_GRANTED) {
                        locationPermissionGranted = false;
                    }
                }

                locationPermissionGranted = true;
            }
        }

        // After permissions requested, initialise the map
        initMap();
    }

    @Override
    public void onBackPressed() {
        Intent navMainMenu = new Intent(MapActivity.this, MainActivity.class);
        startActivity(navMainMenu);
    }

    /**
     * Parses the given last latlng and zoom of the camera and moves the camera there
     */
    private void getLastViewedLocation() {
        String lastLatLngZoom = getIntent().getStringExtra("latlngzoom");
        String[] posZoomParts = lastLatLngZoom.split("\\|"); // Contains latlng to be parsed and zoom
        String[] latLngString = posZoomParts[0].substring(posZoomParts[0].indexOf("(") + 1,
                posZoomParts[0].indexOf(")")).split(","); // This array contains latitude at 0 and longitude at 1
        LatLng latLng = new LatLng(Double.parseDouble(latLngString[0]), Double.parseDouble(latLngString[1]));
        float zoom = Float.parseFloat(posZoomParts[1]);
        moveCamera(latLng, zoom); // Move the camera to this latlng and zoom
    }

    /**
     * Checks to see if location permission is granted for this device, and requests them if not
     */
    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                initMap(); // Initialise map if location permission is granted
            }

            else {
                ActivityCompat.requestPermissions(this, permissions, 1); // Otherwise, request permissions
            }
        }

        else {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }

    }

    /**
     * Initialises the Google Map
     */
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                map = googleMap;
                // Remove current bus stop markers by loading style file
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.style));
                // Set a minimum zoom of 9.5f
                map.setMinZoomPreference(9.5f);
                // Remove the compass and my location buttons
                map.getUiSettings().setCompassEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);

                // Create a ClusterManager and set it to the map
                clusterManager = new ClusterManager<StopSign>(MapActivity.this, map);
                map.setOnCameraIdleListener(clusterManager);

                clusterManager.setCallbacks(new ClusterManager.Callbacks<StopSign>() {
                    @Override
                    public boolean onClusterClick(@NonNull Cluster<StopSign> cluster) {
                        return false;
                    }

                    @Override
                    public boolean onClusterItemClick(@NonNull StopSign clusterItem) {
                        // If a cluster item (marker) is clicked, select the stop it refers to

                        for (Stop stop : stopMarkerOptions) {

                            if (clusterItem.getLatitude() == stop.getLatitude() && clusterItem.getLongitude() == stop.getLongitude()) {
                                selectedStop = stop;
                            }
                        }

                        return false;
                    }
                });

                // If the launching activity was BusActivity, we want to get the last camera state
                if (launcher.equals("buses")) {
                    getLastViewedLocation();
                }
            }
        });
    }

    /**
     * Gets the current location of the device and moves the camera there
     */
    private void getDeviceLocation() {
        locationProvider = LocationServices.getFusedLocationProviderClient(this);

        try {

            if (locationPermissionGranted) {

                Task location = locationProvider.getLastLocation();
                location.addOnCompleteListener((task) -> {

                    if (task.isSuccessful()) {
                        Location currentLocation = (Location) task.getResult();
                        // If location was successfully retrieved, move the camera to that location
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        map.setMyLocationEnabled(true); // Enable my location
                    }

                    else {
                        // If location could not be found display a message
                        Toast.makeText(MapActivity.this, "Unable to find current location", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        }

        catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads stop names, codes and latlngs in London so they can be added as markers on the map
     */
    private void loadStopMarkers() {

        Thread thread = new Thread(() -> {

            try {
                stopMarkerOptions = TFLAPIInteract.getStopLatLng(MapActivity.this);
                runOnUiThread(this::addStopMarkers);
            }

            catch (Exception e) {
                Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        thread.start();
    }

    /**
     * Adds each stop as a marker on the map
     */
    private void addStopMarkers() {
        // Create an ArrayList containing cluster items (markers)
        ArrayList<StopSign> markerList = new ArrayList<StopSign>();

        for (Stop stop : stopMarkerOptions) {
            LatLng stopLatLng = new LatLng(stop.getLatitude(), stop.getLongitude());
            StopSign marker;

            if (!stop.getTowards().equals("")) {
                marker = new StopSign(stopLatLng, stop.getStopName(), "Towards " + stop.getTowards());
            }

            else {
                marker = new StopSign(stopLatLng, stop.getStopName(), stop.getTowards());
            }

            markerList.add(marker);
        }

        clusterManager.setItems(markerList); // Set the marker list as the list of elements for the cluster manager
    }

    /**
     * Moves the camera to the given location
     * @param latLng latitude and longitude of the location to move to
     * @param zoom the amount of zoom to set
     */
    private void moveCamera(LatLng latLng, float zoom) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
}
