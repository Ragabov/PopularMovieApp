package com.ragab.ahmed.educational.movieapp.data.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ragabov on 12/18/2015.
 */
public class ReviewsResponse {

    public Integer id;
    public Integer page;
    public List<Review> results = new ArrayList<Review>();
    public Integer totalPages;
    public Integer totalResults;

}
