package com.ahmedbustimes.londonbustimes;

public class Stop {

    /**
     * An instance of this class holds the stop name and stop code for a particular stop.
     */

    private String stopName;
    private String stopCode;

    /**
     * Create a new Stop
     * @param stopName name of the stop
     * @param stopCode code of the stop
     */
    public Stop(String stopName, String stopCode) {
        this.stopName = stopName;
        this.stopCode = stopCode;
    }

    public String getStopName() {
        return stopName;
    }

    public String getStopCode() {
        return stopCode;
    }

}
