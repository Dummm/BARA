package com.bara.bara.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bara.bara.R;
import com.bara.bara.camera.CameraActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEmail, mPassword, mName;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        firebaseAuthStateListener = firebaseAuth -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Intent intent = new Intent(getApplication(), CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        };

        mAuth = FirebaseAuth.getInstance();

        Button mRegister = findViewById(R.id.registration);
        mName = findViewById(R.id.name);
        mEmail = this.findViewById(R.id.email);
        mPassword = findViewById(R.id.password);

        mRegister.setOnClickListener(v -> {
            final String name = mName.getText().toString();
            final String email = mEmail.getText().toString();
            final String password = mPassword.getText().toString();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(RegisterActivity.this, task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplication(),
                                    R.string.register_error_toast, Toast.LENGTH_SHORT).show();
                        } else {
                            final String userId = requireNonNull(mAuth.getCurrentUser()).getUid();
                            final DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                                    .getReference("posts");
                            final DatabaseReference currentUserDb = FirebaseDatabase.getInstance()
                                    .getReference().child("users").child(userId);

                            updateUserInfo(currentUserDb, email, name);
                        }
                    });
        });

    }

    private void updateUserInfo(DatabaseReference currentUserDb, String email, String name) {
        final Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", email);
        userInfo.put("name", name);
        currentUserDb.updateChildren(userInfo);
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
