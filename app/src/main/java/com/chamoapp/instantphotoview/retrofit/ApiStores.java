package com.chamoapp.instantphotoview.retrofit;

import com.chamoapp.instantphotoview.BuildConfig;
import com.chamoapp.instantphotoview.data.ResultData;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Koo on 2016. 11. 1..
 */

public interface ApiStores {
    String SERVER_URL = "https://api.flickr.com/services/rest/";

    @GET("?method=flickr.interestingness.getList&format=json&nojsoncallback=1&api_key=" + BuildConfig.FLICKR_API_KEY)
    Observable<ResultData> loadRecentPhotos(@Query("page") int page);

    @GET("?method=flickr.people.getInfo&format=json&nojsoncallback=1&api_key=" + BuildConfig.FLICKR_API_KEY)
    Observable<ResultData> loadUserInfo(@Query("user_id") String userId);

}
