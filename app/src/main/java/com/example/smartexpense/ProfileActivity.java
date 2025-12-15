package com.example.smartexpense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends BaseActivity {
    private LinearLayout btnFinancialSettings, btnCategoryManagement, btnLogout, btnDeleteAccount;
    private TextView tvUserName, tvUserEmail;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        initViews();
        loadUserInfo();
        setupClickListeners();

        // Initialize bottom navigation
        initBottomNavigation();

        // Get selected tab from intent or default to 3 (Profile)
        int selectedTab = getIntent().getIntExtra("selectedTab", 3);
        updateTabState(selectedTab);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    private void initViews() {
        btnFinancialSettings = findViewById(R.id.btnFinancialSettings);
        btnCategoryManagement = findViewById(R.id.btnCategoryManagement);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
    }

    private void loadUserInfo() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            if (displayName != null && !displayName.isEmpty()) {
                tvUserName.setText(displayName);
            } else {
                tvUserName.setText("Người Dùng");
            }

            if (email != null) {
                tvUserEmail.setText(email);
            }
        }
    }

    private void setupClickListeners() {

        btnFinancialSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FinancialSettingsActivity.class);
            startActivity(intent);
        });

        btnCategoryManagement.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, CategoryManagementActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());

        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showLogoutConfirmDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    // Đăng xuất Firebase
                    mAuth.signOut();

                    // Chuyển về màn hình đăng nhập
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        // Tạo EditText để nhập "YES"
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nhập YES để xác nhận");
        input.setGravity(android.view.Gravity.CENTER);

        // Tạo layout cho EditText với padding
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) (24 * getResources().getDisplayMetrics().density);
        params.leftMargin = margin;
        params.rightMargin = margin;
        input.setLayoutParams(params);
        container.addView(input);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản không?\n\nThao tác này không thể hoàn tác. Vui lòng nhập \"YES\" để xác nhận.")
                .setView(container)
                .setPositiveButton("OK", (dialog, which) -> {
                    String confirmText = input.getText().toString().trim();
                    if (confirmText.equals("YES")) {
                        deleteUserAccount();
                    } else {
                        android.widget.Toast.makeText(ProfileActivity.this,
                                "Vui lòng nhập chính xác \"YES\" để xác nhận xóa tài khoản",
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteUserAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Hiển thị progress dialog
            android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
            progressDialog.setMessage("Đang xóa tài khoản...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Xóa tài khoản
            user.delete()
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            android.widget.Toast.makeText(ProfileActivity.this,
                                    "Tài khoản đã được xóa thành công",
                                    android.widget.Toast.LENGTH_SHORT).show();

                            // Chuyển về màn hình đăng nhập
                            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Nếu xóa thất bại (thường do cần xác thực lại)
                            android.widget.Toast.makeText(ProfileActivity.this,
                                    "Không thể xóa tài khoản. Vui lòng đăng nhập lại và thử lại.",
                                    android.widget.Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
