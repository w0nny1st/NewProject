package com.example.newproject;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("DELETE FROM notes")
    void deleteAllNotes();

    @Query("SELECT * FROM notes ORDER BY " +
            "CASE WHEN isPinned = 1 THEN 0 ELSE 1 END, " +
            "timestamp DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM notes WHERE isPinned = 1 ORDER BY timestamp DESC")
    LiveData<List<Note>> getPinnedNotes();

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    LiveData<List<Note>> searchNotes(String query);

    @Query("SELECT * FROM notes WHERE id = :id")
    LiveData<Note> getNoteById(String id);
}