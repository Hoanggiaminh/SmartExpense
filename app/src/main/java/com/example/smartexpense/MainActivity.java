package com.example.smartexpense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends BaseActivity {
    private FloatingActionButton fabAddTransaction;
    private FirebaseAuth mAuth;

    // Firebase components
    private FirebaseService firebaseService;
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private List<TransactionItem> transactionItems;
    private Map<String, Category> categoriesMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        firebaseService = FirebaseService.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize bottom navigation and set Home tab as active
        initBottomNavigation();
        updateTabState(0);

        initViews();
        setupTransactionRecyclerView();
        loadCategoriesAndTransactions();
        setupClickListeners();
    }

    private void initViews() {
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
        rvTransactions = findViewById(R.id.rvTransactions);

        transactionItems = new ArrayList<>();
        categoriesMap = new HashMap<>();
    }

    private void setupTransactionRecyclerView() {
        transactionAdapter = new TransactionAdapter(this, transactionItems);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);
        rvTransactions.setNestedScrollingEnabled(false);

        // Set click listener for transactions
        transactionAdapter.setOnTransactionClickListener(transaction -> {
            Intent intent = new Intent(MainActivity.this, TransactionDetailsActivity.class);
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
        firebaseService.getRecentTransactions(50) // Lấy 50 giao dịch gần nhất
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
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

    private void setupClickListeners() {
        fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadCategoriesAndTransactions();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not signed in, redirect to login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
