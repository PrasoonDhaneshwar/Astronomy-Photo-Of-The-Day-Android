package com.prasoon.astronomypictureoftheday;

public class ExampleItem {
    private String mImageUrl;
    private String mTitle;
    private String mDate;

    public ExampleItem(String mImageUrl, String mTitle, String mDate) {
        this.mImageUrl = mImageUrl;
        this.mTitle = mTitle;
        this.mDate = mDate;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDate() {
        return mDate;
    }
}
