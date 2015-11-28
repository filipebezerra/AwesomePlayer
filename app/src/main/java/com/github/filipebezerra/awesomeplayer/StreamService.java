package com.github.filipebezerra.awesomeplayer;

import java.util.List;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * .
 *
 * @author Filipe Bezerra
 * @version #, 24/11/2015
 * @since #
 */
public interface StreamService {
    @GET("tracks?client_id=" + Config.CLIENT_ID)
    Observable<List<Track>> getRecentTracks(@Query("created_at[from]") String date);
}
