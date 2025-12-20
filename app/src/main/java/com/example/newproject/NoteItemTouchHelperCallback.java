package com.example.newproject;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class NoteItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final NotesAdapter adapter;
    private final Drawable deleteIcon;
    private final ColorDrawable background;
    private int iconMargin;

    public NoteItemTouchHelperCallback(NotesAdapter adapter, View itemView) {
        this.adapter = adapter;
        this.deleteIcon = ContextCompat.getDrawable(itemView.getContext(), android.R.drawable.ic_delete);
        this.background = new ColorDrawable(Color.RED);
        this.iconMargin = (int) itemView.getResources().getDimension(android.R.dimen.app_icon_size) / 2;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(0, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        adapter.onItemSwiped(position);
    }

    @Override
    public void onChildDraw(@NonNull Canvas canvas,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;

        int backgroundCornerOffset = 20;
        if (dX > 0) {

            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                    itemView.getBottom());
        } else {

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        }
        background.draw(canvas);

        if (deleteIcon != null) {
            int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

            if (dX > 0) {
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = iconLeft + deleteIcon.getIntrinsicWidth();
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            } else {
                int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            }
            deleteIcon.draw(canvas);
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.5f;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return defaultValue * 2;
    }
}