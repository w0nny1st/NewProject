package com.example.newproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
    private static final int REQUEST_EDIT_NOTE = 1;
    private int selectedNotePosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        loadNotesFromPreferences();

        registerForContextMenu(recyclerView);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(v -> {

            v.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100));

            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivityForResult(intent, REQUEST_EDIT_NOTE);
        });

        fab.setOnLongClickListener(v -> {
            Note quickNote = new Note("Быстрая заметка", "Создано: " +
                    new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                            .format(new java.util.Date()));
            adapter.addNote(quickNote);
            saveNotesToPreferences();
            recyclerView.smoothScrollToPosition(0);
            Toast.makeText(this, "Быстрая заметка создана!", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void setupRecyclerView() {
        notes = new ArrayList<>();
        adapter = new NotesAdapter(notes, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onNoteClick(int position) {
        if (position < 0 || position >= notes.size()) {
            Toast.makeText(this, "Ошибка: заметка не найдена", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedNotePosition = position;
        Note note = notes.get(position);

        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra("note", note);
        intent.putExtra("position", position);
        startActivityForResult(intent, REQUEST_EDIT_NOTE);
    }

    @Override
    public void onNoteLongClick(int position) {
        if (position < 0 || position >= notes.size()) return;

        selectedNotePosition = position;
        recyclerView.showContextMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_NOTE && resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");

            if ("delete".equals(action)) {
                int position = data.getIntExtra("position", -1);
                if (position != -1 && position < notes.size()) {
                    adapter.removeNote(position);
                    saveNotesToPreferences(); // Сохраняем изменения
                    Toast.makeText(this, "Заметка удалена", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            Note updatedNote = (Note) data.getSerializableExtra("note");
            if (updatedNote == null) return;

            int position = data.getIntExtra("position", -1);
            boolean isEdit = data.getBooleanExtra("isEdit", false);

            if (isEdit && position != -1 && position < notes.size()) {

                notes.set(position, updatedNote);
                adapter.updateNotes(notes);
                saveNotesToPreferences();
                Toast.makeText(this, "Заметка обновлена", Toast.LENGTH_SHORT).show();
            } else {

                adapter.addNote(updatedNote);
                saveNotesToPreferences();
                recyclerView.smoothScrollToPosition(0);
                Toast.makeText(this, "Новая заметка добавлена", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.recycler_view) {
            getMenuInflater().inflate(R.menu.context_menu_note, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (selectedNotePosition == -1 || selectedNotePosition >= notes.size()) {
            return false;
        }

        Note note = notes.get(selectedNotePosition);
        int id = item.getItemId();

        if (id == R.id.menu_edit) {
            onNoteClick(selectedNotePosition);
            return true;
        } else if (id == R.id.menu_delete) {
            adapter.removeNote(selectedNotePosition);
            saveNotesToPreferences();
            Toast.makeText(this, "Заметка удалена", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_pin) {
            note.setPinned(!note.isPinned());
            adapter.updateNotes(notes);
            saveNotesToPreferences();

            String message = note.isPinned() ? "Заметка закреплена 📌" : "Заметка откреплена";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_share) {
            shareNote(note);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void shareNote(Note note) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, note.getTitle() + "\n\n" + note.getContent());
        startActivity(Intent.createChooser(shareIntent, "Поделиться заметкой"));
    }


    private void saveNotesToPreferences() {
        android.content.SharedPreferences prefs = getSharedPreferences("notes_prefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();


        editor.putInt("notes_count", notes.size());


        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            String noteJson = convertNoteToJson(note);
            editor.putString("note_" + i, noteJson);
        }

        editor.apply();
        Toast.makeText(this, "Заметки сохранены", Toast.LENGTH_SHORT).show();
    }

    private void loadNotesFromPreferences() {
        android.content.SharedPreferences prefs = getSharedPreferences("notes_prefs", MODE_PRIVATE);
        int notesCount = prefs.getInt("notes_count", 0);

        if (notesCount == 0) {

            loadSampleData();
        } else {

            notes.clear();
            for (int i = 0; i < notesCount; i++) {
                String noteJson = prefs.getString("note_" + i, "");
                if (!noteJson.isEmpty()) {
                    Note note = convertJsonToNote(noteJson);
                    if (note != null) {
                        notes.add(note);
                    }
                }
            }
            adapter.updateNotes(notes);
        }
    }

    private String convertNoteToJson(Note note) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("id", note.getId());
            json.put("title", note.getTitle());
            json.put("content", note.getContent());
            json.put("timestamp", note.getTimestamp());
            json.put("isPinned", note.isPinned());
            json.put("color", note.getColor());
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private Note convertJsonToNote(String jsonString) {
        try {
            org.json.JSONObject json = new org.json.JSONObject(jsonString);
            Note note = new Note(json.getString("title"), json.getString("content"));
            note.setId(json.getString("id"));
            note.setTimestamp(json.getLong("timestamp"));
            note.setPinned(json.getBoolean("isPinned"));
            note.setColor(json.getInt("color"));
            return note;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadSampleData() {
        notes.add(new Note("Добро пожаловать", "Здесь можно создавать заметки!"));

        Note pinnedNote = new Note("Важная заметка", "Дописать проект");
        pinnedNote.setPinned(true);
        notes.add(pinnedNote);

        notes.add(new Note("Список покупок", "• Молоко\n• Хлеб\n• Яйца\n• Фрукты"));
        notes.add(new Note("Задачи на день", "1. Изучить Android разработку\n2. Создать приложение\n3. Протестировать функциональность"));

        adapter.updateNotes(notes);
        saveNotesToPreferences();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNotesToPreferences();
    }
}