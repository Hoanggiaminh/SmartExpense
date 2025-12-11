package com.example.smartexpense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.adapters.TransactionAdapter;
import com.example.smartexpense.model.Category;
import com.example.smartexpense.model.Transaction;
import com.example.smartexpense.model.TransactionItem;
import com.example.smartexpense.services.FirebaseService;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddTransactionActivity extends AppCompatActivity {

    private ImageView btnBack, iconIncome, iconExpense;
    private TextView txtIncome, txtExpense;
    private MaterialCardView cardAddIncome, cardAddExpense;
    private RecyclerView rvLatestEntries;
    private FloatingActionButton fabAdd;

    // Firebase components
    private FirebaseService firebaseService;
    private TransactionAdapter transactionAdapter;
    private List<TransactionItem> transactionItems;
    private Map<String, Category> categoriesMap;

    // Selected transaction type: 0 = Income, 1 = Expense
    private int selectedTransactionType = 0; // Default to Income

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadCategoriesAndTransactions();

        // Set initial state - Income selected by default
        updateCardStates();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        iconIncome = findViewById(R.id.iconIncome);
        iconExpense = findViewById(R.id.iconExpense);
        txtIncome = findViewById(R.id.txtIncome);
        txtExpense = findViewById(R.id.txtExpense);
        cardAddIncome = findViewById(R.id.cardAddIncome);
        cardAddExpense = findViewById(R.id.cardAddExpense);
        rvLatestEntries = findViewById(R.id.rvLatestEntries);
        fabAdd = findViewById(R.id.fabAdd);

        firebaseService = FirebaseService.getInstance();
        transactionItems = new ArrayList<>();
        categoriesMap = new HashMap<>();
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(this, transactionItems);
        rvLatestEntries.setLayoutManager(new LinearLayoutManager(this));
        rvLatestEntries.setAdapter(transactionAdapter);
        rvLatestEntries.setNestedScrollingEnabled(false);

        // Set click listener for transactions
        transactionAdapter.setOnTransactionClickListener(transaction -> {
            Intent intent = new Intent(AddTransactionActivity.this, TransactionDetailsActivity.class);
            intent.putExtra("transaction_id", transaction.getId());
            intent.putExtra("transaction_title", transaction.getTitle());
            intent.putExtra("transaction_amount", transaction.getAmount());
            intent.putExtra("transaction_type", transaction.getType());
            intent.putExtra("transaction_category", transaction.getCategoryName());
            intent.putExtra("transaction_date", transaction.getDate().getSeconds());
            startActivity(intent);
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Card clicks only change selection state
        cardAddIncome.setOnClickListener(v -> {
            selectedTransactionType = 0; // Income
            updateCardStates();
        });

        cardAddExpense.setOnClickListener(v -> {
            selectedTransactionType = 1; // Expense
            updateCardStates();
        });

        // FAB navigates based on selected card
        fabAdd.setOnClickListener(v -> {
            if (selectedTransactionType == 0) {
                // Navigate to Add Income
                startActivity(new Intent(this, AddIncomeActivity.class));
            } else {
                // Navigate to Add Expense
                startActivity(new Intent(this, AddExpenseActivity.class));
            }
        });
    }

    private void updateCardStates() {
        if (selectedTransactionType == 0) {
            // Income selected
            cardAddIncome.setCardBackgroundColor(getResources().getColor(R.color.primary));
            cardAddIncome.setCardElevation(8f);
            txtIncome.setTextColor(getResources().getColor(R.color.white));
            iconIncome.setColorFilter(getResources().getColor(R.color.white));

            cardAddExpense.setCardBackgroundColor(getResources().getColor(R.color.white));
            cardAddExpense.setCardElevation(2f);
            txtExpense.setTextColor(getResources().getColor(R.color.black));
            iconExpense.setColorFilter(getResources().getColor(R.color.black));
        } else {
            // Expense selected
            cardAddExpense.setCardBackgroundColor(getResources().getColor(R.color.primary));
            cardAddExpense.setCardElevation(8f);
            txtExpense.setTextColor(getResources().getColor(R.color.white));
            iconExpense.setColorFilter(getResources().getColor(R.color.white));

            cardAddIncome.setCardBackgroundColor(getResources().getColor(R.color.white));
            cardAddIncome.setCardElevation(2f);
            txtIncome.setTextColor(getResources().getColor(R.color.black));
            iconIncome.setColorFilter(getResources().getColor(R.color.black));
        }
    }

    private void loadCategoriesAndTransactions() {
        loadAllCategories();
    }

    private void loadAllCategories() {
        // Load both income and expense categories
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

                                // Now load transactions
                                loadTransactions();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi tải danh mục chi tiêu", Toast.LENGTH_SHORT).show();
                                loadTransactions(); // Load transactions anyway
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải danh mục thu nhập", Toast.LENGTH_SHORT).show();
                    loadTransactions(); // Load transactions anyway
                });
    }

    private void loadTransactions() {
        firebaseService.getRecentTransactions(20) // Lấy 20 giao dịch gần nhất
                .addOnSuccessListener(querySnapshot -> {
                    List<Transaction> transactions = new ArrayList<>();

                    querySnapshot.forEach(document -> {
                        Transaction transaction = document.toObject(Transaction.class);
                        transaction.setId(document.getId());
                        transactions.add(transaction);
                    });

                    // Convert to TransactionItems with date grouping
                    convertToTransactionItems(transactions);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void convertToTransactionItems(List<Transaction> transactions) {
        transactionItems.clear();

        if (transactions.isEmpty()) {
            transactionAdapter.updateTransactions(transactionItems);
            return;
        }

        // Sort transactions by date (newest first)
        transactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        String currentDateGroup = "";

        for (Transaction transaction : transactions) {
            // Get category info
            Category category = categoriesMap.get(transaction.getCategoryId());
            String categoryName = category != null ? category.getName() : "Chưa phân loại";
            String categoryIcon = category != null ? category.getIcon() : "ic_other";

            // Format date group
            String dateGroup = formatDateGroup(transaction.getDate().toDate());

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

            // Show date header for new date group
            if (!dateGroup.equals(currentDateGroup)) {
                item.setShowDateHeader(true);
                item.setDateHeaderText(dateGroup);
                currentDateGroup = dateGroup;
            }

            transactionItems.add(item);
        }

        // Update adapter
        transactionAdapter.updateTransactions(transactionItems);
    }

    private String formatDateGroup(Date date) {
        Calendar cal = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        cal.setTime(date);

        // Check if it's today
        if (isSameDay(cal, today)) {
            return "Hôm nay";
        }

        // Check if it's yesterday
        if (isSameDay(cal, yesterday)) {
            return "Hôm qua";
        }

        // Check if it's within this week
        long diffInDays = (today.getTimeInMillis() - cal.getTimeInMillis()) / (1000 * 60 * 60 * 24);
        if (diffInDays <= 7) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
            return dayFormat.format(date);
        }

        // Show full date for older transactions
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM", new Locale("vi", "VN"));
        return dateFormat.format(date);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadCategoriesAndTransactions();
    }
}
