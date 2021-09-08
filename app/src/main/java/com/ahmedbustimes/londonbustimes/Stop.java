package com.ahmedbustimes.londonbustimes;

public class Stop {

    /**
     * An instance of this class holds the stop name, stop code, towards and
     * (Optionally) latitude and longitude for a particular stop.
     */

    private String stopName;
    private String stopCode;
    private String towards;
    private double latitude;
    private double longitude;

    /**
     * Create a new Stop without latitude and longitude
     * @param stopName name of the stop
     * @param stopCode code of the stop
     * @param towards where the buses are going towards
     */
    public Stop(String stopName, String stopCode, String towards) {
        this.stopName = stopName;
        this.stopCode = stopCode;
        this.towards = towards;
    }

    /**
     * Create a new stop with latitude and longitude
     * @param stopName name of the stop
     * @param stopCode code of the stop
     * @param towards where the buses are going towards
     * @param latitude latitude of the stop
     * @param longitude longitude of the stop
     */
    public Stop(String stopName, String stopCode, String towards, double latitude, double longitude) {
        this.stopName = stopName;
        this.stopCode = stopCode;
        this.towards = towards;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStopName() {
        return stopName;
    }

    public String getStopCode() {
        return stopCode;
    }

    public String getTowards() {
        return towards;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

}
