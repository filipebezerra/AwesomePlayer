package com.github.filipebezerra.awesomeplayer;

import com.google.gson.annotations.SerializedName;

/**
 * .
 *
 * @author Filipe Bezerra
 * @version #, 24/11/2015
 * @since #
 */
public class Track {
    private String title;

    private int id;

    @SerializedName("stream_url")
    private String streamUrl;

    @SerializedName("artwork_url")
    private String artworkUrl;

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }
}
