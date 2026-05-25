package com.example.expensetracker.models;

public class Budget {
    private int    id;
    private String category;
    private double amount;
    private String month;

    public Budget() {}

    public Budget(String category, double amount, String month) {
        this.category = category;
        this.amount   = amount;
        this.month    = month;
    }

    public int    getId()              { return id; }
    public void   setId(int id)        { this.id = id; }

    public String getCategory()        { return category; }
    public void   setCategory(String c){ this.category = c; }

    public double getAmount()          { return amount; }
    public void   setAmount(double a)  { this.amount = a; }

    public String getMonth()           { return month; }
    public void   setMonth(String m)   { this.month = m; }
}
