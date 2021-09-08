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

public class TFLAPIInteract {

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

                // If the stop code is null, do not add it, otherwise, do
                if (!(parts[1].equals("null") || parts[1].equals("NONE"))) {
                    stopCodes.add(parts[1]); // Add the stop code to the list
                }
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
            // Get the retrieved array
            String array = stopArray[1];
            // Remove the brackets in the reply
            String noBrackArray = array.substring(1, array.length() - 1);
            // Split on comma (but not on commas in quotation marks)
            String[] parts = noBrackArray.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            // Remove the quotation marks
            parts[1] = parts[1].replaceAll("\"", "");
            return parts[1]; // Return the stop name
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
     * Finds where the buses are going towards for a given stop code
     * @param stopCode the stop code
     * @param context the activity
     * @return Where the buses are going towards, or empty string if no such thing exists for the stop
     * @throws NoInternetException if there is no internet
     */
    public static String findTowards(String stopCode, Context context) throws NoInternetException {

        if (!isInternet(context)) {
            throw new NoInternetException("");
        }

        try {
            // Use a http get to retrieve the stop name for the stop code
            URL url = new URL("http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?" +
                    "StopCode1=" + stopCode + "&ReturnList=Towards");
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
            // Get the retrieved array
            String array = stopArray[1];
            // Remove the brackets in the reply
            String noBrackArray = array.substring(1, array.length() - 1);
            // Split on comma (but not on commas in quotation marks)
            String[] parts = noBrackArray.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            // Remove the quotation marks
            parts[1] = parts[1].replaceAll("\"", "");

            // If the towards is null, add return an empty string
            if (parts[1].equals("null") || parts[1].equals("NONE")) {
                return "";
            }

            return parts[1]; // Return the towards
        }

        catch (Exception e) {
            e.printStackTrace();
            return null;
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
     * Requests the names, codes and latlngs of every stop in the TFL bus service
     * @param context the activity
     * @return ArrayList of Stops
     * @throws Exception If there is no internet
     */
    public static ArrayList<Stop> getStopLatLng(Context context) throws NoInternetException {
        ArrayList<Stop> stops = new ArrayList<Stop>(); // Create a ArrayList to hold our stops

        if (!isInternet(context)) {
            throw new NoInternetException("Please ensure you are connected to the internet");
        }

        try {
            // Use a http get to stop names, towards, latitudes and longitudes
            URL url = new URL("http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?" +
                    "ReturnList=StopPointName,StopCode1,Towards,Latitude,Longitude");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream contents = new BufferedInputStream(urlConnection.getInputStream());
            InputStreamReader contentsReader = new InputStreamReader(contents);

            StringBuilder contentsBuilder = new StringBuilder();
            int status;

            while ((status = contentsReader.read()) != -1) {
                contentsBuilder.append((char) status);
            }

            // Now we parse the reply by splitting on each line
            String[] stopArrays = contentsBuilder.toString().split("\\r?\\n");

            for (int i = 1; i < stopArrays.length; i++) {

                // Get the array for a stop
                String array = stopArrays[i];
                // Remove brackets
                String noBrackArray = array.substring(1, array.length() - 1);
                // Split on comma (but not on commas in quotation marks)
                String[] parts = noBrackArray.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                // Remove all quotation marks for each stop part
                parts[1] = parts[1].replaceAll("\"", ""); // Stop Name
                parts[2] = parts[2].replaceAll("\"", ""); // Stop code
                parts[3] = parts[3].replaceAll("\"", ""); // Towards

                // If the stop code is not null, create a new Stop and add it to the list of all stops
                if (!(parts[2].equals("null") || parts[2].equals("NONE"))) {

                    // If towards is null, pass towards as an empty string
                    if (parts[3].equals("null") || parts[3].equals("NONE")) {
                        // parts[4] = latitude and parts[5] = longitude
                        stops.add(new Stop(parts[1], parts[2], "", Double.parseDouble(parts[4]), Double.parseDouble(parts[5])));
                    }

                    else {
                        stops.add(new Stop(parts[1], parts[2], parts[3], Double.parseDouble(parts[4]), Double.parseDouble(parts[5])));
                    }
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return stops;
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
