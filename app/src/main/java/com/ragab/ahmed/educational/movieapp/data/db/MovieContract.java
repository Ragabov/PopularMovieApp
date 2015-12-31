package com.ragab.ahmed.educational.movieapp.data.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Ragabov on 12/19/2015.
 */
public class MovieContract {


    public static final String CONTENT_AUTHORITY = "com.ragab.ahmed.educational.movieapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "weather";
    public static final String PATH_VIDEO= "video";
    public static final String PATH_REVIEW= "review";


    public static final class VideoEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEO).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;

        public static Uri buildVideoUriWithMovieId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }


        public static final String TABLE_NAME = "video";

        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_YOUTUBE_KEY = "key";

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }
    }

    public static final class ReviewEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static Uri buildReviewUriWithMovieId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

        public static final String TABLE_NAME = "review";

        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final String COLUMN_AUTHOR= "author";

        public static final String COLUMN_CONTENT = "content";

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

    }

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_POSTER_PATH = "poster_path";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_RUNTIME = "runtime";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_VOTE_AVERAGE= "vote_average";

        public static String getIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

    }
}
