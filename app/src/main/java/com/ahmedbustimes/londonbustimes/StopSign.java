package com.ahmedbustimes.londonbustimes;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import net.sharewire.googlemapsclustering.ClusterItem;

public class StopSign implements ClusterItem {

    /**
     * A StopSign is a class which extends ClusterItem (marker). It has a latlng as its position, the stop name as its title
     * and the stop towards as its snippet
     *
     */

    private final LatLng position;
    private final String title;
    private final String snippet;

    /**
     * Create a new StopSign
     * @param latLng latitude and longitude of this stop marker
     * @param title name of this stop marker
     * @param snippet where the buses are going towards for this stop marker
     */
    public StopSign(LatLng latLng, String title, String snippet) {
        this.position = latLng;
        this.title = title;
        this.snippet = snippet;
    }

    @Override
    public double getLatitude() {
        return position.latitude;
    }

    @Override
    public double getLongitude() {
        return position.longitude;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }
}
