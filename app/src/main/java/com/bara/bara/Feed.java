package com.bara.bara;

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

    private ImageButton mGoToCreatePost;
    private ImageButton mGoToCamera;
    private ImageButton mLogout;

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;

    private DatabaseReference mDatabaseRef;
    private List<Upload> mUploads;

    private ProgressBar mProgressCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        Slidr.attach(this);

        mGoToCamera = findViewById(R.id.go_to_camera);
        mGoToCreatePost = findViewById(R.id.go_to_create_post);
        mLogout = findViewById(R.id.logout_feed);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);//????????????????? the recycler view doesn't change it's size (aka width, height)
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressCircle = findViewById(R.id.progress_circle);

        mUploads = new ArrayList<>();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("posts");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    mUploads.add(upload);
                    mProgressCircle.setVisibility(View.INVISIBLE);
                }

                mAdapter = new ImageAdapter(Feed.this, mUploads);

                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Feed.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogOut();
            }
        });

        mGoToCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCamera();
            }
        });

        mGoToCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCreatePost();
            }
        });
    }

    private void LogOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplication(), SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return;
    }

    private void goToCamera()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void goToCreatePost()
    {
        Intent intent = new Intent(this, CreatePost.class);
        startActivity(intent);
    }

}