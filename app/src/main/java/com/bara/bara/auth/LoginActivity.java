package com.bara.bara.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bara.bara.R;
import com.bara.bara.camera.CameraActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuthStateListener = firebaseAuth -> {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                goToMainActivity();
            }
        };

        mAuth = FirebaseAuth.getInstance();

        final Button mLogin = findViewById(R.id.login);
        final EditText mEmail = findViewById(R.id.email);
        final EditText mPassword = findViewById(R.id.password);
        final Button mRegistration = findViewById(R.id.registration);

        mRegistration.setOnClickListener(v ->
                startActivity(
                        new Intent(getApplication(), RegisterActivity.class)
                )
        );
        mLogin.setOnClickListener(view -> {
            final String email = mEmail.getText().toString();
            final String password = mPassword.getText().toString();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, checkSuccess());
        });

    }

    private void goToMainActivity() {
        Intent intent = new Intent(getApplication(), CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @NotNull
    private OnCompleteListener<AuthResult> checkSuccess() {
        return task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Sign in ERROR", Toast.LENGTH_SHORT)
                        .show();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }
}

