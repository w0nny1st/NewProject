package com.example.newproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(int position);
        void onNoteLongClick(int position);
    }

    public NotesAdapter(List<Note> notes, OnNoteClickListener listener) {
        this.notes = notes != null ? notes : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        // Apply decorators
        NoteDecorator decorator = getNoteDecorator(note);

        String decoratedTitle = decorator.decorateTitle(note.getTitle());
        String decoratedContent = decorator.decorateContent(note.getContent());
        int decoratedColor = decorator.getDecoratedColor(note.getColor());

        holder.titleTextView.setText(decoratedTitle);
        holder.contentTextView.setText(decoratedContent);
        holder.dateTextView.setText(formatDate(note.getTimestamp()));
        holder.typeTextView.setText(decorator.getType());

        // Set card background color
        holder.cardView.setCardBackgroundColor(decoratedColor);

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNoteClick(position);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onNoteLongClick(position);
            return true;
        });
    }

    private NoteDecorator getNoteDecorator(Note note) {
        // Priority: Pinned > Urgent > Important > Completed > Default
        if (note.isPinned()) {
            return new PinnedNoteDecorator();
        }

        // You can add more conditions based on note properties
        // For demo, we'll assign decorators based on position
        int position = notes.indexOf(note);
        switch (position % 4) {
            case 1: return new ImportantNoteDecorator();
            case 2: return new UrgentNoteDecorator();
            case 3: return new CompletedNoteDecorator();
            default:
                return new NoteDecorator() {
                    @Override
                    public String decorateTitle(String title) { return title; }
                    @Override
                    public String decorateContent(String content) { return content; }
                    @Override
                    public int getDecoratedColor(int originalColor) {
                        return originalColor != 0 ? originalColor : 0xFFFFFFFF;
                    }
                    @Override
                    public String getType() { return "STANDARD"; }
                };
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(timestamp);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void updateNotes(List<Note> newNotes) {
        this.notes = newNotes != null ? newNotes : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addNote(Note note) {
        notes.add(0, note);
        notifyItemInserted(0);
    }

    public void removeNote(int position) {
        if (position >= 0 && position < notes.size()) {
            notes.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView titleTextView;
        TextView contentTextView;
        TextView dateTextView;
        TextView typeTextView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            contentTextView = itemView.findViewById(R.id.content_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            typeTextView = itemView.findViewById(R.id.type_text_view);
        }
    }
}