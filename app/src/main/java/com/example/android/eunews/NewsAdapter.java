package com.example.android.eunews;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * An {@link NewsAdapter} knows how to create a list item layout for each news feed
 * in the data source (a list of {@link NewsItem} objects).
 * <p>
 * These list item layouts will be provided to an adapter view like ListView
 * to be displayed to the user.
 */

public class NewsAdapter extends ArrayAdapter<NewsItem> {

    /**
     * Creating new {@Link NewsAdapter} class
     *
     * @param context
     * @param news
     */
    public NewsAdapter(Activity context, ArrayList<NewsItem> news) {
        super(context, 0, news);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.news_list_item, parent, false);
        }

        // Find the news feed at the given position in the list of news
        NewsItem currentNewsItem = (NewsItem) getItem(position);

        // Find the TextView in the news_list_item.xml layout
        TextView sectionName = (TextView) listItemView.findViewById(R.id.section_name);
        sectionName.setText(currentNewsItem.getNameOfSection());

        // Find the TextView in the news_list_item.xml layout
        TextView articleName = (TextView) listItemView.findViewById(R.id.article_name);
        articleName.setText(currentNewsItem.getNameOfArticle());

        // Find the TextView in the news_list_item.xml layout
        TextView author = (TextView) listItemView.findViewById(R.id.author);
        author.setText(currentNewsItem.getAuthor());

        //Formatting the date
        TextView dateView = (TextView) listItemView.findViewById(R.id.date);
        String JSONDate = currentNewsItem.getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'");
        Date date = null;
        try {
            date = dateFormat.parse(JSONDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat newDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String finalDate = newDateFormat.format(date);
        dateView.setText(finalDate);

        return listItemView;
    }
}
