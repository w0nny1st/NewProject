package com.example.newproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;



public class EditNoteActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText contentEditText;
    private NoteViewModel noteViewModel;
    private Note currentNote;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        titleEditText = findViewById(R.id.title_edit_text);
        contentEditText = findViewById(R.id.content_edit_text);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("note")) {
            try {
                currentNote = (Note) intent.getSerializableExtra("note");
                isEditMode = true;

                if (currentNote != null) {
                    titleEditText.setText(currentNote.getTitle());
                    contentEditText.setText(currentNote.getContent());
                }
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Редактировать заметку");
                }
            } catch (Exception e) {
                e.printStackTrace();
                currentNote = new Note("", "");
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Новая заметка");
                }
            }
        } else {
            currentNote = new Note("", "");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Новая заметка");
            }
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (currentNote != null) {
                    currentNote.setTimestamp(System.currentTimeMillis());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        titleEditText.addTextChangedListener(textWatcher);
        contentEditText.addTextChangedListener(textWatcher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_note, menu);

        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        if (deleteItem != null) {
            deleteItem.setVisible(isEditMode);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            handleBackPressed();
            return true;
        } else if (id == R.id.action_save) {
            saveNote();
            return true;
        } else if (id == R.id.action_delete) {
            deleteNote();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        if (currentNote == null) {
            currentNote = new Note("", "");
        }

        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty()) {
            title = "Без названия";
        }

        currentNote.setTitle(title);
        currentNote.setContent(content);
        currentNote.setTimestamp(System.currentTimeMillis());

        Intent resultIntent = new Intent();
        resultIntent.putExtra("note", currentNote);
        resultIntent.putExtra("isEdit", isEditMode);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void deleteNote() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("action", "delete");
        resultIntent.putExtra("note", currentNote);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void handleBackPressed() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (!title.isEmpty() || !content.isEmpty()) {
            saveNote();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}