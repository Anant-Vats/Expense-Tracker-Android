package com.example.expensetracker.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Budget;
import com.example.expensetracker.utils.Constants;

public class BudgetActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EditText       etOverallBudget;
    private LinearLayout   llCategoryBudgets;
    private String         currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Set Budget");
        }

        db            = new DatabaseHelper(this);
        currentMonth  = Constants.currentMonthString();
        etOverallBudget  = findViewById(R.id.etOverallBudget);
        llCategoryBudgets = findViewById(R.id.llCategoryBudgets);

        // Load existing overall budget
        Budget overall = db.getBudget("TOTAL", currentMonth);
        if (overall != null) {
            etOverallBudget.setText(String.valueOf((int) overall.getAmount()));
        }

        // Build per-category fields
        buildCategoryFields();

        findViewById(R.id.btnSaveBudget).setOnClickListener(v -> saveBudgets());
    }

    private void buildCategoryFields() {
        llCategoryBudgets.removeAllViews();
        for (String cat : Constants.CATEGORIES) {
            View row = getLayoutInflater()
                    .inflate(R.layout.item_budget_row, llCategoryBudgets, false);

            String icon = Constants.iconForCategory(cat);
            ((TextView) row.findViewById(R.id.tvBudgetCatLabel))
                    .setText(icon + "  " + cat);

            EditText etBudget = row.findViewById(R.id.etCategoryBudget);
            etBudget.setTag(cat);   // store category name as tag

            // Pre-fill if budget exists
            Budget existing = db.getBudget(cat, currentMonth);
            if (existing != null && existing.getAmount() > 0) {
                etBudget.setText(String.valueOf((int) existing.getAmount()));
            }

            llCategoryBudgets.addView(row);
        }
    }

    private void saveBudgets() {
        // Save overall
        String overallStr = etOverallBudget.getText().toString().trim();
        if (!overallStr.isEmpty()) {
            try {
                double amt = Double.parseDouble(overallStr);
                db.saveBudget(new Budget("TOTAL", amt, currentMonth));
            } catch (NumberFormatException ignored) {}
        }

        // Save per-category
        for (int i = 0; i < llCategoryBudgets.getChildCount(); i++) {
            View row = llCategoryBudgets.getChildAt(i);
            EditText et = row.findViewById(R.id.etCategoryBudget);
            String cat  = (String) et.getTag();
            String val  = et.getText().toString().trim();
            if (!val.isEmpty()) {
                try {
                    double amt = Double.parseDouble(val);
                    db.saveBudget(new Budget(cat, amt, currentMonth));
                } catch (NumberFormatException ignored) {}
            }
        }

        Toast.makeText(this, "Budgets saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
