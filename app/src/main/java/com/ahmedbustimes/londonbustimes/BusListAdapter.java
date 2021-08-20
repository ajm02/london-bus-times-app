package com.ahmedbustimes.londonbustimes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BusListAdapter extends ArrayAdapter<BusCardDetails> {

    /**
     * BusListAdapter is a custom adapter. It is the adapter for the listview on the BusesActivity.
     */

    private Context context;
    int resource;
    private static class ViewHolder {
        TextView lineID;
        TextView destinationText;
        TextView time;
    }

    /**
     * Create a new BusListAdapter
     * @param context the activity
     * @param resource id for the layout resource of a single list item
     * @param buses the data to adapt
     */
    public BusListAdapter(Context context, int resource, ArrayList<BusCardDetails> buses) {
        super(context, resource, buses);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String lineID = getItem(position).getLineID(); // Get the line
        String destinationText = getItem(position).getDestinationText(); // Get the destination
        String time = getItem(position).getArrivalTime().toString(); // Get the arrival time
        ViewHolder holder; // Holder class to hold the views

        if (convertView == null) {
            // Display the resource
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);

            // Create a new view holder and have it hold the views
            holder = new BusListAdapter.ViewHolder();
            holder.lineID = (TextView) convertView.findViewById(R.id.line);
            holder.destinationText = (TextView) convertView.findViewById(R.id.destination);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(holder);
        }

        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lineID.setText(lineID); // Set the lineID textview to the lineID
        holder.destinationText.setText(destinationText); // Set the destination text textview to the destination
        String displayTime = "";

        // Find the correct text to display for the arrival time
        if (time.equals("0")) {
            displayTime = "Due";
        }

        else if (time.equals("1")) {
            displayTime = time + " min";
        }

        else {
            displayTime = time + " mins";
        }

        holder.time.setText(displayTime); // Set the arrival time textview to the arrival time
        return convertView;
    }
}
