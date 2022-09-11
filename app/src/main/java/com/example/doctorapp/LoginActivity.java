package com.example.doctorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.doctorapp.utils.SharedPrefUtils;

public class LoginActivity extends AppCompatActivity {

    TextView email;
    TextView password;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        progressBar = findViewById(R.id.loading);

        progressBar.setVisibility(View.INVISIBLE);
    }

    public void loginClicked(View view) {
        progressBar.setVisibility(View.VISIBLE);
        String _name = email.getText().toString().replaceAll("\\s","");
        String _password = password.getText().toString();

        // Save user name in shared preference and forward to main activity
        SharedPrefUtils.saveUserInSP(_name, LoginActivity.this);
        startActivity(new Intent(this, MainActivity.class));
    }

    public void signUpClicked(View view) {
        startActivity(new Intent(this, SignUpActivity.class));
    }
}