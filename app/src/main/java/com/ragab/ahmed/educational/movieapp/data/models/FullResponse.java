package com.ragab.ahmed.educational.movieapp.data.models;

/**
 * Created by Ragabov on 12/11/2015.
 */
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class FullResponse {

    public Integer page;

    @SerializedName("results")
    public List<Movie> Movies = new ArrayList<Movie>();

    public Integer totalMovies;
    public Integer totalPages;


}