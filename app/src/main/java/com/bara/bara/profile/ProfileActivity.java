package com.bara.bara.profile;


import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        final TextView UserNameTextHolder = (TextView)findViewById(R.id.User_Name);
        final Button FollowButton =(Button)findViewById(R.id.btnFollow);
        Bundle extras= getIntent().getExtras();
        String UserName=extras.getString("POST_EMAIL");
        UserNameTextHolder.setText(UserName);
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();

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
                        if (snapshot.child("following").getValue(String.class).equals(UserName))
                        {
                            Toast.makeText(getApplicationContext(), "asdf " + dataSnapshot.getChildren().toString(), Toast.LENGTH_LONG).show();
                            FollowButton.setText("Unfollow");
                            FollowButton.setBackgroundColor(Color.RED);
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
         if(currentUser.getEmail().equals(UserName)) {
            FollowButton.setText("Own Profile");
            FollowButton.setEnabled(false);
        }
        // Profile
        else {
            FollowButton.setText("Follow User");
            FollowButton.setBackgroundColor(Color.GREEN);
            FollowButton.setOnClickListener(v->{
                final String key = requireNonNull(dbReference.push().getKey(),
                        "Database reference key is null.");
                dbReference.child(key).setValue(new Follower(currentUser.getEmail(),UserName));
            });
        }
    }
    }

