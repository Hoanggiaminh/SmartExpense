package com.example.smartexpense;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class VerifyOTPActivity extends AppCompatActivity {

    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5;
    private Button btnVerify;
    private TextView tvResend;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Get email from intent
        email = getIntent().getStringExtra("email");

        initViews();
        setupClickListeners();
        setupOtpInputs();
    }

    private void initViews() {
        etOtp1 = findViewById(R.id.et_otp_1);
        etOtp2 = findViewById(R.id.et_otp_2);
        etOtp3 = findViewById(R.id.et_otp_3);
        etOtp4 = findViewById(R.id.et_otp_4);
        etOtp5 = findViewById(R.id.et_otp_5);
        btnVerify = findViewById(R.id.btn_verify);
        tvResend = findViewById(R.id.tv_resend);
    }

    private void setupClickListeners() {
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = etOtp1.getText().toString() +
                           etOtp2.getText().toString() +
                           etOtp3.getText().toString() +
                           etOtp4.getText().toString() +
                           etOtp5.getText().toString();

                if (otp.length() == 5) {
                    // TODO: Verify OTP
                    // Navigate to ResetPasswordActivity after successful OTP verification
                    Intent intent = new Intent(VerifyOTPActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                }
            }
        });

        tvResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Resend OTP
                // Show toast or confirmation
            }
        });
    }

    private void setupOtpInputs() {
        EditText[] otpInputs = {etOtp1, etOtp2, etOtp3, etOtp4, etOtp5};

        for (int i = 0; i < otpInputs.length; i++) {
            final int currentIndex = i;
            final EditText currentInput = otpInputs[i];

            currentInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1) {
                        // Move to next input
                        if (currentIndex < otpInputs.length - 1) {
                            otpInputs[currentIndex + 1].requestFocus();
                        }
                    } else if (s.length() == 0) {
                        // Move to previous input when deleting
                        if (currentIndex > 0) {
                            otpInputs[currentIndex - 1].requestFocus();
                        }
                    }
                }
            });
        }
    }
}
