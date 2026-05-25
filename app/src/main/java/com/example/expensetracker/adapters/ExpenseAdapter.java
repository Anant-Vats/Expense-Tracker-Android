package com.example.expensetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.utils.Constants;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    public interface OnExpenseClickListener {
        void onEdit(Expense expense);
        void onDelete(Expense expense);
    }

    private final Context                context;
    private       List<Expense>          expenses;
    private final OnExpenseClickListener listener;

    public ExpenseAdapter(Context context, List<Expense> expenses, OnExpenseClickListener listener) {
        this.context  = context;
        this.expenses = expenses;
        this.listener = listener;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Expense e = expenses.get(position);

        h.tvIcon.setText(Constants.iconForCategory(e.getCategory()));
        h.tvTitle.setText(e.getTitle());
        h.tvCategory.setText(e.getCategory());
        h.tvAmount.setText(String.format("₹ %.2f", e.getAmount()));
        h.tvDate.setText(formatDisplayDate(e.getDate()));

        h.btnEdit.setOnClickListener(v -> listener.onEdit(e));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(e));
    }

    @Override
    public int getItemCount() { return expenses == null ? 0 : expenses.size(); }

    private String formatDisplayDate(String raw) {
        try {
            Date d = Constants.DATE_FORMAT.parse(raw);
            return d != null ? Constants.DISPLAY_DATE.format(d) : raw;
        } catch (ParseException ex) {
            return raw;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle, tvCategory, tvAmount, tvDate;
        TextView btnEdit, btnDelete;

        ViewHolder(View v) {
            super(v);
            tvIcon     = v.findViewById(R.id.tvExpenseIcon);
            tvTitle    = v.findViewById(R.id.tvExpenseTitle);
            tvCategory = v.findViewById(R.id.tvExpenseCategory);
            tvAmount   = v.findViewById(R.id.tvExpenseAmount);
            tvDate     = v.findViewById(R.id.tvExpenseDate);
            btnEdit    = v.findViewById(R.id.btnEdit);
            btnDelete  = v.findViewById(R.id.btnDelete);
        }
    }
}
