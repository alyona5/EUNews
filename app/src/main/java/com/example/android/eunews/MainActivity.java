package com.example.android.eunews;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Loader;
import android.app.LoaderManager.LoaderCallbacks;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<NewsItem>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String LOG_TAG = MainActivity.class.getName();

    private static final String GUARDIAN_REQUEST_URL =
            "https://content.guardianapis.com/search?";

    private static final int NEWS_LOADER_ID = 1;

    private static String apiKey = "b5bd28b1-3609-4d2a-920a-f920ffd01b86";

    //Adapter for the list of news
    private NewsAdapter mAdapter;

    //TextView that is displayed when the list is empty
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity);

        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = (ListView) findViewById(R.id.list);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyStateTextView);

        //Create a new adapter that takes the list of news as an input
        mAdapter = new NewsAdapter(this, new ArrayList<NewsItem>());

        //Set the adapter on the {@Link ListView}, so the list can be populated in the UI
        newsListView.setAdapter(mAdapter);

        // Obtain a reference to the SharedPreferences file for this app
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // And register to be notified of preference changes
        // So we know when the user has adjusted the query settings
        prefs.registerOnSharedPreferenceChangeListener(this);

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Find the current news item that was clicked on
                NewsItem currentNewsItem = mAdapter.getItem(position);

                //Convert the string URL into URL object (to pass into Intent constructor)
                Uri newsUri = Uri.parse(currentNewsItem.getUrl());

                //Create a new intent to view the news URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                //Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        // Get a reference to ConnectivityManager to check state of Network connectivity
        ConnectivityManager commMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //Gte details on a current active default data network
        NetworkInfo networkInfo = commMgr.getActiveNetworkInfo();

        //If there is network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            //get reference to the LoaderManager in order to interact with loaders
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface)
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            //Otherwise, display error
            //First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            //Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.settings_order_by_key)) ||
                key.equals(getString(R.string.settings_section_key))) {
            mAdapter.clear();

            // Hide the empty state text view as the loading indicator will be displayed
            mEmptyStateTextView.setVisibility(View.GONE);

            // Show the loading indicator while new data is being fetched
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.VISIBLE);

            // Restart the loader to re-query the news as the query settings have been updated
            getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
        }
    }

    @Override
    // onCreateLoader instantiates and returns a new Loader for the given ID
    public Loader<List<NewsItem>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // getString retrieves a String value from the preferences.
        // The second parameter is the default value for this preference.
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        String section = sharedPrefs.getString(
                getString(R.string.settings_section_key),
                getString(R.string.settings_section_default));

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);
        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Append query parameter and its value.
        uriBuilder.appendQueryParameter("orderBy", orderBy);
        uriBuilder.appendQueryParameter("section", section);
        uriBuilder.appendQueryParameter("from-date", "2014-01-01");
        uriBuilder.appendQueryParameter("page-size", "30");
        uriBuilder.appendQueryParameter("q", "ukraine OR germany");
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("api-key", apiKey);


        //Return the completed uri http://content.guardianapis.com/search?order-by=newest&show-tags=contributor&q=
        // Ukraine%20AND%20Germany&api-key=b5bd28b1-3609-4d2a-920a-f920ffd01b86
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<NewsItem>> loader, List<NewsItem> news) {
        //Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        //Set empty state to display "No news found"
        mEmptyStateTextView.setText(R.string.no_news_found);
        //Clear the adapter of previous loaded data
        mAdapter.clear();

        // If there is a valid list of {@link NewsItem}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            mAdapter.addAll(news);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsItem>> loader) {
        //Loader reset
        mAdapter.clear();
    }

    @Override
    // This method initialize the contents of the Activity's options menu.
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate options menu we specified in the XML
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
