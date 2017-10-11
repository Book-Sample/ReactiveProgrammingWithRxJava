package com.mcivicm.meetup;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by zhang on 2017/10/11.
 */

public interface MeetupApi {

    @GET("/2/cities")
    Observable<Cities> listCities(
            @Query("lat") double lat,
            @Query("lon") double lon);


}
