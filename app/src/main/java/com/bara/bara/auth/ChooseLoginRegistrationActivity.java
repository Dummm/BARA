package com.bara.bara.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.bara.bara.R;

public class ChooseLoginRegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_registration);

        final Button mLogin = findViewById(R.id.login);
        final Button mRegistration = findViewById(R.id.registration);

        mLogin.setOnClickListener(v -> goToLoginActivity());
        mRegistration.setOnClickListener(v -> goToRegisterActivity());
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(getApplication(), LoginActivity.class);
        startActivity(intent);
    }

    private void goToRegisterActivity() {
        Intent intent = new Intent(getApplication(), RegisterActivity.class);
        startActivity(intent);
    }
}
