package com.example.newproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.OnNoteClickListener {
    private NoteViewModel noteViewModel;
    private NotificationHelper notificationHelper;
    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private ChipGroup filterChipGroup;
    private Chip chipAll, chipPinned, chipImportant;
    private static final int REQUEST_EDIT_NOTE = 1;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        notificationHelper = new NotificationHelper(this);

        initViews();
        setupRecyclerView();
        setupObservers();
        checkNotificationPermission();
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
                int chipId = checkedIds.get(0);
                if (chipId == R.id.chip_all) {
                    noteViewModel.applyFilter("all");
                } else if (chipId == R.id.chip_pinned) {
                    noteViewModel.applyFilter("pinned");
                } else if (chipId == R.id.chip_important) {
                    noteViewModel.applyFilter("important");
                }
            }
        });

        fab.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100);
                        openEditNoteActivity(null);
                    });
        });
    }

    private void setupRecyclerView() {
        adapter = new NotesAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                DividerItemDecoration.VERTICAL
        );
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void setupObservers() {
        noteViewModel.getFilteredNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                adapter.setNotes(notes);
            }
        });

        noteViewModel.getCurrentFilter().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String filter) {
                switch (filter) {
                    case "all":
                        chipAll.setChecked(true);
                        break;
                    case "pinned":
                        chipPinned.setChecked(true);
                        break;
                    case "important":
                        chipImportant.setChecked(true);
                        break;
                }
            }
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    showPermissionRationaleDialog();
                } else {
                    requestNotificationPermission();
                }
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION);
        }
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Разрешение на уведомления")
                .setMessage("Для отправки напоминаний о заметках необходимо разрешение на уведомления")
                .setPositiveButton("Разрешить", (dialog, which) -> requestNotificationPermission())
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Уведомления разрешены", Toast.LENGTH_SHORT).show();
                showTestNotification();
            } else {
                Toast.makeText(this, "Уведомления запрещены", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showTestNotification() {
        if (notificationHelper.areNotificationsEnabled()) {
            notificationHelper.showSimpleNotification(
                    "Тестовое уведомление",
                    "Это тестовое сообщение от приложения Заметки"
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_simple, menu);

        MenuItem testNotificationItem = menu.add(0, 999, 0, "Тест уведомления");
        testNotificationItem.setIcon(R.drawable.ic_alarm);
        testNotificationItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        MenuItem notificationSettingsItem = menu.add(0, 1000, 0, "Настройки уведомлений");
        notificationSettingsItem.setIcon(android.R.drawable.ic_menu_preferences);
        notificationSettingsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem reminderItem = menu.add(0, 1001, 0, "Создать напоминание");
        reminderItem.setIcon(R.drawable.ic_pin);
        reminderItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

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
        } else if (id == 999) {
            showTestNotification();
            return true;
        } else if (id == 1000) {
            notificationHelper.openNotificationSettings();
            return true;
        } else if (id == 1001) {
            showReminderDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showReminderDialog() {
        List<Note> notes = adapter.getNotes();
        if (notes == null || notes.isEmpty()) {
            Toast.makeText(this, "Нет заметок для напоминания", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] noteTitles = new String[notes.size()];
        for (int i = 0; i < notes.size(); i++) {
            noteTitles[i] = notes.get(i).getTitle();
            if (noteTitles[i].length() > 30) {
                noteTitles[i] = noteTitles[i].substring(0, 30) + "...";
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Выберите заметку для напоминания")
                .setItems(noteTitles, (dialog, which) -> {
                    Note selectedNote = notes.get(which);
                    createReminderForNote(selectedNote);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void createReminderForNote(Note note) {
        String[] reminderTimes = {"Через 5 минут", "Через 30 минут", "Через 1 час", "Через 3 часа", "Завтра утром"};

        new AlertDialog.Builder(this)
                .setTitle("Когда напомнить?")
                .setItems(reminderTimes, (dialog, which) -> {
                    if (notificationHelper.areNotificationsEnabled()) {
                        notificationHelper.showNoteReminder(note);
                        Toast.makeText(this, "Напоминание создано для заметки: " + note.getTitle(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Включите уведомления в настройках", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void openEditNoteActivity(Note note) {
        Intent intent = new Intent(this, EditNoteActivity.class);
        if (note != null) {
            intent.putExtra("note", note);
        }
        startActivityForResult(intent, REQUEST_EDIT_NOTE);
    }

    @Override
    public void onNoteClick(int position) {
        List<Note> notes = adapter.getNotes();
        if (notes != null && position >= 0 && position < notes.size()) {
            openEditNoteActivity(notes.get(position));
        }
    }

    @Override
    public void onNoteLongClick(int position) {
        List<Note> notes = adapter.getNotes();
        if (notes != null && position >= 0 && position < notes.size()) {
            Note note = notes.get(position);
            note.setPinned(!note.isPinned());
            noteViewModel.update(note);

            String message = note.isPinned() ? "Закреплено" : "Откреплено";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            if (notificationHelper.areNotificationsEnabled() && note.isPinned()) {
                notificationHelper.showSimpleNotification(
                        "Заметка закреплена",
                        "Заметка \"" + note.getTitle() + "\" закреплена вверху списка"
                );
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_NOTE && resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");

            if ("delete".equals(action)) {
                Note deletedNote = (Note) data.getSerializableExtra("note");
                if (deletedNote != null) {
                    noteViewModel.delete(deletedNote);
                    Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show();

                    if (notificationHelper.areNotificationsEnabled()) {
                        notificationHelper.showSimpleNotification(
                                "Заметка удалена",
                                "Заметка \"" + deletedNote.getTitle() + "\" удалена"
                        );
                    }
                }
                return;
            }

            Note updatedNote = (Note) data.getSerializableExtra("note");
            if (updatedNote == null) return;

            boolean isEdit = data.getBooleanExtra("isEdit", false);

            if (isEdit) {
                noteViewModel.update(updatedNote);
                Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
            } else {
                noteViewModel.insert(updatedNote);
                Toast.makeText(this, "Добавлено", Toast.LENGTH_SHORT).show();

                if (notificationHelper.areNotificationsEnabled()) {
                    notificationHelper.showSimpleNotification(
                            "Новая заметка",
                            "Заметка \"" + updatedNote.getTitle() + "\" добавлена"
                    );
                }
            }
        }
    }

    private void clearAllNotes() {
        new AlertDialog.Builder(this)
                .setTitle("Очистить все заметки")
                .setMessage("Удалить все заметки?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    noteViewModel.deleteAllNotes();
                    Toast.makeText(this, "Все заметки удалены", Toast.LENGTH_SHORT).show();

                    if (notificationHelper.areNotificationsEnabled()) {
                        notificationHelper.showSimpleNotification(
                                "Все заметки удалены",
                                "Все заметки были успешно очищены"
                        );
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

}