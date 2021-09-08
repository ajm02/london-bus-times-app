package com.ahmedbustimes.londonbustimes;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    /**
     * MainActivity is the main screen that starts upon launch. It contains a list of stops that you have added and allows you to click on them to take you
     * to another activity which show the arrival times for the buses. Layout can be seen with activity_main.xml
     */

    private static final int ERROR_REQUEST = 9001;

    private ArrayList<Stop> stops = new ArrayList<Stop>();
    private StopListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadStopData();

        // Create a listview to hold the list of stops added
        ListView stopsList = (ListView) findViewById(R.id.stopList);
        StopListAdapter adapter = new StopListAdapter(this, R.layout.stop_card_layout, stops);
        stopsList.setAdapter(adapter);
        this.adapter = adapter;
        refreshList();

        // Clicking the add stop button should take us to the add stop screen, so lets set it:
        Button addStop = (Button) findViewById(R.id.addStopButton);
        addStop.setOnClickListener((view) -> {
            Intent launchAddActivity = new Intent(MainActivity.this, AddStopActivity.class);
            startActivity(launchAddActivity);
        });

        // If we can access google maps, then set the use map button to start the MapActivity
        if (canAccessGoogleMaps()) {
            Button useMap = (Button) findViewById(R.id.useMapButton);
            useMap.setOnClickListener((view) -> {
                Intent launchMapActivity = new Intent(MainActivity.this, MapActivity.class);
                launchMapActivity.putExtra("starter_activity", "main");
                startActivity(launchMapActivity);
            });
        }

        // Clicking on a stop on the list should take us to the bus times screen, where you can see the times for the buses at the stop
        stopsList.setOnItemClickListener((adapterView, view, i, l) -> {

            // First check the user is connected to the internet, if not, notify them and prevent access
            if (!TFLAPIInteract.isInternet(MainActivity.this)) {
                Toast fail = Toast.makeText(MainActivity.this, "Please ensure you are connected to the internet", Toast.LENGTH_SHORT);
                fail.show();
                return;
            }

            // Otherwise, start the buses activity, passing the stop code of the stop that was pressed
            Intent navBusActivity = new Intent(MainActivity.this, BusesActivity.class);
            navBusActivity.putExtra("stop_code", stops.get(i).getStopCode());
            navBusActivity.putExtra("stop_name", stops.get(i).getStopName());
            navBusActivity.putExtra("towards", stops.get(i).getTowards());
            navBusActivity.putExtra("starting_activity", "main");
            startActivity(navBusActivity);
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Pressing the back button here should not do anything
            return true;
        }

        return super.onKeyDown(keyCode, keyEvent);
    }

    /**
     * Loads the stop names and codes added to the stops list from stops.txt
     */
    public void loadStopData() {
        File stops = new File(getFilesDir() + File.separator + "stops.txt");
        ArrayList<String> stopsData = new ArrayList<String>();

        // If there is no file to store the data for the stops, make one
        if (!stops.exists()) {

            try {
                stops.createNewFile();
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Otherwise, read the data into this program
        else {

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput("stops.txt")));
                String line = "";

                while ((line = reader.readLine()) != null) {
                    stopsData.add(line);
                }
            }

            catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Populate the stop list
        this.stops.clear();
        ArrayList<Thread> threadList = new ArrayList<Thread>();
        for (String stopLine : stopsData) {
            String[] stopParts = stopLine.split("\\|");

            Thread thread = new Thread(() -> {
                String towards;

                try {
                    // Request the towards for each stop from tfl api
                    towards = TFLAPIInteract.findTowards(stopParts[1], MainActivity.this);
                }

                catch (NoInternetException e) {
                    // If failed, set towards as null
                    towards = "";
                }

                synchronized (this) {
                    this.stops.add(new Stop(stopParts[0], stopParts[1], towards));
                }
            });

            thread.start();
            threadList.add(thread);
        }

        for (Thread thread : threadList) {
            try {
                thread.join();
            }

            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes the given stop from the stops.txt
     * @param stopCode the stop code of the stop to remove
     */
    public void removeStop(String stopCode) {

        try {
            // Open stops.txt and a temp file called stops.txt
            BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput("stops.txt")));
            FileOutputStream tempWriter = openFileOutput("stopsTemp.txt", MODE_PRIVATE);

            String line = "";
            while ((line = reader.readLine()) != null) {

                if (!line.contains(stopCode)) { // Write every line from stops.txt to the temp file, excluding the line with the given stop code
                    tempWriter.write((line + System.lineSeparator()).getBytes());
                }
            }

            File stops = new File(getFilesDir() + File.separator + "stops.txt");
            File stopsTemp = new File(getFilesDir() + File.separator + "stopsTemp.txt");
            stopsTemp.renameTo(stops); // Rename the temp file to stops.txt, therefore successfully removing the line for the given stop
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the stops list, this should occur after a stop has been removed from the list
     */
    public void refreshList() {

        // Ensure the stops are sorted first
        Collections.sort(stops, new Comparator<Stop>() {
            @Override
            public int compare(Stop stop, Stop anotherStop) {
                return stop.getStopName().compareTo(anotherStop.getStopName());
            }
        });

        // Notify the adapter
        adapter.notifyDataSetChanged();
    }

    /**
     * Checks to see if Google Maps can be accessed
     * @return true if it can be accessed, otherwise false
     */
    public boolean canAccessGoogleMaps() {
        int availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (availability == ConnectionResult.SUCCESS) {
            return true;
        }

        else if (GoogleApiAvailability.getInstance().isUserResolvableError(availability)) {
            // If the error is resolvable, then show resolve dialog
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, availability, ERROR_REQUEST);
            dialog.show();
        }

        else {
            Toast.makeText(this, "Unable to access map", Toast.LENGTH_SHORT).show();
        }

        return false;
    }
}