package com.ragab.ahmed.educational.movieapp.network;

import com.ragab.ahmed.educational.movieapp.data.models.FullResponse;
import com.ragab.ahmed.educational.movieapp.data.models.Movie;
import com.ragab.ahmed.educational.movieapp.data.models.ReviewsResponse;
import com.ragab.ahmed.educational.movieapp.data.models.TrailersResponse;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
/**
 * Created by Ragabov on 12/11/2015.
 */



public interface Api {

    @GET("/3/discover/movie?api_key=" + Constants.API_KEY)
    public Call<FullResponse> getMoviesBySortOrder(@Query("sort_by") String sort, @Query("page") int page);

    @GET("/3/movie/{id}?api_key=" + Constants.API_KEY)
    public Call<Movie> getMovieById(@Path("id") int id);

    @GET("/3/movie/{id}/videos?api_key=" + Constants.API_KEY)
    public Call<TrailersResponse> getTrailersByID(@Path("id") int id);

    @GET("/3/movie/{id}/reviews?api_key=" + Constants.API_KEY)
    public Call<ReviewsResponse> getReviewsByID(@Path("id") int id);

}

