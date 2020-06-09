package com.bara.bara.feed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bara.bara.R;
import com.bara.bara.auth.SplashScreenActivity;
import com.bara.bara.camera.CameraActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.List;

public class Feed extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        Slidr.attach(this);

        final Button goToCameraButton = findViewById(R.id.go_to_camera);
        final Button goToCreatePostButton = findViewById(R.id.go_to_create_post);
        final Button logoutButton = findViewById(R.id.logout_feed);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ProgressBar progressCircle = findViewById(R.id.progress_circle);
        final List<Upload> uploads = new ArrayList<>();

        final DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("posts");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    uploads.add(upload);
                    progressCircle.setVisibility(View.INVISIBLE);
                }

                mAdapter = new ImageAdapter(Feed.this, uploads);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Feed.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                progressCircle.setVisibility(View.INVISIBLE);
            }
        });

        logoutButton.setOnClickListener(view -> logOut());
        goToCameraButton.setOnClickListener(v -> goToCamera());
        goToCreatePostButton.setOnClickListener(v -> goToCreatePost());
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        final Intent intent = new Intent(getApplication(), SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void goToCamera() {
        final Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    private void goToCreatePost() {
        final Intent intent = new Intent(this, CreatePost.class);
        startActivity(intent);
    }

}