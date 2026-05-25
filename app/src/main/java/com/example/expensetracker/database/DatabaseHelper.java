package com.example.expensetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.expensetracker.models.Budget;
import com.example.expensetracker.models.Expense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "expense_tracker.db";
    private static final int    DB_VERSION = 1;


    public static final String TABLE_EXPENSES  = "expenses";
    public static final String COL_ID          = "id";
    public static final String COL_TITLE       = "title";
    public static final String COL_AMOUNT      = "amount";
    public static final String COL_CATEGORY    = "category";
    public static final String COL_DATE        = "date";
    public static final String COL_NOTE        = "note";


    public static final String TABLE_BUDGETS   = "budgets";
    public static final String COL_BUD_ID      = "id";
    public static final String COL_BUD_CAT     = "category";
    public static final String COL_BUD_AMOUNT  = "amount";
    public static final String COL_BUD_MONTH   = "month";

    private static final String CREATE_EXPENSES =
            "CREATE TABLE " + TABLE_EXPENSES + " (" +
            COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TITLE    + " TEXT NOT NULL, " +
            COL_AMOUNT   + " REAL NOT NULL, " +
            COL_CATEGORY + " TEXT NOT NULL, " +
            COL_DATE     + " TEXT NOT NULL, " +
            COL_NOTE     + " TEXT" +
            ");";

    private static final String CREATE_BUDGETS =
            "CREATE TABLE " + TABLE_BUDGETS + " (" +
            COL_BUD_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_BUD_CAT    + " TEXT NOT NULL, " +
            COL_BUD_AMOUNT + " REAL NOT NULL, " +
            COL_BUD_MONTH  + " TEXT NOT NULL, " +
            "UNIQUE(" + COL_BUD_CAT + ", " + COL_BUD_MONTH + ") ON CONFLICT REPLACE" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EXPENSES);
        db.execSQL(CREATE_BUDGETS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        onCreate(db);
    }



    public long addExpense(Expense e) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv  = buildExpenseValues(e);
        long id = db.insert(TABLE_EXPENSES, null, cv);
        db.close();
        return id;
    }

    public int updateExpense(Expense e) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv  = buildExpenseValues(e);
        int rows = db.update(TABLE_EXPENSES, cv, COL_ID + "=?",
                new String[]{String.valueOf(e.getId())});
        db.close();
        return rows;
    }

    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }


    public List<Expense> getAllExpenses() {
        return queryExpenses(null, null, COL_DATE + " DESC");
    }


    public List<Expense> getExpensesByDate(String date) {
        return queryExpenses(COL_DATE + "=?", new String[]{date}, COL_DATE + " DESC");
    }


    public List<Expense> getExpensesByMonth(String month) {
        return queryExpenses(COL_DATE + " LIKE ?", new String[]{month + "%"}, COL_DATE + " DESC");
    }


    public List<Expense> getExpensesByCategory(String category) {
        return queryExpenses(COL_CATEGORY + "=?", new String[]{category}, COL_DATE + " DESC");
    }


    public double getTotalForMonth(String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_EXPENSES +
                " WHERE " + COL_DATE + " LIKE ?", new String[]{month + "%"});
        double total = 0;
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close(); db.close();
        return total;
    }


    public double getTotalForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_EXPENSES +
                " WHERE " + COL_DATE + "=?", new String[]{date});
        double total = 0;
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close(); db.close();
        return total;
    }


    public Map<String, Double> getCategoryTotalsForMonth(String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, Double> map = new HashMap<>();
        Cursor c = db.rawQuery(
                "SELECT " + COL_CATEGORY + ", SUM(" + COL_AMOUNT + ") " +
                "FROM " + TABLE_EXPENSES +
                " WHERE " + COL_DATE + " LIKE ?" +
                " GROUP BY " + COL_CATEGORY,
                new String[]{month + "%"});
        while (c.moveToNext()) map.put(c.getString(0), c.getDouble(1));
        c.close(); db.close();
        return map;
    }



    public long saveBudget(Budget b) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv  = new ContentValues();
        cv.put(COL_BUD_CAT,    b.getCategory());
        cv.put(COL_BUD_AMOUNT, b.getAmount());
        cv.put(COL_BUD_MONTH,  b.getMonth());
        long id = db.insertWithOnConflict(TABLE_BUDGETS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return id;
    }

    public Budget getBudget(String category, String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_BUDGETS, null,
                COL_BUD_CAT + "=? AND " + COL_BUD_MONTH + "=?",
                new String[]{category, month}, null, null, null);
        Budget b = null;
        if (c.moveToFirst()) b = cursorToBudget(c);
        c.close(); db.close();
        return b;
    }

    public List<Budget> getBudgetsForMonth(String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Budget> list = new ArrayList<>();
        Cursor c = db.query(TABLE_BUDGETS, null,
                COL_BUD_MONTH + "=?", new String[]{month},
                null, null, null);
        while (c.moveToNext()) list.add(cursorToBudget(c));
        c.close(); db.close();
        return list;
    }



    private ContentValues buildExpenseValues(Expense e) {
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE,    e.getTitle());
        cv.put(COL_AMOUNT,   e.getAmount());
        cv.put(COL_CATEGORY, e.getCategory());
        cv.put(COL_DATE,     e.getDate());
        cv.put(COL_NOTE,     e.getNote());
        return cv;
    }

    private List<Expense> queryExpenses(String selection, String[] selArgs, String orderBy) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Expense> list = new ArrayList<>();
        Cursor c = db.query(TABLE_EXPENSES, null, selection, selArgs, null, null, orderBy);
        while (c.moveToNext()) list.add(cursorToExpense(c));
        c.close(); db.close();
        return list;
    }

    private Expense cursorToExpense(Cursor c) {
        Expense e = new Expense();
        e.setId(      c.getInt(   c.getColumnIndexOrThrow(COL_ID)));
        e.setTitle(   c.getString(c.getColumnIndexOrThrow(COL_TITLE)));
        e.setAmount(  c.getDouble(c.getColumnIndexOrThrow(COL_AMOUNT)));
        e.setCategory(c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)));
        e.setDate(    c.getString(c.getColumnIndexOrThrow(COL_DATE)));
        e.setNote(    c.getString(c.getColumnIndexOrThrow(COL_NOTE)));
        return e;
    }

    private Budget cursorToBudget(Cursor c) {
        Budget b = new Budget();
        b.setId(      c.getInt(   c.getColumnIndexOrThrow(COL_BUD_ID)));
        b.setCategory(c.getString(c.getColumnIndexOrThrow(COL_BUD_CAT)));
        b.setAmount(  c.getDouble(c.getColumnIndexOrThrow(COL_BUD_AMOUNT)));
        b.setMonth(   c.getString(c.getColumnIndexOrThrow(COL_BUD_MONTH)));
        return b;
    }
}
