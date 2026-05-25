package com.example.expensetracker.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.utils.Constants;

import java.util.Calendar;

public class AddEditExpenseActivity extends AppCompatActivity {

    private EditText  etTitle, etAmount, etNote;
    private Spinner   spinnerCategory;
    private TextView  tvDate;
    private Button    btnSave;

    private DatabaseHelper db;
    private String         selectedDate;
    private boolean        isEdit;
    private int            expenseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        db = new DatabaseHelper(this);


        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Views
        etTitle         = findViewById(R.id.etTitle);
        etAmount        = findViewById(R.id.etAmount);
        etNote          = findViewById(R.id.etNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        tvDate          = findViewById(R.id.tvDate);
        btnSave         = findViewById(R.id.btnSave);


        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Constants.CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);


        selectedDate = Constants.todayString();
        tvDate.setText(selectedDate);


        tvDate.setOnClickListener(v -> showDatePicker());


        isEdit    = "edit".equals(getIntent().getStringExtra(Constants.EXTRA_MODE));
        expenseId = getIntent().getIntExtra(Constants.EXTRA_EXPENSE_ID, -1);

        if (isEdit) {
            setTitle("Edit Expense");
            btnSave.setText("Update");
            loadExpenseData();
        } else {
            setTitle("Add Expense");
        }

        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void loadExpenseData() {

        for (Expense e : db.getAllExpenses()) {
            if (e.getId() == expenseId) {
                etTitle.setText(e.getTitle());
                etAmount.setText(String.valueOf(e.getAmount()));
                etNote.setText(e.getNote());
                selectedDate = e.getDate();
                tvDate.setText(selectedDate);
                // Set spinner selection
                for (int i = 0; i < Constants.CATEGORIES.length; i++) {
                    if (Constants.CATEGORIES[i].equals(e.getCategory())) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void saveExpense() {
        String title    = etTitle.getText().toString().trim();
        String amtStr   = etAmount.getText().toString().trim();
        String note     = etNote.getText().toString().trim();
        String category = (String) spinnerCategory.getSelectedItem();


        if (title.isEmpty()) {
            etTitle.setError("Title required");
            etTitle.requestFocus();
            return;
        }
        if (amtStr.isEmpty()) {
            etAmount.setError("Amount required");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            etAmount.setError("Enter a valid positive amount");
            etAmount.requestFocus();
            return;
        }

        Expense e = new Expense(title, amount, category, selectedDate, note);

        if (isEdit) {
            e.setId(expenseId);
            db.updateExpense(e);
            Toast.makeText(this, "Expense updated!", Toast.LENGTH_SHORT).show();
        } else {
            db.addExpense(e);
            Toast.makeText(this, "Expense added!", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void showDatePicker() {

        Calendar cal = Calendar.getInstance();
        try {
            java.util.Date d = Constants.DATE_FORMAT.parse(selectedDate);
            if (d != null) cal.setTime(d);
        } catch (Exception ignored) {}

        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate = String.format(java.util.Locale.getDefault(),
                    "%04d-%02d-%02d", year, month + 1, day);
            tvDate.setText(selectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
           cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
