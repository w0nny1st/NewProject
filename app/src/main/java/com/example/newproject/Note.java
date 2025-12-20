package com.example.newproject;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "notes")
public class Note implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String content;
    private long timestamp;
    private boolean isPinned;
    private int color;
    private boolean isImportant;

    public Note(@NonNull String title, String content) {
        this.id = String.valueOf(System.currentTimeMillis()) + "_" + System.nanoTime();
        this.title = title;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.isPinned = false;
        this.color = 0;
        this.isImportant = false;
    }

    public Note() {
        this.id = String.valueOf(System.currentTimeMillis()) + "_" + System.nanoTime();
        this.title = "";
        this.content = "";
        this.timestamp = System.currentTimeMillis();
        this.isPinned = false;
        this.color = 0;
        this.isImportant = false;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
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
    public boolean isImportant() { return isImportant; }
    public void setImportant(boolean important) { isImportant = important; }
}