package com.ragab.ahmed.educational.movieapp.data.db;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.ragab.ahmed.educational.movieapp.data.db.MovieContract.MovieEntry;
import com.ragab.ahmed.educational.movieapp.data.db.MovieContract.ReviewEntry;
import com.ragab.ahmed.educational.movieapp.data.db.MovieContract.VideoEntry;

/**
 * Created by Ragabov on 12/24/2015.
 */
public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int REVIEWS = 200;
    static final int REVIEWS_WITH_MOVIE_ID = 201;
    static final int VIDEOS = 300;
    static final int VIDEOS_WITH_MOVIE_ID = 301;
    static final int MOVIE_WITH_ID = 101;

    private static final SQLiteQueryBuilder sVideoByMovieIdQueryBuilder;
    private static final SQLiteQueryBuilder sReviewByMovieIdQueryBuilder;

    static{
        sVideoByMovieIdQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //video INNER JOIN movie ON video.movie_id = movie._id
        sVideoByMovieIdQueryBuilder.setTables(
                VideoEntry.TABLE_NAME + " INNER JOIN " +
                        MovieEntry.TABLE_NAME +
                        " ON " + VideoEntry.TABLE_NAME +
                        "." + VideoEntry.COLUMN_MOVIE_KEY +
                        " = " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry._ID);

        sReviewByMovieIdQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //review INNER JOIN movie ON review.movie_id = movie._id
        sReviewByMovieIdQueryBuilder.setTables(
                ReviewEntry.TABLE_NAME + " INNER JOIN " +
                        MovieEntry.TABLE_NAME +
                        " ON " + ReviewEntry.TABLE_NAME +
                        "." + ReviewEntry.COLUMN_MOVIE_KEY +
                        " = " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry._ID);
    }


    private static final String sMovieIdSelection =
            MovieEntry.TABLE_NAME+
                    "." + MovieEntry._ID + " = ? ";


    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {

        String movieId = MovieEntry.getIdFromUri(uri);

        String[] selectionArgs;
        String selection ;

        try{
            //Check if movieId is a valid long
            Long.parseLong(movieId);
            selection = sMovieIdSelection;
            selectionArgs =  new String[]{movieId};
        }
        catch (NumberFormatException e)
        {
            selection = null;
            selectionArgs = null;
        }

        return mOpenHelper.getReadableDatabase().query(
                MovieEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

    }

    private Cursor getVideosByMovieId(Uri uri, String[] projection, String sortOrder) {

        String movie_id = VideoEntry.getMovieIdFromUri(uri);
        return sVideoByMovieIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieIdSelection,
                new String[]{movie_id},
                null,
                null,
                sortOrder
        );
    }
    private Cursor getReviewsByMovieId(Uri uri, String[] projection, String sortOrder) {

        String movie_id = ReviewEntry.getMovieIdFromUri(uri);
        return sReviewByMovieIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieIdSelection,
                new String[]{movie_id},
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_VIDEO, VIDEOS);
        matcher.addURI(authority, MovieContract.PATH_VIDEO + "/#", VIDEOS_WITH_MOVIE_ID);
        matcher.addURI(authority, MovieContract.PATH_REVIEW , REVIEWS);
        matcher.addURI(authority, MovieContract.PATH_REVIEW + "/#" , REVIEWS_WITH_MOVIE_ID);
        return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.
     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case MOVIE_WITH_ID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case VIDEOS:
                return VideoEntry.CONTENT_TYPE;
            case REVIEWS:
                return ReviewEntry.CONTENT_TYPE;
            case MOVIE:
                return MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        Cursor retCursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                case MOVIE_WITH_ID:
                    retCursor = getMovieById(uri, projection, sortOrder);
                    break;

            case VIDEOS_WITH_MOVIE_ID:
                retCursor = getVideosByMovieId(uri, projection, sortOrder);
                break;

            case REVIEWS_WITH_MOVIE_ID:
                retCursor = getReviewsByMovieId(uri,projection,sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri =  MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case VIDEOS: {
                long _id = db.insert(VideoEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = VideoEntry.buildVideoUriWithMovieId(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS: {
                long _id = db.insert(ReviewEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ReviewEntry.buildReviewUriWithMovieId(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( selection == null ) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(
                        MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case VIDEOS:
                rowsDeleted = db.delete(
                        VideoEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case REVIEWS:
                rowsDeleted = db.delete(
                        ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case VIDEOS:
                rowsUpdated = db.update(VideoEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case REVIEWS:
                rowsUpdated = db.update(ReviewEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}