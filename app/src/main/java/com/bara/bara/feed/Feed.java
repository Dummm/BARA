package com.bara.bara.feed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bara.bara.R;
import com.bara.bara.auth.SplashScreenActivity;
import com.bara.bara.camera.CameraActivity;
import com.bara.bara.model.Post;
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
    private boolean showOnlyFollowedPosts;
    private List<Post> posts;
    private boolean follows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        follows = false;
        Slidr.attach(this);
        showOnlyFollowedPosts = true;
        final Button goToCameraButton = findViewById(R.id.go_to_camera);
        final Button goToCreatePostButton = findViewById(R.id.go_to_create_post);
        final Button logoutButton = findViewById(R.id.logout_feed);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ProgressBar progressCircle = findViewById(R.id.progress_circle);
        posts = new ArrayList<>();
        updateFeed(progressCircle);

        final Button allPostsButton = findViewById(R.id.all_posts);
        allPostsButton.setOnClickListener(v -> {
            showOnlyFollowedPosts = false;
            updateFeed(progressCircle);
        });
        final Button filteredPostsButton = findViewById(R.id.followed_posts);
        filteredPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOnlyFollowedPosts = true;
                updateFeed(progressCircle);
            }
        });


        logoutButton.setOnClickListener(view -> logOut());
        goToCameraButton.setOnClickListener(v -> goToCamera());
        goToCreatePostButton.setOnClickListener(v -> goToCreatePost());
    }

    private void updateFeed(ProgressBar progressCircle) {
        final DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("posts");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (showOnlyFollowedPosts) {
                    populateFeedWithFilteredPosts(dataSnapshot, progressCircle);
                } else {
                    populateFeedWithAllPosts(dataSnapshot, progressCircle);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Feed.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                progressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void populateFeedWithFilteredPosts(@NonNull DataSnapshot dataSnapshot, ProgressBar progressCircle) {
        this.posts = new ArrayList<>();

        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            Post post = postSnapshot.getValue(Post.class);
            checkForFollowedUsers(post);

            progressCircle.setVisibility(View.INVISIBLE);
        }
    }

    private void checkForFollowedUsers(Post post) {
        FirebaseDatabase.getInstance().getReference().child("follower")
                .orderByChild("follower").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            if (postSnapshot.child("following").getValue(String.class).equals(post.getUser().getUuid())) {
                                posts.add(post);
                            }
                        }
                        mAdapter = new ImageAdapter(Feed.this, posts);
                        mRecyclerView.setAdapter(mAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        throw databaseError.toException();
                    }
                });
    }

    private void populateFeedWithAllPosts(@NonNull DataSnapshot dataSnapshot, ProgressBar progressCircle) {
        this.posts = new ArrayList<>();
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            Post post = postSnapshot.getValue(Post.class);
            posts.add(post);
            progressCircle.setVisibility(View.INVISIBLE);
        }

        mAdapter = new ImageAdapter(Feed.this, posts);
        mRecyclerView.setAdapter(mAdapter);
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