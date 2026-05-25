package com.example.expensetracker.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Constants {

    // ── Expense categories ───────────────────────────────────────────────────
    public static final String[] CATEGORIES = {
        "Food & Dining",
        "Travel & Transport",
        "Shopping",
        "Entertainment",
        "Health & Medical",
        "Education",
        "Bills & Utilities",
        "Rent & Housing",
        "Others"
    };

    // ── Category emoji icons (same order as CATEGORIES) ─────────────────────
    public static final String[] CATEGORY_ICONS = {
        "🍔", "✈️", "🛍️", "🎬", "💊", "📚", "💡", "🏠", "📦"
    };

    // ── Intent / bundle keys ─────────────────────────────────────────────────
    public static final String EXTRA_EXPENSE_ID = "expense_id";
    public static final String EXTRA_MODE       = "mode";  // "add" or "edit"

    // ── Date formatters ──────────────────────────────────────────────────────
    public static final SimpleDateFormat DATE_FORMAT  =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public static final SimpleDateFormat MONTH_FORMAT =
            new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    public static final SimpleDateFormat DISPLAY_DATE =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat DISPLAY_MONTH =
            new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    public static String todayString() {
        return DATE_FORMAT.format(new Date());
    }

    public static String currentMonthString() {
        return MONTH_FORMAT.format(new Date());
    }

    /** Returns emoji icon for a given category name. */
    public static String iconForCategory(String category) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equals(category)) return CATEGORY_ICONS[i];
        }
        return "📦";
    }
}
