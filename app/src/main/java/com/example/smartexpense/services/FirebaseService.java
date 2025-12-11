package com.example.smartexpense.services;

import com.example.smartexpense.model.Category;
import com.example.smartexpense.model.Transaction;
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

import java.util.ArrayList;
import java.util.Arrays;
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
            new Category(null, "Khác", "ic_other", "income")
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
            new Category(null, "Khác", "ic_other", "expense")
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

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}
