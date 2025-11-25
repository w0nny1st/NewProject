package com.example.newproject;

public interface NoteDecorator {
    String decorateTitle(String title);
    String decorateContent(String content);
    int getDecoratedColor(int originalColor);
    String getType();
}

class PinnedNoteDecorator implements NoteDecorator {
    @Override
    public String decorateTitle(String title) {
        return "📌 " + title;
    }

    @Override
    public String decorateContent(String content) {
        return content;
    }

    @Override
    public int getDecoratedColor(int originalColor) {
        return originalColor != 0 ? originalColor : 0xFFE3F2FD; // Light blue
    }

    @Override
    public String getType() {
        return "PINNED";
    }
}

class ImportantNoteDecorator implements NoteDecorator {
    @Override
    public String decorateTitle(String title) {
        return "⭐ " + title;
    }

    @Override
    public String decorateContent(String content) {
        return content;
    }

    @Override
    public int getDecoratedColor(int originalColor) {
        return 0xFFFFF9C4; // Light yellow
    }

    @Override
    public String getType() {
        return "IMPORTANT";
    }
}

class UrgentNoteDecorator implements NoteDecorator {
    @Override
    public String decorateTitle(String title) {
        return "🚨 " + title;
    }

    @Override
    public String decorateContent(String content) {
        return content;
    }

    @Override
    public int getDecoratedColor(int originalColor) {
        return 0xFFFFEBEE; // Light red
    }

    @Override
    public String getType() {
        return "URGENT";
    }
}

class CompletedNoteDecorator implements NoteDecorator {
    @Override
    public String decorateTitle(String title) {
        return "✅ " + title;
    }

    @Override
    public String decorateContent(String content) {
        return content;
    }

    @Override
    public int getDecoratedColor(int originalColor) {
        return 0xFFE8F5E8; // Light green
    }

    @Override
    public String getType() {
        return "COMPLETED";
    }
}