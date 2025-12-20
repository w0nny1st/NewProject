package com.example.newproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.OnNoteClickListener {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private List<Note> allNotes;
    private List<Note> filteredNotes;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private ChipGroup filterChipGroup;
    private Chip chipAll, chipPinned, chipImportant;
    private static final int REQUEST_EDIT_NOTE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        loadNotesFromPreferences();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);

        filterChipGroup = findViewById(R.id.filter_chip_group);
        chipAll = findViewById(R.id.chip_all);
        chipPinned = findViewById(R.id.chip_pinned);
        chipImportant = findViewById(R.id.chip_important);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Мои Заметки");
        }

        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {

                chipAll.setChecked(true);
            } else {
                applyFilter(checkedIds.get(0));
            }
        });

        fab.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100);
                        openEditNoteActivity(null, -1);
                    });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_simple, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_clear) {
            clearAllNotes();
            return true;
        } else if (id == R.id.menu_about) {
            Toast.makeText(this, "Приложение Заметки v1.0", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearAllNotes() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Очистить все заметки")
                .setMessage("Удалить все заметки?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    allNotes.clear();
                    filteredNotes.clear();
                    adapter.updateNotes(filteredNotes);
                    saveNotesToPreferences();
                    Toast.makeText(this, "Все заметки удалены", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void setupRecyclerView() {
        allNotes = new ArrayList<>();
        filteredNotes = new ArrayList<>();
        adapter = new NotesAdapter(filteredNotes, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }


    private void applyFilter(int chipId) {
        filteredNotes.clear();

        if (chipId == R.id.chip_all) {
            filteredNotes.addAll(allNotes);
        } else if (chipId == R.id.chip_pinned) {
            for (Note note : allNotes) {
                if (note.isPinned()) {
                    filteredNotes.add(note);
                }
            }
        } else if (chipId == R.id.chip_important) {

            for (Note note : allNotes) {
                if (note.getColor() != 0 ||
                        note.getTitle().toLowerCase().contains("важн") ||
                        note.getContent().toLowerCase().contains("важн")) {
                    filteredNotes.add(note);
                }
            }
        }

        adapter.updateNotes(filteredNotes);
    }

    private void openEditNoteActivity(Note note, int position) {
        Intent intent = new Intent(this, EditNoteActivity.class);
        if (note != null) {
            intent.putExtra("note", note);
            intent.putExtra("position", position);
        }
        startActivityForResult(intent, REQUEST_EDIT_NOTE);
    }

    @Override
    public void onNoteClick(int position) {
        if (position < 0 || position >= filteredNotes.size()) {
            return;
        }

        Note note = filteredNotes.get(position);

        int originalPosition = allNotes.indexOf(note);
        openEditNoteActivity(note, originalPosition);
    }

    @Override
    public void onNoteLongClick(int position) {
        if (position < 0 || position >= filteredNotes.size()) return;

        Note note = filteredNotes.get(position);
        note.setPinned(!note.isPinned());

        int originalPosition = allNotes.indexOf(note);
        if (originalPosition != -1) {
            allNotes.set(originalPosition, note);
        }

        applyFilter(filterChipGroup.getCheckedChipId());
        saveNotesToPreferences();

        String message = note.isPinned() ? "Закреплено" : "Откреплено";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_NOTE && resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");

            if ("delete".equals(action)) {
                int position = data.getIntExtra("position", -1);
                if (position != -1 && position < allNotes.size()) {
                    allNotes.remove(position);
                    applyFilter(filterChipGroup.getCheckedChipId());
                    saveNotesToPreferences();
                    Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            Note updatedNote = (Note) data.getSerializableExtra("note");
            if (updatedNote == null) return;

            int position = data.getIntExtra("position", -1);
            boolean isEdit = data.getBooleanExtra("isEdit", false);

            if (isEdit && position != -1 && position < allNotes.size()) {
                allNotes.set(position, updatedNote);
            } else {
                allNotes.add(0, updatedNote);
            }

            applyFilter(filterChipGroup.getCheckedChipId());
            saveNotesToPreferences();
            recyclerView.smoothScrollToPosition(0);

            String message = isEdit ? "Сохранено" : "Добавлено";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNotesToPreferences() {
        android.content.SharedPreferences prefs = getSharedPreferences("notes_prefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("notes_count", allNotes.size());

        for (int i = 0; i < allNotes.size(); i++) {
            Note note = allNotes.get(i);
            String noteJson = convertNoteToJson(note);
            editor.putString("note_" + i, noteJson);
        }

        editor.apply();
    }

    private void loadNotesFromPreferences() {
        android.content.SharedPreferences prefs = getSharedPreferences("notes_prefs", MODE_PRIVATE);
        int notesCount = prefs.getInt("notes_count", 0);

        allNotes.clear();

        if (notesCount == 0) {
            loadSampleData();
        } else {
            for (int i = 0; i < notesCount; i++) {
                String noteJson = prefs.getString("note_" + i, "");
                if (!noteJson.isEmpty()) {
                    Note note = convertJsonToNote(noteJson);
                    if (note != null) {
                        allNotes.add(note);
                    }
                }
            }
        }

        applyFilter(R.id.chip_all);
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
        allNotes.add(new Note("Добро пожаловать", "Это приложение для заметок"));
        allNotes.add(new Note("Список покупок", "Молоко, Хлеб, Яйца, Фрукты"));
        allNotes.add(new Note("Задачи на день", "1. Сделать проект\n2. Погулять\n3. Отдохнуть"));
        allNotes.add(new Note("Важная встреча", "Не забыть про встречу с клиентом в 15:00"));

        Note pinnedNote = new Note("Важная заметка", "Не забыть сдать проект до пятницы!");
        pinnedNote.setPinned(true);
        allNotes.add(pinnedNote);

        Note importantNote = new Note("Идеи для проекта", "1. Material Design\n2. SplashScreen\n3. Фильтрация");
        importantNote.setColor(0xFFFFEB3B);
        allNotes.add(importantNote);

        saveNotesToPreferences();
    }
}