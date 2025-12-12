package com.example.smartexpense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.adapters.TransactionAdapter;
import com.example.smartexpense.model.Category;
import com.example.smartexpense.model.Transaction;
import com.example.smartexpense.model.TransactionItem;
import com.example.smartexpense.services.FirebaseService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DayDetailActivity extends AppCompatActivity {

    private TextView tvDate;
    private TextView tvDayIncome;
    private TextView tvDayExpense;
    private TextView tvDayBalance;
    private ImageView btnBack;
    private RecyclerView rvDayTransactions;
    private LinearLayout emptyState;
    private FloatingActionButton fabAddTransaction;

    private FirebaseService firebaseService;
    private TransactionAdapter transactionAdapter;
    private List<TransactionItem> transactionItems;
    private Map<String, Category> categoriesMap;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get selected date from intent
        long dateMillis = getIntent().getLongExtra("selected_date", System.currentTimeMillis());
        selectedDate = Calendar.getInstance();
        selectedDate.setTimeInMillis(dateMillis);

        firebaseService = FirebaseService.getInstance();
        categoriesMap = new HashMap<>();
        transactionItems = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadCategoriesAndTransactions();
        setupClickListeners();
    }

    private void initViews() {
        tvDate = findViewById(R.id.tvDate);
        tvDayIncome = findViewById(R.id.tvDayIncome);
        tvDayExpense = findViewById(R.id.tvDayExpense);
        tvDayBalance = findViewById(R.id.tvDayBalance);
        btnBack = findViewById(R.id.btnBack);
        rvDayTransactions = findViewById(R.id.rvDayTransactions);
        emptyState = findViewById(R.id.emptyState);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);

        // Set date display
        SimpleDateFormat sdf = new SimpleDateFormat("d 'tháng' M yyyy", new Locale("vi", "VN"));
        tvDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(this, transactionItems);
        rvDayTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvDayTransactions.setAdapter(transactionAdapter);

        // Set click listener for transactions
        transactionAdapter.setOnTransactionClickListener(transaction -> {
            Intent intent = new Intent(DayDetailActivity.this, TransactionDetailsActivity.class);
            intent.putExtra("transaction_id", transaction.getId());
            intent.putExtra("transaction_title", transaction.getTitle());
            intent.putExtra("transaction_amount", transaction.getAmount());
            intent.putExtra("transaction_type", transaction.getType());
            intent.putExtra("transaction_category", transaction.getCategoryName());
            intent.putExtra("transaction_date", transaction.getDate().getSeconds());
            startActivity(intent);
        });
    }

    private void loadCategoriesAndTransactions() {
        // Load categories first, then transactions
        firebaseService.getCategoriesByType("income")
                .addOnSuccessListener(incomeCategories -> {
                    for (Category category : incomeCategories) {
                        categoriesMap.put(category.getId(), category);
                    }

                    firebaseService.getCategoriesByType("expense")
                            .addOnSuccessListener(expenseCategories -> {
                                for (Category category : expenseCategories) {
                                    categoriesMap.put(category.getId(), category);
                                }

                                // Now load transactions for the day
                                loadDayTransactions();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi tải danh mục", Toast.LENGTH_SHORT).show();
                                loadDayTransactions();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải danh mục", Toast.LENGTH_SHORT).show();
                    loadDayTransactions();
                });
    }

    private void loadDayTransactions() {
        // Set start and end of day
        Calendar startOfDay = (Calendar) selectedDate.clone();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);

        Calendar endOfDay = (Calendar) selectedDate.clone();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);

        Timestamp startTimestamp = new Timestamp(startOfDay.getTime());
        Timestamp endTimestamp = new Timestamp(endOfDay.getTime());

        firebaseService.getTransactionsBetweenDates(startTimestamp, endTimestamp)
                .addOnSuccessListener(querySnapshot -> {
                    List<Transaction> transactions = new ArrayList<>();
                    // Sử dụng mảng để có thể thay đổi giá trị trong lambda
                    final double[] totals = new double[2]; // [0] = income, [1] = expense

                    querySnapshot.forEach(document -> {
                        Transaction transaction = document.toObject(Transaction.class);
                        transaction.setId(document.getId());
                        transactions.add(transaction);

                        // Calculate totals
                        if ("income".equals(transaction.getType())) {
                            totals[0] += transaction.getAmount();
                        } else if ("expense".equals(transaction.getType())) {
                            totals[1] += transaction.getAmount();
                        }
                    });

                    // Update summary
                    double totalIncome = totals[0];
                    double totalExpense = totals[1];
                    tvDayIncome.setText(formatCurrency(totalIncome));
                    tvDayExpense.setText(formatCurrency(totalExpense));
                    tvDayBalance.setText(formatCurrency(totalIncome - totalExpense));

                    // Convert to transaction items
                    convertToTransactionItems(transactions);

                    // Show/hide empty state
                    if (transactions.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        rvDayTransactions.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        rvDayTransactions.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    emptyState.setVisibility(View.VISIBLE);
                    rvDayTransactions.setVisibility(View.GONE);
                });
    }

    private void convertToTransactionItems(List<Transaction> transactions) {
        transactionItems.clear();

        // Sort transactions by time (newest first)
        transactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        for (Transaction transaction : transactions) {
            // Get category info
            Category category = categoriesMap.get(transaction.getCategoryId());
            String categoryName = category != null ? category.getName() : "Chưa phân loại";
            String categoryIcon = category != null ? category.getIcon() : "ic_other";

            // Create transaction item
            TransactionItem item = new TransactionItem(
                    transaction.getId(),
                    transaction.getTitle(),
                    categoryName,
                    categoryIcon,
                    transaction.getAmount(),
                    transaction.getType(),
                    transaction.getDate()
            );

            // Don't show date headers for single day view
            item.setShowDateHeader(false);

            transactionItems.add(item);
        }

        // Update adapter
        transactionAdapter.updateTransactions(transactionItems);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(DayDetailActivity.this, AddTransactionActivity.class);
            intent.putExtra("selected_date", selectedDate.getTimeInMillis());
            startActivity(intent);
        });
    }

    private String formatCurrency(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        DecimalFormat formatter = new DecimalFormat("$ #,###", symbols);
        if (amount == 0) {
            return "$ 0";
        }
        return formatter.format(amount);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this activity
        loadCategoriesAndTransactions();
    }
}

