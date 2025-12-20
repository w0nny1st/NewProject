package com.example.newproject;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {

    private NoteDao noteDao;
    private LiveData<List<Note>> allNotes;
    private ExecutorService executorService;

    public NoteRepository(Application application) {
        NoteDatabase database = NoteDatabase.getDatabase(application);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Note note) {
        executorService.execute(() -> noteDao.insert(note));
    }

    public void update(Note note) {
        executorService.execute(() -> noteDao.update(note));
    }

    public void delete(Note note) {
        executorService.execute(() -> noteDao.delete(note));
    }

    public void deleteAllNotes() {
        executorService.execute(() -> noteDao.deleteAllNotes());
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> getPinnedNotes() {
        return noteDao.getPinnedNotes();
    }

    public LiveData<List<Note>> searchNotes(String query) {
        return noteDao.searchNotes(query);
    }

    public LiveData<Note> getNoteById(String id) {
        return noteDao.getNoteById(id);
    }
}