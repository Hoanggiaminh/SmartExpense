package com.example.smartexpense;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.adapters.CategoryFilterAdapter;
import com.example.smartexpense.adapters.TransactionAdapter;
import com.example.smartexpense.model.Category;
import com.example.smartexpense.model.Transaction;
import com.example.smartexpense.model.TransactionItem;
import com.example.smartexpense.services.FirebaseService;
import com.example.smartexpense.utils.CurrencyUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
    private TextView tvUserName;
    private TextView tvBalance;
    private TextView tvIncomeThisMonth;
    private TextView tvExpenseThisMonth;
    private TextView tvFilterCategory;
    private ImageView btnFilter;
    private TextView tvEmptyTransactions;

    // Firebase components
    private FirebaseService firebaseService;
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private List<TransactionItem> transactionItems;
    private Map<String, Category> categoriesMap;

    // Filter
    private String currentFilterCategoryId = "all";
    private List<Category> allCategories;
    private boolean categoriesLoaded = false; // Flag để tránh load trùng lặp

    // Store original transactions for filtering
    private List<Transaction> originalTransactions = new ArrayList<>();

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
        loadUserData();
        loadCategoriesAndTransactions();
        setupClickListeners();
    }

    private void initViews() {
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
        rvTransactions = findViewById(R.id.rvTransactions);
        tvUserName = findViewById(R.id.tv_user_name);
        tvBalance = findViewById(R.id.tv_balance);
        tvIncomeThisMonth = findViewById(R.id.tv_income_this_month);
        tvExpenseThisMonth = findViewById(R.id.tv_expense_this_month);
        tvFilterCategory = findViewById(R.id.tv_filter_category);
        btnFilter = findViewById(R.id.btn_filter);
        tvEmptyTransactions = findViewById(R.id.tv_empty_transactions);

        transactionItems = new ArrayList<>();
        categoriesMap = new HashMap<>();
        allCategories = new ArrayList<>();

        // Set default filter text
        tvFilterCategory.setText("Tất cả");
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

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Load user profile data
            firebaseService.getUserProfile(userId)
                    .addOnSuccessListener(user -> {
                        tvUserName.setText(user.getUsername());

                        // Load financial data
                        loadFinancialData(userId);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải thông tin người dùng", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadFinancialData(String userId) {
        firebaseService.getUserBalance(userId)
                .addOnSuccessListener(balance -> {
                    // Format and set balance
                    String formattedBalance = CurrencyUtils.formatCurrency(balance);
                    tvBalance.setText(formattedBalance);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải số dư", Toast.LENGTH_SHORT).show());

        firebaseService.getIncomeThisMonth(userId)
                .addOnSuccessListener(income -> {
                    // Format and set income
                    String formattedIncome = CurrencyUtils.formatCurrency(income);
                    tvIncomeThisMonth.setText(formattedIncome);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải thu nhập tháng này", Toast.LENGTH_SHORT).show());

        firebaseService.getExpenseThisMonth(userId)
                .addOnSuccessListener(expense -> {
                    // Format and set expense
                    String formattedExpense = CurrencyUtils.formatCurrency(expense);
                    tvExpenseThisMonth.setText(formattedExpense);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải chi tiêu tháng này", Toast.LENGTH_SHORT).show());
    }

    private void loadCategoriesAndTransactions() {
        // Load categories first, then transactions
        loadAllCategories();
    }

    private void loadAllCategories() {
        // Nếu đã load categories rồi thì không load lại
        if (categoriesLoaded && !allCategories.isEmpty()) {
            Log.d("MainActivity", "Categories already loaded, skipping...");
            loadTransactions();
            return;
        }

        firebaseService.getAllCategories()
                .addOnSuccessListener(categories -> {
                    allCategories.clear(); // Clear để tránh trùng lặp
                    categoriesMap.clear(); // Clear map cũ

                    // Add all categories
                    allCategories.addAll(categories);
                    for (Category category : categories) {
                        categoriesMap.put(category.getId(), category);
                    }

                    categoriesLoaded = true; // Đánh dấu đã load xong

                    Log.d("MainActivity", "Categories loaded successfully: " + allCategories.size());
                    loadTransactions();
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error loading categories", e);
                    Toast.makeText(this, "Lỗi khi tải danh mục", Toast.LENGTH_SHORT).show();
                    categoriesLoaded = true;
                    loadTransactions();
                });
    }

    private void loadTransactions() {
        firebaseService.getRecentTransactions(50) // Lấy 50 giao dịch gần nhất
                .addOnSuccessListener(querySnapshot -> {
                    List<Transaction> transactions = new ArrayList<>();
                    originalTransactions.clear();

                    querySnapshot.forEach(document -> {
                        Transaction transaction = document.toObject(Transaction.class);
                        transaction.setId(document.getId());
                        transactions.add(transaction);
                        originalTransactions.add(transaction); // Store for filtering
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
            tvEmptyTransactions.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
            return;
        } else {
            tvEmptyTransactions.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
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

        if (isSameDay(cal, today)) {
            return "Hôm nay";
        }

        if (isSameDay(cal, yesterday)) {
            return "Hôm qua";
        }

        // Chỉ hiển thị Thứ nếu cùng tuần
        if (isSameWeek(cal, today)) {
            SimpleDateFormat dayFormat =
                    new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
            return dayFormat.format(date);
        }

        // Ngày cũ
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
        return dateFormat.format(date);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isSameWeek(Calendar c1, Calendar c2) {
        return c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR)
                && c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
    }

    private void setupClickListeners() {
        fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        btnFilter.setOnClickListener(v -> showCategoryFilterDialog());
    }

    private void showCategoryFilterDialog() {
        // Log để debug
        Log.d("MainActivity", "Opening filter dialog - categoriesLoaded: " + categoriesLoaded);
        Log.d("MainActivity", "allCategories size: " + allCategories.size());

        // Nếu categories chưa được load, không mở dialog
        if (!categoriesLoaded || allCategories.isEmpty()) {
            Toast.makeText(this, "Đang tải danh mục, vui lòng thử lại...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_category_filter, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Setup RecyclerView for categories
        RecyclerView rvCategories = dialogView.findViewById(R.id.rv_categories);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Prepare categories list with "Tất cả" option
        List<Category> filterCategories = new ArrayList<>();

        // Add "Tất cả" option at the beginning
        Category allCategory = new Category();
        allCategory.setId("all");
        allCategory.setName("Tất cả");
        allCategory.setIcon("ic_all");
        filterCategories.add(allCategory);

        // Add all other categories (tạo copy để tránh reference)
        for (Category category : allCategories) {
            Category categoryCopy = new Category();
            categoryCopy.setId(category.getId());
            categoryCopy.setName(category.getName());
            categoryCopy.setIcon(category.getIcon());
            categoryCopy.setType(category.getType());
            filterCategories.add(categoryCopy);
        }

        Log.d("MainActivity", "Filter categories size: " + filterCategories.size());

        // Setup adapter
        CategoryFilterAdapter adapter = new CategoryFilterAdapter(this, filterCategories);
        adapter.setSelectedCategoryId(currentFilterCategoryId);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(adapter);

        // Set category selection listener
        adapter.setOnCategorySelectedListener(category -> {
            currentFilterCategoryId = category.getId();
            tvFilterCategory.setText(category.getName());

            // Filter transactions
            filterTransactionsByCategory();

            dialog.dismiss();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void filterTransactionsByCategory() {
        if (originalTransactions.isEmpty()) return;

        if ("all".equals(currentFilterCategoryId)) {
            // Show all transactions - rebuild from original data
            convertToTransactionItems(originalTransactions);
        } else {
            // Filter by selected category
            List<Transaction> filteredTransactions = new ArrayList<>();

            for (Transaction transaction : originalTransactions) {
                if (currentFilterCategoryId.equals(transaction.getCategoryId())) {
                    filteredTransactions.add(transaction);
                }
            }

            // Rebuild transaction items with proper date headers
            convertToTransactionItems(filteredTransactions);
        }
    }

    private Transaction findOriginalTransaction(String transactionId) {
        // This method helps find the original transaction from the filtered list
        // We'll need to store original transactions for filtering
        return originalTransactions.stream()
                .filter(t -> t.getId().equals(transactionId))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload financial data
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadFinancialData(currentUser.getUid());
        }

        // Chỉ refresh transactions, giữ lại filter hiện tại
        if (categoriesLoaded) {
            // Load lại transactions và áp dụng filter hiện tại
            loadTransactionsAndApplyFilter();
        } else {
            loadCategoriesAndTransactions();
        }
    }

    private void loadTransactionsAndApplyFilter() {
        firebaseService.getRecentTransactions(50) // Lấy 50 giao dịch gần nhất
                .addOnSuccessListener(querySnapshot -> {
                    List<Transaction> transactions = new ArrayList<>();
                    originalTransactions.clear();

                    querySnapshot.forEach(document -> {
                        Transaction transaction = document.toObject(Transaction.class);
                        transaction.setId(document.getId());
                        transactions.add(transaction);
                        originalTransactions.add(transaction); // Store for filtering
                    });

                    // Convert to TransactionItems with date grouping
                    convertToTransactionItems(transactions);

                    // Áp dụng lại filter nếu không phải "all"
                    if (!"all".equals(currentFilterCategoryId)) {
                        filterTransactionsByCategory();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
