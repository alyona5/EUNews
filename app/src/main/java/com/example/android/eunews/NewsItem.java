package com.example.android.eunews;

/**
 * {Link NewsItem} represents the information about the news article, such as the section, name of
 * the article, author name and date published.
 */

public class NewsItem {

    //Website url of the news
    private String mUrl;

    //Name of the section
    private String mNameOfSection;

    //Name of the article
    private String mNameOfArticle;

    //Date of published article
    private String mDate;

    private String mAuthor;

    /**
     * Creating new NewsItem object.
     *
     * @param url;
     * @param nameOfSection;
     * @param nameOfArticle;
     * @param date;
     * @param author
     */
    public NewsItem(String nameOfSection, String nameOfArticle, String date, String url, String author) {
        mUrl = url;
        mNameOfSection = nameOfSection;
        mNameOfArticle = nameOfArticle;
        mDate = date;
        mAuthor = author;
    }

    //Get the URL
    public String getUrl() {
        return mUrl;
    }

    //Get the information about the section
    public String getNameOfSection() {
        return mNameOfSection;
    }

    //Get the information about the article
    public String getNameOfArticle() {
        return mNameOfArticle;
    }

    //Get the information about the author
    public String getAuthor() {
        return mAuthor;
    }

    //Get the information about the date
    public String getDate() {
        return mDate;
    }

    public void setDate(String formattedDate) {
        mDate = formattedDate;
    }

}
