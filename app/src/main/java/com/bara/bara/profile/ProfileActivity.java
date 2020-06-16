package com.bara.bara.profile;


import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bara.bara.R;
import com.bara.bara.feed.ImageAdapter;
import com.bara.bara.model.Post;
import com.bara.bara.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private Button followButton;
    private String currentUserUUID;
    private String profileUserUUID;
    private DatabaseReference dbReference;
    private TextView followersTextView;
    private TextView followingsTextView;
    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        dbReference = FirebaseDatabase.getInstance().getReference().child("follower");
        final TextView nameTextView = (TextView) findViewById(R.id.name);
        followButton = (Button) findViewById(R.id.btnFollow);
        final Bundle extras = getIntent().getExtras();
        profileUserUUID = extras.getString("USER_ID");
        followersTextView = findViewById(R.id.tv1);
        followingsTextView = findViewById(R.id.tv2);
        currentUserUUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final ProgressBar progressCircle = findViewById(R.id.profile_progress_circle);

        updateFeed(progressCircle);
        mRecyclerView = findViewById(R.id.profile_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        getUsernameForProfile(nameTextView);

        checkForAutoFollow();

        followButton.setOnClickListener(v -> createFollowerEntry(dbReference, profileUserUUID));

        toggleFollowing();

        updateFollowersNumber();
    }

    private void updateFollowersNumber() {
        dbReference.orderByChild("follower")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        computeNumberOfFollowersAndFollowings(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        throw databaseError.toException();
                    }
                });
    }

    private void updateFeed(ProgressBar progressCircle) {
        final DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("posts");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                populateFeedWithAllPosts(dataSnapshot, progressCircle);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                progressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void populateFeedWithAllPosts(@NonNull DataSnapshot dataSnapshot, ProgressBar progressCircle) {
        List<Post> posts = new ArrayList<>();
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            Post post = postSnapshot.getValue(Post.class);
            if (post.getUser().getUuid().equals(profileUserUUID)) {
                posts.add(post);
            }
            progressCircle.setVisibility(View.INVISIBLE);
        }

        mAdapter = new ImageAdapter(ProfileActivity.this, posts);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void toggleFollowing() {
        dbReference.orderByChild("following").equalTo(profileUserUUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        changeFollowButtonStyle(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        throw databaseError.toException();
                    }
                });
    }

    private void checkForAutoFollow() {
        if (currentUserUUID.equals(profileUserUUID)) {
            setAutoFollowButtonStyle();
        } else {
            followButton.setText("Follow me");
            followButton.setBackgroundColor(Color.parseColor("#FF9800"));
        }
    }

    private void getUsernameForProfile(TextView nameTextView) {
        FirebaseDatabase.getInstance().getReference().child("users").child(profileUserUUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        setUsername(dataSnapshot, nameTextView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        nameTextView.setText('-');
                    }
                });
    }

    private void setUsername(@NonNull DataSnapshot dataSnapshot, TextView nameTextView) {
        String username = dataSnapshot.getValue(User.class).getName();
        nameTextView.setText(username);
    }

    private void computeNumberOfFollowersAndFollowings(DataSnapshot dataSnapshot) {
        int numFollowings = 0;
        int numFollowers = 0;
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            if (snapshot.child("follower").getValue(String.class).equals(profileUserUUID)) {
                numFollowings++;
            } else if (snapshot.child("following").getValue(String.class).equals(profileUserUUID)) {
                numFollowers++;
            }
        }

        followersTextView.setText(String.valueOf(numFollowers));
        followingsTextView.setText(String.valueOf(numFollowings));
    }

    private void setAutoFollowButtonStyle() {
        followButton.setText("Own Profile");
        followButton.setEnabled(false);
    }

    private void changeFollowButtonStyle(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String currentFollowedUserUUID = snapshot.child("follower").getValue(String.class);
            if (currentFollowedUserUUID.equals(this.currentUserUUID)) {
                followButton.setText("Unfollow");
                followButton.setBackgroundColor(Color.parseColor("#EF0909"));
                followButton.setOnClickListener(v -> getVoidTask(snapshot));
            }
        }
    }

    @NotNull
    private Task<Void> getVoidTask(DataSnapshot snapshot) {
        followButton.setText("Follow me");
        followButton.setBackgroundColor(Color.parseColor("#FF9800"));
        followButton.setOnClickListener(v -> createFollowerEntry(dbReference, profileUserUUID));
        return snapshot.getRef().removeValue();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        followButton.setOnClickListener(null);
    }

    private void createFollowerEntry(DatabaseReference dbReference, String userId) {
        DatabaseReference pushedPostRef = dbReference.push();
        Map<String, Object> followMap = new HashMap<>();
        followMap.put("follower", FirebaseAuth.getInstance().getCurrentUser().getUid());
        followMap.put("following", userId);
        pushedPostRef.updateChildren(followMap);
    }
}

