package com.iosharp.android.ssplayer.model;

public class Event {

    private int mId;
    private String mNetwork;
    private String mName;
    private String mDescription;
    private long mStartDate;
    private long mEndDate;
    private long mRuntime;
    private int mChannel;
    private String mLanguage;
    private String mCategory; // Not retrieved
    private String mQuality;
    private String mDate;

    public Event() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getNetwork() {
        return mNetwork;
    }

    public void setNetwork(String network) {
        mNetwork = network;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public long getStartDate() {
        return mStartDate;
    }

    public void setStartDate(long startDate) {
        mStartDate = startDate;
    }

    public long getEndDate() {
        return mEndDate;
    }

    public void setEndDate(long endDate) {
        mEndDate = endDate;
    }

    public long getRuntime() {
        return mRuntime;
    }

    public void setRuntime(long runtime) {
        mRuntime = runtime;
    }

    public int getChannel() {
        return mChannel;
    }

    public void setChannel(int channel) {
        mChannel = channel;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String language) {
        mLanguage = language;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public String getQuality() {
        return mQuality;
    }

    public void setQuality(String quality) {
        mQuality = quality;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }
}
