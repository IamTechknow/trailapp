package com.ucschackathon.app.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ucschackathon.app.Marker;
import com.ucschackathon.app.R;
import com.ucschackathon.app.TrailActivity;

import java.util.ArrayList;

/**
 * Custom adapter that connects Marker data to the list item view in RecyclerView
 * Marker data is obtained from the local database
 * A custom ViewHolder is defined that wraps the icon and textview for the data
 */

public class MarkerAdapter extends RecyclerView.Adapter<MarkerAdapter.ViewHolder>{
    private ArrayList<Marker> mItems;
    private Resources mResources;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView subtitle;

        public ViewHolder(View itemView) {
            super(itemView);

            icon = (ImageView) itemView.findViewById(R.id.icon);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle);
        }
    }

    public MarkerAdapter(ArrayList<Marker> items, Resources r) {
        super();

        //get marker data and icons
        mItems = items;
        mResources = r;
    }

    @Override
    public MarkerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ViewHolder(v);
    }

    //Set the icon and title for each list item
    @Override
    public void onBindViewHolder(MarkerAdapter.ViewHolder holder, int position) {
        final Marker m = mItems.get(position);
        int resourceID = TrailActivity.getMarkerIconID(m);

        //use deprecated function to make our lives easier
        holder.icon.setImageDrawable(mResources.getDrawable(resourceID));
        holder.subtitle.setText(m.getTitle());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}