package com.example.expensetracker.models;

public class Expense {
    private int id;
    private String title;
    private double amount;
    private String category;
    private String date;
    private String note;

    public Expense() {}

    public Expense(String title, double amount, String category, String date, String note) {
        this.title    = title;
        this.amount   = amount;
        this.category = category;
        this.date     = date;
        this.note     = note;
    }

    // Getters & Setters
    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public String getTitle()             { return title; }
    public void   setTitle(String t)     { this.title = t; }

    public double getAmount()            { return amount; }
    public void   setAmount(double a)    { this.amount = a; }

    public String getCategory()          { return category; }
    public void   setCategory(String c)  { this.category = c; }

    public String getDate()              { return date; }
    public void   setDate(String d)      { this.date = d; }

    public String getNote()              { return note; }
    public void   setNote(String n)      { this.note = n; }
}
