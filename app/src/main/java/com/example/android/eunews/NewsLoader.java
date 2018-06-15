package com.example.android.eunews;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Loads a list of news feed by using an AsyncTask to perform the
 * network request to the given URL.
 */

public class NewsLoader extends AsyncTaskLoader<List<NewsItem>> {

    //Tag for log messages
    private static final String LOG_TAG = NewsLoader.class.getName();

    //Query URL
    private String mUrl;

    /**
     * Construct a new {@Link NewsLoader}
     *
     * @param context of the activity;
     * @param url     to load data from;
     */

    public NewsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    //On a background thread
    @Override
    public List<NewsItem> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        //Perform the network request, parse the response, and extract the list of news
        List<NewsItem> news = QueryUtils.fetchNewsInfo(mUrl);
        return news;
    }
}
