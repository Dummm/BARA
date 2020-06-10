package com.bara.bara.profile;


import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bara.bara.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.r0adkll.slidr.Slidr;

import static java.util.Objects.requireNonNull;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        DatabaseReference dbReference;
        dbReference = FirebaseDatabase.getInstance().getReference().child("follower");
        final TextView NameTextHolder = (TextView)findViewById(R.id.name);
        final Button FollowButton =(Button)findViewById(R.id.btnFollow);
        Bundle extras= getIntent().getExtras();
        String mail = extras.getString("POST_EMAIL");
        ((TextView) findViewById(R.id.email)).setText(mail);

        FirebaseDatabase.getInstance()
            .getReference("users")
            .orderByChild("email")
            .equalTo(mail)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot usernameSnapshot : dataSnapshot.getChildren()) {
//                        Log.i(ImageAdapter.class.getSimpleName(), "asdf:" + usernameSnapshot.getValue(UserData.class)));
                        String username = usernameSnapshot.getValue(UserData.class).getName();
                        NameTextHolder.setText(username);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    NameTextHolder.setText("-");
                }
            });

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();


        dbReference
            .orderByChild("follower")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long follows = 0;
                    long followers = 0;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("follower").getValue(String.class).equals(mail)) {
                            follows++;
                        } else if (snapshot.child("following").getValue(String.class).equals(mail)) {
                            followers++;
                        }
                    }

                    ((TextView) findViewById(R.id.tv1)).setText("" + followers);
                    ((TextView) findViewById(R.id.tv2)).setText("" + follows);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw databaseError.toException();
                }
            });

        //TODO THIS IS BAD NEEDS REFACTORING
        // Check if user is on his own profile
        Query query= dbReference.orderByChild("follower").equalTo(currentUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            //Check the relation between the user and the profile selected {Own Profile, Followed Profile, Profile}
                if(dataSnapshot.exists()) {
                    //Followed Profile
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("following").getValue(String.class).equals(mail))
                        {
                            FollowButton.setText("Unfollow");
                            FollowButton.setBackgroundColor(Color.parseColor("#EF0909"));
                            FollowButton.setOnClickListener(v-> dataSnapshot.getRef().removeValue());
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
        //Own Profile
         if(currentUser.getEmail().equals(mail)) {
            FollowButton.setText("Own Profile");
            FollowButton.setEnabled(false);
        }
        // Profile
        else {
            FollowButton.setText("Follow User");
            FollowButton.setBackgroundColor(Color.parseColor("#FF9800"));
            FollowButton.setOnClickListener(v->{
                final String key = requireNonNull(dbReference.push().getKey(),
                        "Database reference key is null.");
                dbReference.child(key).setValue(new Follower(currentUser.getEmail(),mail));
            });
        }
    }
    }

