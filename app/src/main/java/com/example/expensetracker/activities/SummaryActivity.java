package com.example.expensetracker.activities;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Budget;
import com.example.expensetracker.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SummaryActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private LinearLayout   llCategoryBreakdown;
    private TextView       tvSummaryTitle, tvTotalSpent, tvBudgetInfo, tvDailyAvg;
    private Spinner        spinnerMonth;

    private final String[] MONTHS = generateMonthOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Summary");
        }

        db                  = new DatabaseHelper(this);
        llCategoryBreakdown = findViewById(R.id.llCategoryBreakdown);
        tvSummaryTitle      = findViewById(R.id.tvSummaryTitle);
        tvTotalSpent        = findViewById(R.id.tvTotalSpent);
        tvBudgetInfo        = findViewById(R.id.tvBudgetInfo);
        tvDailyAvg          = findViewById(R.id.tvDailyAvg);
        spinnerMonth        = findViewById(R.id.spinnerMonth);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, MONTHS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);
        spinnerMonth.setSelection(0); // current month first

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) {
                loadSummary(MONTHS[pos]);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        loadSummary(MONTHS[0]);
    }

    private void loadSummary(String monthKey) {
        // Header
        try {
            java.util.Date d = Constants.MONTH_FORMAT.parse(monthKey);
            tvSummaryTitle.setText(d != null ? Constants.DISPLAY_MONTH.format(d) : monthKey);
        } catch (Exception e) {
            tvSummaryTitle.setText(monthKey);
        }

        // Totals
        double total = db.getTotalForMonth(monthKey);
        tvTotalSpent.setText(String.format("Total Spent: ₹ %.2f", total));

        // Daily average (days in month so far or full month)
        int days = daysElapsedInMonth(monthKey);
        double avg = days > 0 ? total / days : 0;
        tvDailyAvg.setText(String.format("Daily Average: ₹ %.2f", avg));

        // Budget info
        Budget budget = db.getBudget("TOTAL", monthKey);
        if (budget != null && budget.getAmount() > 0) {
            double remaining = budget.getAmount() - total;
            tvBudgetInfo.setText(remaining >= 0
                    ? String.format("✅ Budget: ₹%.2f  |  Remaining: ₹%.2f", budget.getAmount(), remaining)
                    : String.format("⚠️ Budget: ₹%.2f  |  Exceeded by: ₹%.2f", budget.getAmount(), -remaining));
        } else {
            tvBudgetInfo.setText("No budget set for this month");
        }

        // Category breakdown
        llCategoryBreakdown.removeAllViews();
        Map<String, Double> catMap = db.getCategoryTotalsForMonth(monthKey);

        if (catMap.isEmpty()) {
            TextView noData = new TextView(this);
            noData.setText("No expenses recorded for this month.");
            noData.setPadding(16, 16, 16, 16);
            llCategoryBreakdown.addView(noData);
            return;
        }

        // Sort entries by amount descending (Java 8 compatible — no Stream API)
        List<Map.Entry<String, Double>> entries = new ArrayList<>(catMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b) {
                return Double.compare(b.getValue(), a.getValue());
            }
        });

        for (Map.Entry<String, Double> entry : entries) {
            String category = entry.getKey();
            double amount   = entry.getValue();
            double pct      = total > 0 ? (amount / total) * 100 : 0;

            // Inflate category row
            android.view.View row = getLayoutInflater()
                    .inflate(R.layout.item_category_summary, llCategoryBreakdown, false);

            ((TextView) row.findViewById(R.id.tvCatIcon)).setText(
                    Constants.iconForCategory(category));
            ((TextView) row.findViewById(R.id.tvCatName)).setText(category);
            ((TextView) row.findViewById(R.id.tvCatAmount)).setText(
                    String.format("₹ %.2f", amount));
            ((TextView) row.findViewById(R.id.tvCatPercent)).setText(
                    String.format("%.1f%%", pct));

            ProgressBar pb = row.findViewById(R.id.pbCategory);
            pb.setMax(100);
            pb.setProgress((int) pct);

            // Check category budget
            Budget catBudget = db.getBudget(category, monthKey);
            TextView tvCatBudget = row.findViewById(R.id.tvCatBudget);
            if (catBudget != null && catBudget.getAmount() > 0) {
                double rem = catBudget.getAmount() - amount;
                tvCatBudget.setText(rem >= 0
                        ? String.format("Budget ₹%.0f  •  ₹%.0f left", catBudget.getAmount(), rem)
                        : String.format("⚠️ Over by ₹%.0f", -rem));
            } else {
                tvCatBudget.setText("");
            }

            llCategoryBreakdown.addView(row);
        }
    }

    /** How many days have elapsed so far in the given month (capped at today). */
    private int daysElapsedInMonth(String monthKey) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String currentMonth = Constants.currentMonthString();
        if (monthKey.equals(currentMonth)) {
            return cal.get(java.util.Calendar.DAY_OF_MONTH);
        }
        // Past month: return total days
        try {
            java.util.Date d = Constants.MONTH_FORMAT.parse(monthKey);
            if (d != null) {
                cal.setTime(d);
                return cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            }
        } catch (Exception ignored) {}
        return 30;
    }

    /** Generate last 12 months as "yyyy-MM" strings, current month first. */
    private static String[] generateMonthOptions() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = Constants.MONTH_FORMAT.format(cal.getTime());
            cal.add(java.util.Calendar.MONTH, -1);
        }
        return months;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
