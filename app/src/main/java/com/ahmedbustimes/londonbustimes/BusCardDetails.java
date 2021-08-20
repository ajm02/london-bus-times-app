package com.ahmedbustimes.londonbustimes;

public class BusCardDetails {

    /**
     * An instance of this class holds the line ID, destination and the arrival time for a bus arriving at a particular stop
     */

    private String lineID;
    private String destinationText;
    private Integer arrivalTime;

    /**
     * Create a new BusCardDetails
     * @param lineID line
     * @param destinationText destination
     * @param arrivalTime time in minutes until arrival
     */
    public BusCardDetails(String lineID, String destinationText, int arrivalTime) {
        this.lineID = lineID;
        this.destinationText = destinationText;
        this.arrivalTime = arrivalTime;
    }

    public String getLineID() {
        return lineID;
    }

    public String getDestinationText() {
        return destinationText;
    }

    public Integer getArrivalTime() {
        return arrivalTime;
    }
}
