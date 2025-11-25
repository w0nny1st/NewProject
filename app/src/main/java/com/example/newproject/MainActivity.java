package com.example.newproject;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.OnNoteClickListener {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private List<Note> notes;
    private FloatingActionButton fab;
    private int noteCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        loadSampleData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(v -> addNewNote());
    }

    private void setupRecyclerView() {
        notes = new ArrayList<>();
        adapter = new NotesAdapter(notes, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadSampleData() {
        notes.add(new Note("Добро пожаловать", "Здесь можно создавать заметки!"));

        Note pinnedNote = new Note("Важная заметка", "Дописать проект");
        pinnedNote.setPinned(true);
        notes.add(pinnedNote);

        notes.add(new Note("Список покупок", "• Молоко\n• Хлеб\n• Яйца\n• Фрукты"));
        notes.add(new Note("Задачи на день", "1. Изучить Android разработку\n2. Создать приложение\n3. Протестировать функциональность"));

        adapter.updateNotes(notes);
    }

    private void addNewNote() {
        noteCounter++;
        Note newNote = new Note("Заметка " + noteCounter, "Это автоматически созданная заметка.");
        adapter.addNote(newNote);

        recyclerView.smoothScrollToPosition(0);
        Toast.makeText(this, "Новая заметка добавлена", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNoteClick(int position) {
        Note note = notes.get(position);
        Toast.makeText(this, "Открыть: " + note.getTitle(), Toast.LENGTH_SHORT).show();
        // Здесь можно открыть экран редактирования заметки
    }

    @Override
    public void onNoteLongClick(int position) {
        Note note = notes.get(position);
        note.setPinned(!note.isPinned());
        adapter.updateNotes(notes);

        String message = note.isPinned() ? "Заметка закреплена 📌" : "Заметка откреплена";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}