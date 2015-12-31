package com.ragab.ahmed.educational.movieapp.ui.detailscreen;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ragab.ahmed.educational.movieapp.R;
import com.ragab.ahmed.educational.movieapp.data.db.MovieContract;
import com.ragab.ahmed.educational.movieapp.data.models.Movie;
import com.ragab.ahmed.educational.movieapp.data.models.Review;
import com.ragab.ahmed.educational.movieapp.data.models.ReviewsResponse;
import com.ragab.ahmed.educational.movieapp.data.models.Trailer;
import com.ragab.ahmed.educational.movieapp.data.models.TrailersResponse;
import com.ragab.ahmed.educational.movieapp.network.Api;
import com.ragab.ahmed.educational.movieapp.network.Constants;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ImageButton.OnClickListener
{

    private static final String ARG_MOVIE_ID = "movie_id";

    private static Gson gson;
    private static Api mApi;

    private int movieId;
    private boolean fromDB = false;
    private int allLoadedFromDb = 0 ;
    private Movie movie;
    private View rootView ;
    private ImageButton imageButton;
    private ImageView imageView;
    private ListView trailersListView;
    private ListView reviewsListView;
    private ProgressDialog mProgressDialog;
    private ShareActionProvider mShareActionProvider;
    private Context globalContext;
    
    public static final int MOVIE_LOADER = 0;
    public static final int VIDEO_LOADER = 1;
    public static final int REVIEW_LOADER = 2;

    //Movie Columns and indices
    public static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_RUNTIME,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry._ID
    };

    static final int COL_MOVIE_TITLE = 0;
    static final int COL_MOVIE_RELEASE = 1;
    static final int COL_MOVIE_VOTE = 2;
    static final int COL_MOVIE_RUNTIME = 3;
    static final int COL_MOVIE_OVERVIEW = 4;
    static final int COL_MOVIE_POSTER = 5;
    static final int COL_MOVIE_ID = 6;

    //Review Columns and Indices
    public static final String[] REVIEW_COLUMNS = {
            MovieContract.ReviewEntry.COLUMN_AUTHOR,
            MovieContract.ReviewEntry.COLUMN_CONTENT,
    };

    static final int COL_REVIEW_AUTHOR = 0;
    static final int COL_REVIEW_CONTENT = 1;

    //Video Columns and Indices
    public static final String[] VIDEO_COLUMNS = {
            MovieContract.VideoEntry.COLUMN_NAME,
            MovieContract.VideoEntry.COLUMN_YOUTUBE_KEY
    };

    static final int COL_VIDEO_NAME = 0;
    static final int COL_VIDEO_KEY = 1;

    public static DetailFragment newInstance(int movieId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIE_ID, movieId);

        if (gson == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat(Movie.DATE_FORMAT);
            gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
            gson = gsonBuilder.create();
        }
        if (mApi == null) {
            final Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            mApi = retrofit.create(Api.class);
        }


        fragment.setArguments(args);
        return fragment;
    }

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        globalContext = getActivity().getApplicationContext();
        
        if (getArguments() != null) {
            movieId = getArguments().getInt(ARG_MOVIE_ID);
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.show();

            ContentResolver contentResolver = globalContext.getContentResolver();

            Cursor cursor = contentResolver.query(
                    MovieContract.MovieEntry.buildMovieUri(movieId),
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst())
            {
                //Movie in DB, get data from the DB
                fromDB = true;
                imageButton.setSelected(true);
                getLoaderManager().initLoader(MOVIE_LOADER, savedInstanceState, this);
                getLoaderManager().initLoader(REVIEW_LOADER, savedInstanceState, this);
                getLoaderManager().initLoader(VIDEO_LOADER, savedInstanceState, this);
            }
            else
            {
                fromDB = false;
                imageButton.setSelected(false);
                Call<Movie> getMovieCall = mApi.getMovieById(movieId);
                getMovieCall.enqueue(new Callback<Movie>() {
                    @Override
                    public void onResponse(Response<Movie> response, Retrofit retrofit) {
                        movie = response.body();
                        fillPrimary();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        mProgressDialog.dismiss();
                        Toast.makeText(
                                globalContext,
                                "Failed to load the movies, Check your internet connection.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

                Call<TrailersResponse> getTrailersCall = mApi.getTrailersByID(movieId);
                getTrailersCall.enqueue(new Callback<TrailersResponse>() {
                    @Override
                    public void onResponse(Response<TrailersResponse> response, Retrofit retrofit) {

                        trailersListView.setAdapter(
                                new TrailerListViewAdapter(globalContext, response.body().results));
                        setListViewHeightBasedOnChildren(trailersListView);
                        ((ScrollView)rootView).smoothScrollTo(0,0);

                        if (response.body().results.size() != 0) {
                            String firstTrailerKey = response.body().results.get(0).key;
                            setShareIntent(firstTrailerKey);
                        }
                        else
                            //To remove the previous share intent if it exists
                            setShareIntent("");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(
                                globalContext,
                                "Failed to load the movies, Check your internet connection.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

                Call<ReviewsResponse> getReviewsCall = mApi.getReviewsByID(movieId);
                getReviewsCall.enqueue(new Callback<ReviewsResponse>() {
                    @Override
                    public void onResponse(Response<ReviewsResponse> response, Retrofit retrofit) {

                        reviewsListView.setAdapter(
                                new ReviewListViewAdapter(globalContext.getApplicationContext(), response.body().results));
                        setListViewHeightBasedOnChildren(reviewsListView);

                        ((ScrollView)rootView).smoothScrollTo(0,0);
                        mProgressDialog.dismiss();
                        imageButton.setEnabled(true);
                        imageButton.setClickable(true);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(
                                globalContext,
                                "Failed to load the movies, Check your internet connection  and relaunch app",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater menuInflater)
    {
        super.onCreateOptionsMenu(menu, menuInflater);

        // Inflate menu resource file.
        menuInflater.inflate(R.menu.menu_detail, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

    }

    private void setShareIntent(String firstTrailerKey) {
        if (mShareActionProvider != null) {
            if (firstTrailerKey.equals(""))
            {
                mShareActionProvider.setShareIntent(null);
                Toast.makeText(globalContext,
                        "There is no trailer to share for this movie.", Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        Constants.YOUTUBE_VIDEO_BASE_URL + "?v=" + firstTrailerKey);
                sendIntent.setType("text/plain");
                mShareActionProvider.setShareIntent(sendIntent);
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_detail, container, false);

        imageView = (ImageView) rootView.findViewById(R.id.movie_poster_image);
        imageButton = (ImageButton) rootView.findViewById(R.id.favourite_button);
        imageButton.setEnabled(false);
        imageButton.setClickable(false);
        imageButton.setOnClickListener(this);

        trailersListView = (ListView) rootView.findViewById(R.id.movie_trailers_list_view);

        reviewsListView = (ListView) rootView.findViewById(R.id.movie_reviews_list_view);

        // Scrolling the Parent ScrollView up by one page when scrolling to the first element
        // Of the ListView, Because the parent scroll is no longer reachable from within the
        // ListView scroll
        reviewsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean atTop;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (listIsAtTop(reviewsListView) && scrollState == SCROLL_STATE_IDLE)
                {
                    ((ScrollView)view.getParent().getParent()).pageScroll(View.FOCUS_UP);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        reviewsListView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        return rootView;
    }

    public void fillPrimary ()
    {
        TextView movieTitle = (TextView) rootView.findViewById(R.id.movie_title_text);
        movieTitle.setText(movie.title);
        String uri = ((fromDB)?"":Constants.IMG_BASE_URL + Constants.IMG_MID_SIZE) + movie.posterPath;
        if (!uri.isEmpty()) {
            Picasso.with(globalContext)
                    .load(uri)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
        }
        //Date methods are deprecated, using Calendar instead to get Year
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(movie.releaseDate);

        TextView movieYear = (TextView) rootView.findViewById(R.id.movie_year_text);
        movieYear.setText(Integer.toString(calendar.get(Calendar.YEAR)));

        Resources mResources = getResources();

        TextView movieDuration = (TextView) rootView.findViewById(R.id.movie_duration_text);
        movieDuration.setText(String.format(mResources.getString(R.string.movie_runtime), movie.runtime));

        TextView movieRating = (TextView) rootView.findViewById(R.id.movie_rating_text);

        movieRating.setText(String.format(mResources.getString(R.string.movie_rating), movie.voteAverage));

        TextView movieOverview = (TextView) rootView.findViewById(R.id.movie_overview_text);
        movieOverview.setText(movie.overview);
    }
    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, AbsListView.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private boolean listIsAtTop(ListView listView)   {
        if(listView.getChildCount() == 0) return false;
        return listView.getChildAt(0).getTop() == 0;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id)
        {
            case MOVIE_LOADER:
                return new CursorLoader(globalContext,
                        MovieContract.MovieEntry.buildMovieUri(movieId),
                        MOVIE_COLUMNS,
                        null,
                        null,
                        null
                );

            case REVIEW_LOADER:
                return new CursorLoader(globalContext,
                        MovieContract.ReviewEntry.buildReviewUriWithMovieId(movieId),
                        REVIEW_COLUMNS,
                        null,
                        null,
                        null
                );

            case VIDEO_LOADER:
                return new CursorLoader(globalContext,
                        MovieContract.VideoEntry.buildVideoUriWithMovieId(movieId),
                        VIDEO_COLUMNS,
                        null,
                        null,
                        null
                );

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (loader != null)
        {
            if (!data.moveToFirst())
            {
                onLastLoaderFinished();
                return ;
            }
            switch (loader.getId())
            {
                case MOVIE_LOADER:
                    movie = new Movie();
                    movie.posterPath = data.getString(COL_MOVIE_POSTER);
                    movie.overview = data.getString(COL_MOVIE_OVERVIEW);
                    DateFormat df = new SimpleDateFormat(Movie.DATE_FORMAT);
                    try {
                        movie.releaseDate = df.parse(data.getString(COL_MOVIE_RELEASE));
                    }catch (ParseException e)
                    {}
                    movie.runtime = data.getInt(COL_MOVIE_RUNTIME);
                    movie.title = data.getString(COL_MOVIE_TITLE);
                    movie.voteAverage = data.getDouble(COL_MOVIE_VOTE);
                    movie.id = data.getInt(COL_MOVIE_ID);
                    fillPrimary();
                    break;

                case REVIEW_LOADER:
                    List<Review> reviewList = new ArrayList<Review>();
                    do {
                        Review review = new Review();
                        review.author = data.getString(COL_REVIEW_AUTHOR);
                        review.content = data.getString(COL_REVIEW_CONTENT);
                        reviewList.add(review);
                    } while (data.moveToNext());

                    reviewsListView.setAdapter( new ReviewListViewAdapter(globalContext,reviewList));
                    setListViewHeightBasedOnChildren(reviewsListView);
                    break;

                case VIDEO_LOADER:
                    List<Trailer> trailerList = new ArrayList<Trailer>();
                    do {
                        Trailer trailer = new Trailer();
                        trailer.name = data.getString(COL_VIDEO_NAME);
                        trailer.key = data.getString(COL_VIDEO_KEY);
                        trailerList.add(trailer);
                    } while (data.moveToNext());

                    trailersListView.setAdapter( new TrailerListViewAdapter(globalContext, trailerList));
                    setListViewHeightBasedOnChildren(trailersListView);
            }
            onLastLoaderFinished();
        }
    }

    private void onLastLoaderFinished()
    {
        //To check if all Loaders have been called
        if (allLoadedFromDb++ == 2)
        {
            allLoadedFromDb = 0;
            mProgressDialog.dismiss();
            ((ScrollView)rootView).smoothScrollTo(0,0);

            ListAdapter trailerListViewAdapter = trailersListView.getAdapter();
            if(trailerListViewAdapter != null && trailerListViewAdapter.getCount() != 0) {
                Trailer firstTrailer = (Trailer) trailerListViewAdapter.getItem(0);
                setShareIntent(firstTrailer.key);
            }
            else
                //To remove the previous share intent if it exists
                setShareIntent("");

            //Enable clicking on the Favorite button
            imageButton.setEnabled(true);
            imageButton.setClickable(true);
        }
    }
    private String saveToInternalSorage(Bitmap bitmapImage, String name) throws IOException {
        ContextWrapper cw = new ContextWrapper(globalContext.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory, name);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
            return  "";
        }
        fos.close();
        //adding file prefix for picasso to load images from file system
        return "file:"+mypath.getAbsolutePath();
    }
    private boolean deleteFromInternalStorage(String filePath) throws IOException {
        ContextWrapper cw = new ContextWrapper(globalContext.getApplicationContext());

        File mypath = new File (filePath);
        return mypath.delete();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {

        v.setSelected(!v.isSelected());
        ContentResolver contentResolver = globalContext.getContentResolver();
        if (movie == null) return;

        if (v.isSelected()) {
            ContentValues contentValues = new ContentValues();

            SimpleDateFormat df = new SimpleDateFormat(Movie.DATE_FORMAT);

            String imagePath = null;
            try {
                if (movie.posterPath != null) {
                    imagePath = saveToInternalSorage(
                            ((BitmapDrawable) imageView.getDrawable()).getBitmap(),
                            movie.posterPath
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            contentValues.put(MovieContract.MovieEntry._ID, movie.id);
            contentValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.overview);
            contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, imagePath);
            contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, df.format(movie.releaseDate));
            contentValues.put(MovieContract.MovieEntry.COLUMN_RUNTIME, movie.runtime);
            contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.title);
            contentValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.voteAverage);


            contentResolver.insert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    contentValues
            );

            ListAdapter trailerListAdapter = trailersListView.getAdapter();
            ListAdapter reviewListAdapter = reviewsListView.getAdapter();

            long trailerCount = (trailerListAdapter == null ? 0 : trailerListAdapter.getCount());
            long reviewCount = (reviewListAdapter == null ? 0 : reviewListAdapter.getCount());

            for (int i = 0; i < trailerCount; i++) {
                contentValues.clear();
                Trailer trailer = (Trailer) trailerListAdapter.getItem(i);
                contentValues.put(MovieContract.VideoEntry.COLUMN_MOVIE_KEY, movie.id);
                contentValues.put(MovieContract.VideoEntry.COLUMN_YOUTUBE_KEY, trailer.key);
                contentValues.put(MovieContract.VideoEntry.COLUMN_NAME, trailer.name);

                contentResolver.insert(
                        MovieContract.VideoEntry.CONTENT_URI,
                        contentValues
                );
            }

            for (int i = 0; i < reviewCount; i++) {
                contentValues.clear();
                Review review = (Review) reviewListAdapter.getItem(i);
                contentValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, movie.id);
                contentValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review.author);
                contentValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, review.content);

                contentResolver.insert(
                        MovieContract.ReviewEntry.CONTENT_URI,
                        contentValues
                );
            }
            Toast.makeText(globalContext, "Added to favorites", Toast.LENGTH_SHORT)
                    .show();
        } else {

            String sMovieIdSelection =
                    MovieContract.MovieEntry.TABLE_NAME +
                            "." + MovieContract.MovieEntry._ID + " = ? ";
            String[] selectionArgs = new String[]{movie.id.toString()};

            try {
                //Removing the file prefix from the path
                if (movie.posterPath != null && movie.posterPath.length() > "file:".length())
                  deleteFromInternalStorage(movie.posterPath.substring("file:".length()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            contentResolver.delete(
                    MovieContract.MovieEntry.CONTENT_URI,
                    sMovieIdSelection,
                    selectionArgs
            );

            String sReviewMovieKeySelection =
                    MovieContract.ReviewEntry.TABLE_NAME +
                            "." + MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + " = ? ";
            String sVideoMovieKeySelection =
                    MovieContract.VideoEntry.TABLE_NAME +
                            "." + MovieContract.VideoEntry.COLUMN_MOVIE_KEY + " = ? ";

            contentResolver.delete(
                    MovieContract.ReviewEntry.CONTENT_URI,
                    sReviewMovieKeySelection,
                    selectionArgs
            );

            contentResolver.delete(
                    MovieContract.VideoEntry.CONTENT_URI,
                    sVideoMovieKeySelection,
                    selectionArgs
            );

            Toast.makeText(globalContext, "Removed from favorites", Toast.LENGTH_SHORT)
                    .show();

        }
    }
}
