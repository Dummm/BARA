package com.bara.bara.profile;


import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bara.bara.R;
import com.bara.bara.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private Button followButton;
    private String currentUserUUID;
    private String profileUserUUID;
    private DatabaseReference dbReference;
    private TextView followersTextView;
    private TextView followingsTextView;

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

