package net.cdmsoftware.guardiannews;

public class News {
    private String mTitle;
    private String mAuthor;
    private String mWebUrl;

    public News(String title, String author, String url) {
        this.mAuthor = author;
        this.mTitle = title;
        this.mWebUrl = url;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getWebUrl() {
        return mWebUrl;
    }
}
