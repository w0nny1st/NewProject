package com.example.newproject;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        holder.titleTextView.setText(note.getTitle());
        holder.contentTextView.setText(note.getContent());

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(note.getTimestamp());
        holder.dateTextView.setText(date);

        if (note.getColor() != 0) {
            holder.cardView.setCardBackgroundColor(note.getColor());
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        if (note.isPinned()) {
            holder.statusIcon.setImageResource(R.drawable.ic_pin);
            holder.statusIcon.setColorFilter(holder.itemView.getContext()
                    .getResources().getColor(R.color.accent_color));
        } else {
            holder.statusIcon.setImageResource(R.drawable.ic_notes);
            holder.statusIcon.setColorFilter(holder.itemView.getContext()
                    .getResources().getColor(R.color.primary_color));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNoteClick(position);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onNoteLongClick(position);
            return true;
        });
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
        ImageView statusIcon;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            contentTextView = itemView.findViewById(R.id.content_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            statusIcon = itemView.findViewById(R.id.status_icon);
        }
    }
}