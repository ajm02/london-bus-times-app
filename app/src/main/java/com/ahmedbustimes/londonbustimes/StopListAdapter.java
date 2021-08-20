package com.ahmedbustimes.londonbustimes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class StopListAdapter extends ArrayAdapter<Stop> {

    /**
     * StopListAdapter is a custom adapter. It is an adapter for the listview on the MainActivity
     */

    private Context context;
    int resource;
    private static class ViewHolder {
        TextView stopNameDisplay;
        TextView stopCodeDisplay;
        ImageView deleteImage;
    }

    /**
     * Create a new StopListAdapter
     * @param context the activity
     * @param resource id for the layout resource for a single list item
     * @param stops the data to adapt
     */
    public StopListAdapter(Context context, int resource, ArrayList<Stop> stops) {
        super(context, resource, stops);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String stopName = getItem(position).getStopName(); // Get the name of the stop
        String stopCode = "Stop Code: " + getItem(position).getStopCode(); // Get the code of the stop
        ViewHolder holder; // Holder class to hold the views

        if (convertView == null) {
            // Display the list item
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);

            // Create a ViewHolder to hold the views
            holder = new ViewHolder();
            holder.stopNameDisplay = (TextView) convertView.findViewById(R.id.stop_name);
            holder.stopCodeDisplay = (TextView) convertView.findViewById(R.id.stop_code);
            holder.deleteImage = (ImageView) convertView.findViewById(R.id.deleteImage);
            convertView.setTag(holder);
        }

        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.stopNameDisplay.setText(stopName); // Set the stopNameDisplay to stopName
        holder.stopCodeDisplay.setText(stopCode); // Set the stopCodeDisplay to stopCode
        // Add an onClick listener to the delete image to remove the stop
        holder.deleteImage.setOnClickListener((view) -> {
            ((MainActivity) context).removeStop(getItem(position).getStopCode()); // Remove stop
            ((MainActivity) context).loadStopData(); // Reload the stop data
            ((MainActivity) context).refreshList(); // Refresh the stop list
        });

        return convertView;
    }
}
