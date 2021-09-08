package com.ahmedbustimes.londonbustimes;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BusesActivity extends AppCompatActivity {

    /**
     * BusesActivity is an activity where you can view the buses and arrival times for the stop you clicked on in the MainActivity, or chose to
     * view the times for in MapActivity. Layout can be seen with activity_display_buses.xml
     */

    private String launcher; // The activity which started this
    private String lastLatLngZoom; // Last camera position if started from MapActivity

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_buses);

        String stopCode = getIntent().getStringExtra("stop_code"); // Get the stop code of the stop that was clicked on
        String stopName = getIntent().getStringExtra("stop_name"); // Get the stop name
        String towards = getIntent().getStringExtra("towards"); // Get towards
        launcher = getIntent().getStringExtra("starting_activity");
        lastLatLngZoom = getIntent().getStringExtra("last_lat_lng_zoom");

        TextView title = (TextView) findViewById(R.id.stop_title);
        TextView towardsWords = (TextView) findViewById(R.id.towards_title);
        title.setText(stopName); // Set title as stop name

        // Set towards
        if (!towards.equals("")) {
            towards = "Towards " + towards;
            towardsWords.setText(towards);
        }

        else {
            towardsWords.setText("");
        }

        Thread thread = new Thread(() -> {

            try {
                ArrayList<BusCardDetails> busTimes = TFLAPIInteract.getTimes(stopCode); // Get arraylist of BusCardDetails for this stop code

                runOnUiThread(() -> {
                    // Create a listview to display the BusCardDetails
                    ListView busList = (ListView) findViewById(R.id.list);
                    BusListAdapter adapter = new BusListAdapter(BusesActivity.this, R.layout.bus_card_layout, busTimes);
                    busList.setAdapter(adapter);
                });
            }

            catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();

        // Set the back button to allow us to return to the main menu
        Button backButton = (Button) findViewById(R.id.backButton2);
        backButton.setOnClickListener((view) -> {
            goBack();
        });
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    /**
     * Navigate back to the launcher activity
     */
    private void goBack() {

        if (launcher.equals("main")) {
            Intent returnMainMenu = new Intent(BusesActivity.this, MainActivity.class);
            startActivity(returnMainMenu);
        }

        else if (launcher.equals("map")) {
            Intent returnMapActivity = new Intent(BusesActivity.this, MapActivity.class);
            // If MapActivity was the launcher activity, we must get the last camera position
            returnMapActivity.putExtra("starter_activity", "buses");
            returnMapActivity.putExtra("latlngzoom", lastLatLngZoom);
            startActivity(returnMapActivity);
        }
    }
}
