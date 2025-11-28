package com.example.smartexpense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends BaseActivity {

    LinearLayout transactionItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupClickListeners();

        // Initialize bottom navigation
        initBottomNavigation();

        // Get selected tab from intent or default to 0 (Home)
        int selectedTab = getIntent().getIntExtra("selectedTab", 0);
        updateTabState(selectedTab);
    }

    private void initViews() {
        transactionItem = findViewById(R.id.transactionItem);
    }

    private void setupClickListeners() {
        transactionItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransactionDetailsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}