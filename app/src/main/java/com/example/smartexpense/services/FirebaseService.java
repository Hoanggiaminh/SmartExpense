package com.example.smartexpense.services;

import com.example.smartexpense.model.Category;
import com.example.smartexpense.model.Transaction;
import com.example.smartexpense.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseService {
    private static FirebaseService instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private FirebaseService() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    // User Profile methods
    public Task<User> getUserProfile(String userId) {
        return db.collection("users")
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        User user = document.toObject(User.class);
                        if (user == null) {
                            // Create default user if not exists
                            user = new User();
                            user.setUid(userId);
                            user.setUsername(auth.getCurrentUser() != null ?
                                auth.getCurrentUser().getDisplayName() : "Người dùng");
                            user.setEmail(auth.getCurrentUser() != null ?
                                auth.getCurrentUser().getEmail() : "");
                        }
                        return user;
                    } else {
                        // Return default user
                        User defaultUser = new User();
                        defaultUser.setUid(userId);
                        defaultUser.setUsername(auth.getCurrentUser() != null ?
                            auth.getCurrentUser().getDisplayName() : "Người dùng");
                        defaultUser.setEmail(auth.getCurrentUser() != null ?
                            auth.getCurrentUser().getEmail() : "");
                        return defaultUser;
                    }
                });
    }

    // Financial calculation methods
    public Task<Double> getUserBalance(String userId) {
        return db.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .continueWith(task -> {
                    double balance = 0.0;
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (DocumentSnapshot document : querySnapshot) {
                            Transaction transaction = document.toObject(Transaction.class);
                            if (transaction != null) {
                                if ("income".equals(transaction.getType())) {
                                    balance += transaction.getAmount();
                                } else if ("expense".equals(transaction.getType())) {
                                    balance -= transaction.getAmount();
                                }
                            }
                        }
                    }
                    return balance;
                });
    }

    public Task<Double> getIncomeThisMonth(String userId) {
        return getAmountByTypeAndPeriod(userId, "income", getCurrentMonthRange());
    }

    public Task<Double> getExpenseThisMonth(String userId) {
        return getAmountByTypeAndPeriod(userId, "expense", getCurrentMonthRange());
    }

    private Task<Double> getAmountByTypeAndPeriod(String userId, String type, Timestamp[] dateRange) {
        Log.d("FirebaseService", "Calculating " + type + " from " + dateRange[0].toDate() + " to " + dateRange[1].toDate());

        return db.collection("users")
                .document(userId)
                .collection("transactions")
                .whereEqualTo("type", type)
                .whereGreaterThanOrEqualTo("date", dateRange[0])
                .whereLessThanOrEqualTo("date", dateRange[1])
                .get()
                .continueWith(task -> {
                    double total = 0.0;
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        Log.d("FirebaseService", "Found " + querySnapshot.size() + " transactions for " + type);

                        for (DocumentSnapshot document : querySnapshot) {
                            Transaction transaction = document.toObject(Transaction.class);
                            if (transaction != null) {
                                Log.d("FirebaseService", "Transaction: " + transaction.getTitle() + " - " + transaction.getAmount() + " - " + transaction.getDate().toDate());
                                total += transaction.getAmount();
                            }
                        }
                        Log.d("FirebaseService", "Total " + type + ": " + total);
                    } else {
                        Log.e("FirebaseService", "Error getting transactions", task.getException());
                    }
                    return total;
                });
    }

    private Timestamp[] getCurrentMonthRange() {
        Calendar calendar = Calendar.getInstance();

        // Start of current month (ngày 1, 00:00:00)
        Calendar startCal = (Calendar) calendar.clone();
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        Timestamp startOfMonth = new Timestamp(startCal.getTime());

        // End of current month (ngày cuối tháng, 23:59:59)
        Calendar endCal = (Calendar) calendar.clone();
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Timestamp endOfMonth = new Timestamp(endCal.getTime());

        return new Timestamp[]{startOfMonth, endOfMonth};
    }

    // Transaction methods - Sử dụng subcollection users/{uid}/transactions/
    public Task<DocumentReference> addTransaction(Transaction transaction) {
        String userId = getCurrentUserId();
        if (userId == null) return null;

        return db.collection("users")
                .document(userId)
                .collection("transactions")
                .add(transaction);
    }

    public Task<QuerySnapshot> getUserTransactions() {
        String userId = getCurrentUserId();
        if (userId == null) return null;

        return db.collection("users")
                .document(userId)
                .collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .get();
    }

    public Task<QuerySnapshot> getRecentTransactions(int limit) {
        String userId = getCurrentUserId();
        if (userId == null) return null;

        return db.collection("users")
                .document(userId)
                .collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }

    // Lấy chi tiết một transaction theo ID
    public Task<DocumentSnapshot> getTransactionById(String transactionId) {
        String userId = getCurrentUserId();
        if (userId == null) return null;

        return db.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transactionId)
                .get();
    }

    // Cập nhật transaction
    public Task<Void> updateTransaction(String transactionId, String title, double amount,
                                      String type, String categoryId, Timestamp date) {
        String userId = getCurrentUserId();
        if (userId == null) return null;

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("amount", amount);
        updates.put("type", type);
        updates.put("categoryId", categoryId);
        updates.put("date", date);
        updates.put("updatedAt", Timestamp.now());

        return db.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transactionId)
                .update(updates);
    }

    // Xóa transaction
    public Task<Void> deleteTransaction(String transactionId) {
        String userId = getCurrentUserId();
        if (userId == null) return null;

        return db.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transactionId)
                .delete();
    }

    // Category methods - Load cả danh mục mặc định và danh mục user
    public Task<List<Category>> getCategoriesByType(String type) {
        // Tạo task để load cả 2 loại danh mục
        Task<QuerySnapshot> defaultCategoriesTask = getDefaultCategories(type);
        Task<QuerySnapshot> userCategoriesTask = getUserCategories(type);

        return Tasks.whenAllSuccess(defaultCategoriesTask, userCategoriesTask)
                .continueWith(task -> {
                    List<Category> allCategories = new ArrayList<>();

                    // Thêm danh mục mặc định
                    QuerySnapshot defaultResult = (QuerySnapshot) task.getResult().get(0);
                    defaultResult.forEach(document -> {
                        Category category = document.toObject(Category.class);
                        category.setId(document.getId()); // Sử dụng document ID
                        allCategories.add(category);
                    });

                    // Thêm danh mục user
                    QuerySnapshot userResult = (QuerySnapshot) task.getResult().get(1);
                    userResult.forEach(document -> {
                        Category category = document.toObject(Category.class);
                        category.setId(document.getId()); // Sử dụng document ID
                        allCategories.add(category);
                    });

                    return allCategories;
                });
    }

    // Lấy danh mục mặc định từ collection "default_categories"
    private Task<QuerySnapshot> getDefaultCategories(String type) {
        return db.collection("default_categories")
                .whereEqualTo("type", type)
                .get();
    }

    // Lấy danh mục user từ subcollection trong user document
    private Task<QuerySnapshot> getUserCategories(String type) {
        String userId = getCurrentUserId();
        if (userId == null) return Tasks.forResult(null);

        return db.collection("users")
                .document(userId)
                .collection("categories")
                .whereEqualTo("type", type)
                .get();
    }

    // Thêm danh mục mới vào subcollection của user
    public Task<DocumentReference> addUserCategory(Category category) {
        String userId = getCurrentUserId();
        if (userId == null) return null;

        return db.collection("users")
                .document(userId)
                .collection("categories")
                .add(category);
    }

    // Tạo danh mục mặc định
    public void createDefaultCategories() {
        // Default income categories
        List<Category> incomeCategories = Arrays.asList(
            new Category(null, "Lương", "ic_salary", "income"),
            new Category(null, "Thưởng", "ic_bonus", "income"),
            new Category(null, "Đầu tư", "ic_investment", "income"),
            new Category(null, "Khác (thu)", "ic_other", "income")
        );

        // Default expense categories
        List<Category> expenseCategories = Arrays.asList(
            new Category(null, "Ăn uống", "ic_food", "expense"),
            new Category(null, "Di chuyển", "ic_transport", "expense"),
            new Category(null, "Mua sắm", "ic_shopping", "expense"),
            new Category(null, "Giải trí", "ic_entertainment", "expense"),
            new Category(null, "Y tế", "ic_health", "expense"),
            new Category(null, "Giáo dục", "ic_education", "expense"),
            new Category(null, "Hóa đơn", "ic_bills", "expense"),
            new Category(null, "Khác (chi)", "ic_other", "expense")
        );

        // Add all default categories to "default_categories" collection
        CollectionReference defaultCategoriesRef = db.collection("default_categories");

        for (Category category : incomeCategories) {
            defaultCategoriesRef.add(category);
        }

        for (Category category : expenseCategories) {
            defaultCategoriesRef.add(category);
        }
    }

    // Method to get all categories (both income and expense) in one call
    public Task<List<Category>> getAllCategories() {
        Task<QuerySnapshot> defaultCategoriesTask = db.collection("default_categories").get();

        String userId = getCurrentUserId();
        Task<QuerySnapshot> userCategoriesTask;
        if (userId != null) {
            userCategoriesTask = db.collection("users")
                    .document(userId)
                    .collection("categories")
                    .get();
        } else {
            userCategoriesTask = Tasks.forResult(null);
        }

        return Tasks.whenAllSuccess(defaultCategoriesTask, userCategoriesTask)
                .continueWith(task -> {
                    List<Category> allCategories = new ArrayList<>();

                    // Add default categories
                    QuerySnapshot defaultResult = (QuerySnapshot) task.getResult().get(0);
                    if (defaultResult != null) {
                        defaultResult.forEach(document -> {
                            Category category = document.toObject(Category.class);
                            category.setId(document.getId());
                            allCategories.add(category);
                        });
                    }

                    // Add user categories
                    QuerySnapshot userResult = (QuerySnapshot) task.getResult().get(1);
                    if (userResult != null) {
                        userResult.forEach(document -> {
                            Category category = document.toObject(Category.class);
                            category.setId(document.getId());
                            allCategories.add(category);
                        });
                    }

                    Log.d("FirebaseService", "Total categories loaded: " + allCategories.size());
                    return allCategories;
                });
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}
