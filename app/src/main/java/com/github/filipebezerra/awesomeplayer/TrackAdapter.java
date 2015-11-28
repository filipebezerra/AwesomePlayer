package com.github.filipebezerra.awesomeplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * .
 *
 * @author Filipe Bezerra
 * @version #, 24/11/2015
 * @since #
 */
public class TrackAdapter extends BaseAdapter {
    @NonNull private final Context mContext;
    private final List<Track> mTrackList;

    public TrackAdapter(@NonNull Context context, List<Track> trackList) {
        mContext = context;
        mTrackList = trackList;
    }

    @Override
    public int getCount() {
        return mTrackList.size();
    }

    @Override
    public Track getItem(int position) {
        return mTrackList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Track track = getItem(position);

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.track_list_row, parent, false);
            holder = new ViewHolder();
            holder.trackImageView = (ImageView) convertView.findViewById(R.id.track_image);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.track_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.titleTextView.setText(track.getTitle());

        Picasso.with(mContext)
                .load(track.getArtworkUrl())
                .placeholder(R.drawable.ic_image_black)
                .error(R.drawable.ic_broken_image_black)
                .fit()
                .tag(track.getId())
                .into(holder.trackImageView);

        return convertView;
    }

    static class ViewHolder {
        ImageView trackImageView;
        TextView titleTextView;
    }
}
