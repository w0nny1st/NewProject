package com.example.newproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import androidx.core.app.NotificationManagerCompat;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String noteId = intent.getStringExtra("note_id");

        if (noteId == null) return;

        if ("DELETE_NOTE".equals(action)) {
            deleteNote(context, noteId);
            cancelNotification(context, noteId);
            Toast.makeText(context, "Заметка удалена", Toast.LENGTH_SHORT).show();
        }
        else if ("REMIND_LATER".equals(action)) {
            cancelNotification(context, noteId);
            scheduleReminderLater(context, noteId);
            Toast.makeText(context, "Напомню через 30 минут", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteNote(Context context, String noteId) {
        SharedPreferences prefs = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE);
        int notesCount = prefs.getInt("notes_count", 0);

        List<Note> notes = new ArrayList<>();
        for (int i = 0; i < notesCount; i++) {
            String noteJson = prefs.getString("note_" + i, "");
            if (!noteJson.isEmpty()) {
                try {
                    JSONObject json = new JSONObject(noteJson);
                    Note note = new Note(json.getString("title"), json.getString("content"));
                    note.setId(json.getString("id"));
                    note.setTimestamp(json.getLong("timestamp"));
                    note.setPinned(json.getBoolean("isPinned"));
                    note.setColor(json.getInt("color"));

                    if (!note.getId().equals(noteId)) {
                        notes.add(note);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("notes_count", notes.size());

        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            try {
                JSONObject json = new JSONObject();
                json.put("id", note.getId());
                json.put("title", note.getTitle());
                json.put("content", note.getContent());
                json.put("timestamp", note.getTimestamp());
                json.put("isPinned", note.isPinned());
                json.put("color", note.getColor());
                editor.putString("note_" + i, json.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        editor.apply();
    }

    private void cancelNotification(Context context, String noteId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = noteId.hashCode();
        notificationManager.cancel(notificationId);
    }

    private void scheduleReminderLater(Context context, String noteId) {
        Toast.makeText(context, "Напомню позже о заметке " + noteId.substring(0, 8), Toast.LENGTH_SHORT).show();
    }
}