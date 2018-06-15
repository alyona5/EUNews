package com.example.android.eunews;

import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.net.URL;
import java.util.List;
import java.util.SimpleTimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Helper methods related to requesting and receiving news data from the Guardian website.
 */

public final class QueryUtils {

    //Tag for the log messages
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the News data set and return a list of {@link NewsItem} objects.
     */
    public static List<NewsItem> fetchNewsInfo(String requestUrl) {

        //Create URL object
        URL url = createUrl(requestUrl);

        //Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link NewsItem}s
        List<NewsItem> news = extractFeatureFromJson(jsonResponse);

        //Return the list of {@Link NewsItem}s
        return news;
    }

    //Returns new URL object from the given string URL.
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL.", e);
        }
        return url;
    }

    //Make http request to the given url and return a string as the response
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        //If the URL is null then return early
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON result.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    //Convert the {@Link InputStream} into a String which contains the whole JSON response from the server.
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    //Return the list of {@Link NewsItem} objects that has been built up from parsing the given JSON response
    private static List<NewsItem> extractFeatureFromJson(String newsJSON) {
        //if the JSON string is empty or null, then return early
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding news to
        List<NewsItem> news = new ArrayList<>();

        // Parsing the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            //Create JSONObject form JSON response string
            JSONObject baseJsonResponse = new JSONObject(newsJSON);
            // Extract the JSONArray associated with the key  "results",
            // which represents a list of news.
            JSONObject responseObject = baseJsonResponse.getJSONObject("response");
            // For a given news, extract the JSONObject associated with the
            // key called "results", which represents a list of all properties
            // for that news item.
            JSONArray newsArray = responseObject.getJSONArray("results");

            // For each news item in the newsArray, create an {@link NewsItem} object
            for (int i = 0; i < newsArray.length(); i++) {
                // Get a single news item at position i  within the list of news feed
                JSONObject currentNews = newsArray.getJSONObject(i);

                // Extract the value for the key called "sectionName"
                String nameOfSection = currentNews.getString("sectionName");
                // Extract the value for the key called "webTitle"
                String nameOfArticle = currentNews.getString("webTitle");
                // Extract the value for the key called "webPublicationDate"
                String date = currentNews.getString("webPublicationDate");
                Log.e("QueryUtils", "DATE");
                // Extract the value for the key called "webUrl"
                String url = currentNews.getString("webUrl");
                // extract the value for the key "webTitle" (author)
                String author = "";
                try {
                    // Extract the JSONArray associated with the key "tags"
                    JSONArray tagsArray = currentNews.getJSONArray("tags");

                    for (int j = 0; j < tagsArray.length(); j++) {
                        JSONObject authorObj = tagsArray.getJSONObject(j);
                        // Extract the value for the key called "webTitle"
                        if (authorObj.has("webTitle")) {
                            author = authorObj.getString("webTitle");
                        } else {
                            author = "N/A";
                        }
                    }
                } catch (Exception e) {
                    Log.e("QueryUtils", "Problem finding the author", e);
                }

                // Create a new {@link NewsItem} object with the nameOfSection, nameOfArticle, date,
                // and url from the JSON response.
                NewsItem newsBlock = new NewsItem(nameOfSection, nameOfArticle, date, url, author);
                news.add(newsBlock);
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the news JSON results", e);
        }
        return news;
    }
}
