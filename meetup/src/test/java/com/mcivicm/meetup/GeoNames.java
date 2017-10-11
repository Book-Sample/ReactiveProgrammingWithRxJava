package com.mcivicm.meetup;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by zhang on 2017/10/11.
 */

public interface GeoNames {

    @GET("/searchJSON")
    Observable<SearchResult> search(
            @Query("q") String query,
            @Query("maxRows") int maxRows,
            @Query("style") String style,
            @Query("username") String username);

}
