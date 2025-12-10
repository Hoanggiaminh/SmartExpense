package com.example.smartexpense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends BaseActivity {

    private MaterialCardView btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        initViews();
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
        // Initialize views here
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    signOut();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void signOut() {
        mAuth.signOut();
        Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

        // Navigate to LoginActivity
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
