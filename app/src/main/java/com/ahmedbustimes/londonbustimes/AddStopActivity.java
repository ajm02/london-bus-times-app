package com.ahmedbustimes.londonbustimes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.text.WordUtils;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AddStopActivity extends AppCompatActivity {

    /**
     * AddStopActivity is an activity where you can add stops to the list of clickable stops in the MainActivity. It's layout can be seen with activity_add_stop.xml.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stop);

        // Navigate back to the main menu when the back button is pressed
        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener((view) -> {
            goBack();
        });

        // Add the stop given to the stop list
        Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener((view) -> {
            // First get the given stop name and stop code and clear
            String stopName = ((TextView) findViewById(R.id.stopName)).getText().toString();
            String stopCode = ((TextView) findViewById(R.id.stopCode)).getText().toString();
            ((EditText) findViewById(R.id.stopName)).getText().clear();
            ((EditText) findViewById(R.id.stopCode)).getText().clear();

            // If a stop code was given, attempt to find the name of the stop
            if (!stopCode.equals("")) {

                // Networking, so lets start a new thread
                Thread thread = new Thread(() -> {

                    try {
                        String foundStopName = TFLAPIInteract.findStopName(stopCode, AddStopActivity.this); // Find the name of this stop
                        String lineToAdd = foundStopName + "|" + stopCode + "\n";
                        int result = addStop(lineToAdd, stopCode, AddStopActivity.this); // Add the stop and store the result

                        runOnUiThread(() -> {
                            String text;

                            // If stop was added, success message
                            if (result == 2) {
                                text = "Added stop " + foundStopName + " to the list";
                            }

                            // If stop is already present in the list, mention that
                            else if (result == 1) {
                                text = "Stop " + foundStopName + " is already present in the list";
                            }

                            // Otherwise, say the stop cannot be added
                            else {
                                text = "Failed to add stop";
                            }
                            Toast success = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                            success.show();
                        });
                    }

                    // If user is not connected to the internet, tell them to do so
                    catch (NoInternetException e) {
                        runOnUiThread(() -> {
                            Toast fail = Toast.makeText(AddStopActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                            fail.show();
                        });
                    }

                    // If the stop code is not valid
                    catch(Exception e) {
                        runOnUiThread(() -> {
                            Toast fail = Toast.makeText(AddStopActivity.this, "Error: Cannot find stop code " + stopCode, Toast.LENGTH_SHORT);
                            fail.show();
                        });
                    }
                });

                thread.start();
            }

            // If a stop name alone is given, attempt to find the stop codes for the stops with this name
            else if (!stopName.equals("")) {

                Thread thread = new Thread(() -> {

                    try {
                        ArrayList<String> foundStopCodes = TFLAPIInteract.findStopCodes(stopName, AddStopActivity.this); // Find the stop codes
                        ArrayList<String> linesToAdd = new ArrayList<String>();

                        for (String i : foundStopCodes) {
                            linesToAdd.add(WordUtils.capitalizeFully(stopName) + "|" + i + "\n");
                        }

                        boolean addedStops = false;

                        for (int i = 0; i < foundStopCodes.size(); i++) {

                            // If a single stop is successfully added, then set addedStops to true
                            if (addStop(linesToAdd.get(i), foundStopCodes.get(i), AddStopActivity.this) == 2) {
                                addedStops = true;
                            }
                        }

                        boolean finalAddedStops = addedStops;
                        runOnUiThread(() -> {
                            String text;

                            if (finalAddedStops) {
                                text = "Added stop/s to the list"; // If stops were added, mention that
                            }

                            else {
                                text = "Failed to add stop/s"; // Otherwise, say failed to add stops
                            }

                            Toast success = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                            success.show();
                        });
                    }

                    // If user is not connected to the internet, tell them to do so
                    catch (NoInternetException e) {
                        runOnUiThread(() -> {
                            Toast fail = Toast.makeText(AddStopActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                            fail.show();
                        });
                    }

                    // If the stop name is not valid
                    catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast fail = Toast.makeText(AddStopActivity.this, "Error: Cannot find stop " + stopName, Toast.LENGTH_SHORT);
                            fail.show();
                        });
                    }
                });

                thread.start();
            }

            // If nothing is given, tell user to enter a stop name or code
            else {
                Toast enterSomething = Toast.makeText(getApplicationContext(), "Please enter " +
                        "a stop name or a stop code", Toast.LENGTH_SHORT);
                enterSomething.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    /**
     * Navigates back to the main menu
     */
    private void goBack() {
        Intent returnMainMenu = new Intent(AddStopActivity.this, MainActivity.class);
        startActivity(returnMainMenu);
    }

    /**
     * Adds a stop to the list of clickable stops by writing it to a text file containing stop codes and their respective stop names
     * @param stopData the name and code of the stop to write
     * @param stopCode the stop code
     * @return 0 if failed to add that stop, 1 if the stop is already present in the file, and 2 if the stop was successfully added
     */
    public static int addStop(String stopData, String stopCode, Context context) {

        // Read the stops file
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput("stops.txt")));
            String line = "";

            while ((line = reader.readLine()) != null) {

                // If the line contains the given stop code, then the stop is already present in the file
                if (line.contains(stopCode)) {
                    return 1;
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        // Write to the stops file
        try {
            FileOutputStream fos = context.openFileOutput("stops.txt", MODE_APPEND);
            fos.write(stopData.getBytes()); // Append the stop name and code to the file
            return 2;
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
