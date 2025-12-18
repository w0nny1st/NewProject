package com.example.newproject;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;


public class Note implements Serializable {
    private String id;
    private String title;
    private String content;
    private long timestamp;
    private boolean isPinned;
    private int color;

    public Note(String title, String content) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.title = title;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.isPinned = false;
        this.color = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
}