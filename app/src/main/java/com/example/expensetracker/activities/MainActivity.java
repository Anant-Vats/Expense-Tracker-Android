package com.example.expensetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.adapters.ExpenseAdapter;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Budget;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ExpenseAdapter.OnExpenseClickListener {

    private DatabaseHelper  db;
    private ExpenseAdapter  adapter;
    private TextView        tvMonthTotal, tvMonthLabel, tvBudgetStatus;
    private TabLayout       tabLayout;
    private String          currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        db           = new DatabaseHelper(this);
        currentMonth = Constants.currentMonthString();


        tvMonthTotal   = findViewById(R.id.tvMonthTotal);
        tvMonthLabel   = findViewById(R.id.tvMonthLabel);
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus);
        tabLayout      = findViewById(R.id.tabLayout);


        RecyclerView rv = findViewById(R.id.recyclerExpenses);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter(this, null, this);
        rv.setAdapter(adapter);


        FloatingActionButton fab = findViewById(R.id.fabAddExpense);
        fab.setOnClickListener(v -> openAddExpense());


        tabLayout.addTab(tabLayout.newTab().setText("This Month"));
        tabLayout.addTab(tabLayout.newTab().setText("Today"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { refreshList(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        refreshAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAll();
    }

    private void refreshAll() {
        refreshList();
        refreshSummary();
    }

    private void refreshList() {
        List<Expense> list;
        if (tabLayout.getSelectedTabPosition() == 0) {
            list = db.getExpensesByMonth(currentMonth);
        } else {
            list = db.getExpensesByDate(Constants.todayString());
        }
        adapter.setExpenses(list);


        View emptyView = findViewById(R.id.tvEmptyState);
        emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void refreshSummary() {
        double total = db.getTotalForMonth(currentMonth);
        tvMonthTotal.setText(String.format("₹ %.2f", total));


        Budget budget = db.getBudget("TOTAL", currentMonth);
        if (budget != null && budget.getAmount() > 0) {
            double pct = (total / budget.getAmount()) * 100;
            String status;
            if (pct >= 100) status = String.format("⚠ Over budget! (%.0f%%)", pct);
            else if (pct >= 80) status = String.format(" %.0f%% of budget used", pct);
            else status = String.format(" %.0f%% of ₹%.0f budget used", pct, budget.getAmount());
            tvBudgetStatus.setText(status);
            tvBudgetStatus.setVisibility(View.VISIBLE);
        } else {
            tvBudgetStatus.setVisibility(View.GONE);
        }


        try {
            java.util.Date d = Constants.MONTH_FORMAT.parse(currentMonth);
            tvMonthLabel.setText(d != null ? Constants.DISPLAY_MONTH.format(d) : currentMonth);
        } catch (Exception ignored) {
            tvMonthLabel.setText(currentMonth);
        }
    }


    @Override
    public void onEdit(Expense expense) {
        Intent i = new Intent(this, AddEditExpenseActivity.class);
        i.putExtra(Constants.EXTRA_EXPENSE_ID, expense.getId());
        i.putExtra(Constants.EXTRA_MODE, "edit");
        startActivity(i);
    }

    @Override
    public void onDelete(Expense expense) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Delete \"" + expense.getTitle() + "\"?")
            .setPositiveButton("Delete", (d, w) -> {
                db.deleteExpense(expense.getId());
                refreshAll();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void openAddExpense() {
        Intent i = new Intent(this, AddEditExpenseActivity.class);
        i.putExtra(Constants.EXTRA_MODE, "add");
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_summary) {
            startActivity(new Intent(this, SummaryActivity.class));
            return true;
        } else if (id == R.id.action_budget) {
            startActivity(new Intent(this, BudgetActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
