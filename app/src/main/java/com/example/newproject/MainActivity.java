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
        loadSampleData();

        registerForContextMenu(recyclerView);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(v -> openEditNoteActivity(null, -1));
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

    private void openEditNoteActivity(Note note, int position) {
        Intent intent = new Intent(this, EditNoteActivity.class);
        if (note != null) {
            Note noteCopy = new Note(note.getTitle(), note.getContent());
            noteCopy.setId(note.getId());
            noteCopy.setTimestamp(note.getTimestamp());
            noteCopy.setPinned(note.isPinned());
            noteCopy.setColor(note.getColor());

            intent.putExtra("note", noteCopy);
            intent.putExtra("position", position);
        }
        startActivityForResult(intent, REQUEST_EDIT_NOTE);
    }

    @Override
    public void onNoteClick(int position) {
        if (position < 0 || position >= notes.size()) {
            Toast.makeText(this, "Ошибка: заметка не найдена", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedNotePosition = position;
        Note note = notes.get(position);
        openEditNoteActivity(note, position);
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
                    Toast.makeText(this, "Заметка удалена", Toast.LENGTH_SHORT).show();
                }
            } else {
                Note updatedNote = (Note) data.getSerializableExtra("note");
                int position = data.getIntExtra("position", -1);
                boolean isEdit = data.getBooleanExtra("isEdit", false);

                if (updatedNote != null) {
                    if (isEdit && position != -1 && position < notes.size()) {

                        notes.set(position, updatedNote);
                        adapter.updateNotes(notes);
                        Toast.makeText(this, "Заметка обновлена", Toast.LENGTH_SHORT).show();
                    } else {

                        adapter.addNote(updatedNote);
                        recyclerView.smoothScrollToPosition(0);
                        Toast.makeText(this, "Новая заметка добавлена", Toast.LENGTH_SHORT).show();
                    }
                }
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
            openEditNoteActivity(note, selectedNotePosition);
            return true;
        } else if (id == R.id.menu_delete) {
            adapter.removeNote(selectedNotePosition);
            Toast.makeText(this, "Заметка удалена", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_pin) {
            note.setPinned(!note.isPinned());
            adapter.updateNotes(notes);

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
}