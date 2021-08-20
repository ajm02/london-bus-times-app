package com.ahmedbustimes.londonbustimes;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BusesActivity extends AppCompatActivity {

    /**
     * BusesActivity is an activity where you can view the buses and arrival times for the stop you clicked on in the MainActivity. Layout can be seen with
     * activity_display_buses.xml
     */

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_buses);

        String stopCode = getIntent().getStringExtra("stop_code"); // Get the stop code of the stop that was clicked on in the MainActivity

        Thread thread = new Thread(() -> {

            try {
                ArrayList<BusCardDetails> busTimes = APIInteract.getTimes(stopCode); // Get arraylist of BusCardDetails for this stop code

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
            Intent returnMainMenu = new Intent(BusesActivity.this, MainActivity.class);
            startActivity(returnMainMenu);
        });
    }
}
