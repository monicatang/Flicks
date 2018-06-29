package me.monicatang.flicks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import me.monicatang.flicks.models.Movie;

public class MovieListActivity extends AppCompatActivity {

    //base url for API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";

    //parameter name for API key
    public final static String API_KEY_PARAM = "api_key";

    //tag for logging from this activity
    public final static String TAG = "MovieListActivity";


    String imageBaseUrl;
    //poster size to use when fetching images, part of url
    String posterSize;
    //the list of currently playing movies
    ArrayList<Movie> movies;



    //instance fields
    AsyncHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        //Initialize the client
        client = new AsyncHttpClient();
        movies = new ArrayList<>();
        //Get configuration
        getConfiguration();


    }

    //get the list of currently playing movies from API
    private void getNowPlaying() {
        String url = API_BASE_URL + "/movie/now_playing";
        //set request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));
        client.get(url, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //load the results into movies list
                try {
                    JSONArray results = response.getJSONArray("results");
                    //iterate through result set and create Movie objects
                    for (int i = 0; i < results.length(); i++){
                        Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);
                    }
                    Log.i(TAG, String.format("Loaded %s movies", results.length()));
                    //get the now playing movie list
                    getNowPlaying();
                } catch (JSONException e) {
                    logError("Failed to parse now playing movies", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from now playing endpoint", throwable, true);
            }
        });
    }

    //get configuration from the API
    private void getConfiguration() {
        String url = API_BASE_URL + "/configuration";
        //set request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));
        //execute GET request expecting JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject images = response.getJSONObject("images");

                    //get image base url
                    imageBaseUrl = images.getString("secure_base_url");
                    //get poster size
                    JSONArray posterSizeOptions = images.getJSONArray("poster_sizes");
                    //use the option at index 3 or w342 as a fallback
                    posterSize = posterSizeOptions.optString(3, "w342");
                    Log.i(TAG, String.format("Loaded configuration with imageBaseUrl %s and posterSize %s", imageBaseUrl, posterSize));
                } catch (JSONException e) {
                    logError("Failed parsing configuration", e, true);
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed getting configuration", throwable, true);
            }
        });

    }

    //handle errors
    private void logError(String message, Throwable error, boolean alertUser){
        //always log the error
        Log.e(TAG, message, error);
        //alert the user to avoid silent errors
        if(alertUser){
            //show a long toast with the error message
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

        }
    }

}
