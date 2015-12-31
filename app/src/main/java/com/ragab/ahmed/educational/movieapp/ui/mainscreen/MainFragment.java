package com.ragab.ahmed.educational.movieapp.ui.mainscreen;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.ragab.ahmed.educational.movieapp.R;
import com.ragab.ahmed.educational.movieapp.data.db.MovieContract;
import com.ragab.ahmed.educational.movieapp.data.models.FullResponse;
import com.ragab.ahmed.educational.movieapp.data.models.Movie;
import com.ragab.ahmed.educational.movieapp.network.Api;
import com.ragab.ahmed.educational.movieapp.network.Constants;
import com.ragab.ahmed.educational.movieapp.ui.MainActivity;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Ragabov on 12/10/2015.
 */
public class MainFragment extends Fragment implements Callback<FullResponse>,
        GridView.OnScrollListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    private View rootView;
    private GridView mgridView;
    private int currentPage , totalPages , pageLength ;
    private Context globalContext;
    private boolean mutex = true, isShowingFavourites = false;
    private Api mApi;
    ProgressDialog mProgressDialog;
    private String sortOrder;

    public static final int MOVIES_LOADER = 0;

    //Movie Columns and indices
    public static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
    };

    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_POSTER = 1;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        globalContext = getActivity().getApplicationContext();
        currentPage = 0; totalPages = 0; pageLength =0;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat(Movie.DATE_FORMAT);
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);

        //Register a typeAdapter to the DATE class to handle empty date string in JSON
        gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            DateFormat df = new SimpleDateFormat(Movie.DATE_FORMAT);
            @Override
            public Date deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
                    throws JsonParseException {
                try {
                    return df.parse(json.getAsString());
                } catch (ParseException e) {
                    return null;
                }
            }
        });
        Gson customGson = gsonBuilder.create();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(customGson))
                .build();

        mApi = retrofit.create(Api.class);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.progress_loading));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView != null)
            return rootView;

        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mgridView = (GridView) rootView.findViewById(R.id.movie_grid_view);
        mgridView.setOnScrollListener(this);

        //Set onItemClickListener to the Activity, Activity containing the fragment must implement
        //On Item Click Listener for the gridview

        mgridView.setOnItemClickListener((MainActivity) getActivity());


        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(globalContext);

        String tempSortOrder = sharedPreferences.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));

        if (tempSortOrder.equals(sortOrder)) {

            GridViewAdapter gridViewAdapter = (GridViewAdapter) mgridView.getAdapter();
            if (gridViewAdapter != null)
                gridViewAdapter.clear();

            //Reseting page number so that first request get first page AND
            //Scrolling the gridview doesn't request a new page
            currentPage = 0;
            sortOrder = tempSortOrder;

            String favorite = getString(R.string.pref_sort_favorite);
            if (sortOrder.equals(favorite))
            {
                mProgressDialog.show();
                isShowingFavourites = true;
                getLoaderManager().initLoader(MOVIES_LOADER, null, this);
            }
            else {
                isShowingFavourites = false;
                mProgressDialog.show();
                Call<FullResponse> call = mApi.getMoviesBySortOrder(sortOrder, 1);
                call.enqueue(this);
            }
        }

    }
    @Override
    public void onResponse(retrofit.Response<FullResponse> response, Retrofit retrofit) {

        mProgressDialog.dismiss();
        if (currentPage == 0) {
            currentPage = response.body().page;
            totalPages = response.body().totalPages;
            pageLength = response.body().Movies.size();
            mgridView.setAdapter(
                    new GridViewAdapter(getActivity(), response.body().Movies, false)
            );
        }
        else {
            currentPage = response.body().page;
            List<Movie> movies = response.body().Movies;
            int length = movies.size();
            ArrayAdapter<Movie> madapter = (ArrayAdapter<Movie>)mgridView.getAdapter();
/*            for (int i = 0; i < length; i++)
                madapter.add(movies.get(i));*/ // API 10 or lower
            madapter.addAll(movies);

            //Enable OnScrollListener to execute another request for the next page of movies
            mutex = true;
        }
    }

    @Override
    public void onFailure(Throwable t) {
        mProgressDialog.dismiss();
        Toast.makeText(
                getActivity(),
                getString(R.string.failed_loading),
                Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //First response not received yet
        if (currentPage == 0 || isShowingFavourites) return ;


        if (mutex & currentPage != totalPages && visibleItemCount+firstVisibleItem == totalItemCount)
        {
            Call<FullResponse> call = mApi.getMoviesBySortOrder(sortOrder, currentPage + 1);

            //Disable further executions of this if condition until the current request is fulfilled
            mutex = false;
            call.enqueue(this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                MOVIE_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Don't call add to Adapter unless showing the favorite list based on settings
        String favorite = getString(R.string.pref_sort_favorite);
        if(loader != null && sortOrder.equals(favorite))
        {
            List<Movie> movieList = new ArrayList<Movie>();

            if (data.moveToFirst())
            {
                do {
                    Movie movie = new Movie();
                    movie.id = data.getInt(COL_MOVIE_ID);
                    movie.posterPath = data.getString(COL_MOVIE_POSTER);
                    movieList.add(movie);
                }while (data.moveToNext());

                GridViewAdapter madapter = (GridViewAdapter)mgridView.getAdapter();

                if (madapter == null){
                    madapter = new GridViewAdapter(getActivity().getApplicationContext(),
                            movieList, true);
                    mgridView.setAdapter(madapter);
                }
                else {
                     madapter.clear();
                     madapter.loadingFromInternalStorage = true;
                     madapter.addAll(movieList);
                }
            }
            else
            {
                //Clear adapter if there is no movie in the FAV DB
                GridViewAdapter madapter = (GridViewAdapter)mgridView.getAdapter();
                if (madapter != null)
                    madapter.clear();
            }
        }
        mProgressDialog.dismiss();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
