package com.example.newproject;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private NoteRepository repository;
    private LiveData<List<Note>> allNotes;
    private MutableLiveData<List<Note>> filteredNotes = new MutableLiveData<>();
    private MutableLiveData<String> currentFilter = new MutableLiveData<>("all");

    public NoteViewModel(Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();

        allNotes.observeForever(notes -> {
            if (notes != null) {
                applyFilter(currentFilter.getValue());
            }
        });
    }

    public void insert(Note note) {
        repository.insert(note);
    }

    public void update(Note note) {
        repository.update(note);
    }

    public void delete(Note note) {
        repository.delete(note);
    }

    public void deleteAllNotes() {
        repository.deleteAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> getFilteredNotes() {
        return filteredNotes;
    }

    public LiveData<String> getCurrentFilter() {
        return currentFilter;
    }

    public void applyFilter(String filterType) {
        currentFilter.setValue(filterType);

        if (allNotes.getValue() == null) return;

        List<Note> notes = allNotes.getValue();

        switch (filterType) {
            case "all":
                filteredNotes.setValue(notes);
                break;
            case "pinned":
                List<Note> pinnedNotes = new java.util.ArrayList<>();
                for (Note note : notes) {
                    if (note.isPinned()) {
                        pinnedNotes.add(note);
                    }
                }
                filteredNotes.setValue(pinnedNotes);
                break;
            case "important":
                List<Note> importantNotes = new java.util.ArrayList<>();
                for (Note note : notes) {
                    if (note.getColor() != 0 ||
                            note.getTitle().toLowerCase().contains("важн") ||
                            note.getContent().toLowerCase().contains("важн")) {
                        importantNotes.add(note);
                    }
                }
                filteredNotes.setValue(importantNotes);
                break;
        }
    }

    public LiveData<List<Note>> searchNotes(String query) {
        return repository.searchNotes(query);
    }

    public LiveData<Note> getNoteById(String id) {
        return repository.getNoteById(id);
    }
}