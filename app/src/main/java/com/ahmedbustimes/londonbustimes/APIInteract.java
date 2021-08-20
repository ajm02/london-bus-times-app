package com.ahmedbustimes.londonbustimes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.apache.commons.text.WordUtils;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class APIInteract {

    /**
     * APIInteract is a class responsible for interacting with the tfl countdown API. It provides some static methods to interact with the API. It also contains a
     * method to check if the user is connected to the internet
     */

    /**
     * Finds the stop codes for the stops with the given name
     * @param stopName name of the stop
     * @param context the activity
     * @return arraylist of the stop codes
     * @throws NoInternetException if user is not connected to the internet
     * @throws Exception if the given stop name is not real
     */
    public static ArrayList<String> findStopCodes(String stopName, Context context) throws Exception {
        String preppedStopName = WordUtils.capitalizeFully(stopName).replaceAll(" ", "%20"); // First capitalize the start of each word and add %20 in the spaces to cover whitespace
        ArrayList<String> stopCodes = new ArrayList<String>();

        // If there is no internet, throw a NoInternetException
        if (!isInternet(context)) {
            throw new NoInternetException("Please ensure you are connected to the internet");
        }

        try {
            // Use a http get to retrieve the stop codes for the stop name
            URL url = new URL("http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?" +
                    "StopPointName=" + preppedStopName + "&ReturnList=StopCode1");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream contents = new BufferedInputStream(urlConnection.getInputStream());
            InputStreamReader contentsReader = new InputStreamReader(contents);

            StringBuilder contentsBuilder = new StringBuilder();
            int status;

            while ((status = contentsReader.read()) != -1) {
                contentsBuilder.append((char) status);
            }

            // Now we parse the reply, first by splitting on each new line
            String[] stopArrays = contentsBuilder.toString().split("\\r?\\n");

            // For each line remove all quotation marks, the open and close square brackets, then split on comma to get two parts: array type and stop code
            for (int i = 1; i < stopArrays.length; i++) {

                String array = stopArrays[i];
                String replacedQuotes = array.replaceAll("\"", "");
                String[] parts = replacedQuotes.substring(1, replacedQuotes.length() - 1).split(",");
                stopCodes.add(parts[1]); // Add the stop code to the list
            }

            return stopCodes;
        }

        catch (FileNotFoundException e) {
            throw new FileNotFoundException(e.getMessage());
        }

        catch (MalformedURLException e) {
            throw new MalformedURLException(e.getMessage());
        }

        catch (IOException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Finds the stop name for a given stop code
     * @param stopCode the stop code
     * @param context the activity
     * @return stop name
     * @throws NoInternetException if user is not connected to the internet
     * @throws Exception if the given stop code is not real
     */
    public static String findStopName(String stopCode, Context context) throws Exception {

        // If there is no internet, throw a NoInternetException
        if (!isInternet(context)) {
            throw new NoInternetException("Please ensure you are connected to the internet");
        }

        try {
            // Use a http get to retrieve the stop name for the stop code
            URL url = new URL("http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?" +
                    "StopCode1=" + stopCode + "&ReturnList=StopPointName");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream contents = new BufferedInputStream(urlConnection.getInputStream());
            InputStreamReader contentsReader = new InputStreamReader(contents);

            StringBuilder contentsBuilder = new StringBuilder();
            int status;

            while ((status = contentsReader.read()) != -1) {
                contentsBuilder.append((char) status);
            }

            // Now we parse the reply, first by splitting on each new line
            String[] stopArray = contentsBuilder.toString().split("\\r?\\n");
            // Get the retrieved array, remove quotation marks, open and close brackets and split on the comma to get two parts: array type and stop name
            String array = stopArray[1];
            String replacedQuotes = array.replaceAll("\"", "");
            String[] parts = replacedQuotes.substring(1, replacedQuotes.length() - 1).split(",");
            return parts[1]; // return the stop name
        }

        catch (FileNotFoundException e) {
            throw new FileNotFoundException(e.getMessage());
        }

        catch (MalformedURLException e) {
            throw new MalformedURLException(e.getMessage());
        }

        catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Gives you a list of all the incoming buses, where they are going to, and the time left until arrival for a given stop.
     * @param stopCode stop code
     * @return arraylist of BusCardDetails objects
     * @throws Exception if there was an error attempting to retrieve the information
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static ArrayList<BusCardDetails> getTimes(String stopCode) throws Exception {
        ArrayList<BusCardDetails> times = new ArrayList<BusCardDetails>();

        try {
            // Use a http get to retrieve the lines, destinations and estimated arrival times
            URL url = new URL("http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?" +
                    "StopCode1=" + stopCode + "&ReturnList=LineID,DestinationText,EstimatedTime,ExpireTime");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream contents = new BufferedInputStream(urlConnection.getInputStream());
            InputStreamReader contentsReader = new InputStreamReader(contents);

            StringBuilder contentsBuilder = new StringBuilder();
            int status;

            while ((status = contentsReader.read()) != -1) {
                contentsBuilder.append((char) status);
            }

            // Now we parse the reply, first by splitting on each new line
            String[] predArrays = contentsBuilder.toString().split("\\r?\\n");

            // For each line remove all quotation marks, the open and close square brackets, then split on comma
            for (int i = 1; i < predArrays.length; i++) {

                String array = predArrays[i];
                String replacedQuotes = array.replaceAll("\"", "");
                String[] parts = replacedQuotes.substring(1, replacedQuotes.length() - 1).split(","); // Leaves us with a parts array

                String line = parts[1]; // The second element is the bus lineID
                String destination = parts[2]; // The third element is the bus destination
                long epochArriveTime = Long.parseLong(parts[3]) / 1000; // The fourth element is the arrival time, dividing by 1000 gives us the time in UTC epoch

                // Convert the time from UTC epoch to LocalDateTime for London
                ZonedDateTime UTCArriveTime = LocalDateTime.ofEpochSecond(epochArriveTime, 0, ZoneOffset.UTC).atZone(ZoneOffset.UTC);
                LocalDateTime arriveTime = UTCArriveTime.withZoneSameInstant(ZoneId.of("Europe/London")).toLocalDateTime();

                // Work out the difference in minutes between now and the arrival time of the bus
                int minutes = (int) Duration.between(LocalDateTime.now(), arriveTime).toMinutes();

                times.add(new BusCardDetails(line, destination, minutes));
            }

            // Sort the list of arriving buses in order of arrival time
            Collections.sort(times, new Comparator<BusCardDetails>() {
                @Override
                public int compare(BusCardDetails busCardDetails, BusCardDetails t1) {
                    return busCardDetails.getArrivalTime() - t1.getArrivalTime();
                }
            });

            return times;
        }

        catch (Exception e) {
            throw new Exception("Error attempting to retrieve bus times");
        }
    }

    /**
     * Checks if there is internet connection on this device
     * @param context current activity
     * @return true if connected, false if not
     */
    public static boolean isInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
