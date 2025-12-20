package com.example.newproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
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
import androidx.recyclerview.widget.ItemTouchHelper;
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
        setupItemTouchHelper();
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
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100)
                                .withEndAction(() -> openEditNoteActivity(null))
                                .start();
                    }).start();
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

    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteConfirmation(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                Paint paint = new Paint();

                if (dX > 0) {
                    paint.setColor(Color.parseColor("#FF3B30"));
                    c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(),
                            dX, (float) itemView.getBottom(), paint);

                    Drawable icon = ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_delete);
                    if (icon != null) {
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();
                        int iconLeft = itemView.getLeft() + iconMargin;
                        int iconRight = iconLeft + icon.getIntrinsicWidth();
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        icon.draw(c);
                    }
                } else {
                    paint.setColor(Color.parseColor("#FF9500"));
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                            (float) itemView.getRight(), (float) itemView.getBottom(), paint);

                    Drawable icon = ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_menu_share);
                    if (icon != null) {
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();
                        int iconRight = itemView.getRight() - iconMargin;
                        int iconLeft = iconRight - icon.getIntrinsicWidth();
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        icon.draw(c);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.5f;
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return defaultValue * 2;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setupObservers() {
        noteViewModel.getFilteredNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                adapter.setNotes(notes);

                if (recyclerView.getAdapter().getItemCount() > 0) {
                    for (int i = 0; i < recyclerView.getChildCount(); i++) {
                        View child = recyclerView.getChildAt(i);
                        child.setAlpha(0f);
                        child.setTranslationY(50);
                        child.animate()
                                .alpha(1f)
                                .translationY(0)
                                .setDuration(300)
                                .setStartDelay(i * 50)
                                .start();
                    }
                }
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

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onNoteClick(int position) {
        List<Note> notes = adapter.getNotes();
        if (notes != null && position >= 0 && position < notes.size()) {

            View itemView = recyclerView.getLayoutManager().findViewByPosition(position);
            if (itemView != null) {
                itemView.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            itemView.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .withEndAction(() -> openEditNoteActivity(notes.get(position)))
                                    .start();
                        })
                        .start();
            } else {
                openEditNoteActivity(notes.get(position));
            }
        }
    }

    @Override
    public void onNoteLongClick(int position) {
        List<Note> notes = adapter.getNotes();
        if (notes != null && position >= 0 && position < notes.size()) {
            Note note = notes.get(position);

            View itemView = recyclerView.getLayoutManager().findViewByPosition(position);
            if (itemView != null) {
                itemView.animate()
                        .translationY(-20)
                        .setDuration(200)
                        .withEndAction(() -> itemView.animate()
                                .translationY(0)
                                .setDuration(200)
                                .start())
                        .start();
            }

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
    public void onNoteSwiped(int position) {
        showDeleteConfirmation(position);
    }

    private void showDeleteConfirmation(int position) {
        List<Note> notes = adapter.getNotes();
        if (notes == null || position < 0 || position >= notes.size()) {
            adapter.notifyDataSetChanged();
            return;
        }

        Note noteToDelete = notes.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Удалить заметку")
                .setMessage("Удалить заметку \"" + noteToDelete.getTitle() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> {

                    animateNoteRemoval(position, noteToDelete);
                })
                .setNegativeButton("Отмена", (dialog, which) -> {

                    adapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> {

                    adapter.notifyItemChanged(position);
                })
                .show();
    }

    private void animateNoteRemoval(int position, Note note) {
        View itemView = recyclerView.getLayoutManager().findViewByPosition(position);

        if (itemView != null) {

            itemView.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .alpha(0.5f)
                    .setDuration(300)
                    .withEndAction(() -> {

                        noteViewModel.delete(note);

                        Toast.makeText(this, "Заметка удалена", Toast.LENGTH_SHORT).show();

                        fab.animate()
                                .rotationBy(360)
                                .setDuration(500)
                                .start();

                        if (notificationHelper.areNotificationsEnabled()) {
                            notificationHelper.showSimpleNotification(
                                    "Заметка удалена",
                                    "Заметка \"" + note.getTitle() + "\" удалена"
                            );
                        }
                    })
                    .start();
        } else {

            noteViewModel.delete(note);
            Toast.makeText(this, "Заметка удалена", Toast.LENGTH_SHORT).show();

            fab.animate()
                    .rotationBy(360)
                    .setDuration(500)
                    .start();
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
                    showDeleteAnimation(); // Анимация удаления
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
                showAddAnimation(); // Анимация добавления
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

    private void showAddAnimation() {
        fab.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(200)
                .withEndAction(() -> fab.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start())
                .start();
    }

    private void showDeleteAnimation() {
        fab.animate()
                .rotationBy(360)
                .setDuration(500)
                .start();
    }

    private void clearAllNotes() {
        new AlertDialog.Builder(this)
                .setTitle("Очистить все заметки")
                .setMessage("Удалить все заметки?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    noteViewModel.deleteAllNotes();
                    Toast.makeText(this, "Все заметки удалены", Toast.LENGTH_SHORT).show();

                    fab.animate()
                            .rotationBy(720)
                            .scaleX(1.3f)
                            .scaleY(1.3f)
                            .setDuration(800)
                            .withEndAction(() -> fab.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(300)
                                    .start())
                            .start();

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}