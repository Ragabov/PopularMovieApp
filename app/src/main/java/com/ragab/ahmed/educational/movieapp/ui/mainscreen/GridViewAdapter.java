package com.ragab.ahmed.educational.movieapp.ui.mainscreen;

/**
 * Created by Ragabov on 12/10/2015.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.ragab.ahmed.educational.movieapp.R;
import com.ragab.ahmed.educational.movieapp.data.models.Movie;
import com.ragab.ahmed.educational.movieapp.network.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

final class GridViewAdapter extends ArrayAdapter<Movie> {

    LayoutInflater minflater;
    public boolean loadingFromInternalStorage;

    public GridViewAdapter(Context context, List<Movie> movies, boolean loadingFromInternalStorage) {
        super(context, R.layout.movie_list_item, movies);
        minflater = LayoutInflater.from(getContext());
        this.loadingFromInternalStorage = loadingFromInternalStorage;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView =  minflater.inflate(R.layout.movie_list_item, null);
        }
        // Get the image URL for the current position.
        String url = ((loadingFromInternalStorage)? "" : Constants.IMG_BASE_URL +Constants.IMG_MID_SIZE)
                + getItem(position).posterPath;

        if (!url.isEmpty())
            // Trigger the download of the URL asynchronously into the image view.
            Picasso.with(getContext()) //
                    .load(url)//
                    .placeholder(R.drawable.placeholder)
                    .fit()
                    .into((ImageView) convertView);

        return convertView;
    }
}