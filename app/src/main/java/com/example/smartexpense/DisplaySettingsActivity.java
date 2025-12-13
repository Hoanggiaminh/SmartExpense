package com.example.smartexpense;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.card.MaterialCardView;

public class DisplaySettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout btnLightTheme, btnDarkTheme, btnSystemTheme;
    private RadioButton radioLightTheme, radioDarkTheme, radioSystemTheme;
    private MaterialCardView btnLanguageSettings;
    private TextView tvCurrentLanguage;
    private String currentTheme = "light";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_settings);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnLightTheme = findViewById(R.id.btnLightTheme);
        btnDarkTheme = findViewById(R.id.btnDarkTheme);
        btnSystemTheme = findViewById(R.id.btnSystemTheme);
        radioLightTheme = findViewById(R.id.radioLightTheme);
        radioDarkTheme = findViewById(R.id.radioDarkTheme);
        radioSystemTheme = findViewById(R.id.radioSystemTheme);
        btnLanguageSettings = findViewById(R.id.btnLanguageSettings);
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnLightTheme.setOnClickListener(v -> selectTheme("light"));
        btnDarkTheme.setOnClickListener(v -> selectTheme("dark"));
        btnSystemTheme.setOnClickListener(v -> selectTheme("system"));

        btnLanguageSettings.setOnClickListener(v -> showLanguageDialog());
    }

    private void selectTheme(String theme) {
        currentTheme = theme;

        // Update radio buttons
        radioLightTheme.setChecked(theme.equals("light"));
        radioDarkTheme.setChecked(theme.equals("dark"));
        radioSystemTheme.setChecked(theme.equals("system"));

        // Apply theme
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private void showLanguageDialog() {
        String[] languages = {"Tiếng Việt", "English"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ngôn ngữ")
                .setItems(languages, (dialog, which) -> {
                    tvCurrentLanguage.setText(languages[which]);
                    // TODO: Implement language change logic
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}

