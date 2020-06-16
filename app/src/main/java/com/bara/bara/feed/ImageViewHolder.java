package com.bara.bara.feed;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bara.bara.R;
import com.bara.bara.helper.DoubleClickListener;
import com.bara.bara.model.Post;
import com.bara.bara.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ImageViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewMessage;
    public TextView textViewUser;
    public Button numLikesView;
    public ImageView imageView;
    private DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
    private Post clickedPost;
    private User postAuthor;

    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewMessage = itemView.findViewById(R.id.post_message);
        imageView = itemView.findViewById(R.id.image_view_upload);
        numLikesView = itemView.findViewById(R.id.num_likes);
        textViewUser = itemView.findViewById(R.id.post_user);


        imageView.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {

            }

            @Override
            public void onDoubleClick(View v) {
                DatabaseReference likesRef = dbReference.child("likes");
                final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference userLikeRef = likesRef.child(clickedPost.getId()).child(currentUserId);
                userLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            dataSnapshot.getRef().removeValue();
                            numLikesView.setText(String.valueOf(Integer.parseInt(numLikesView.getText().toString()) - 1));
                            numLikesView.setTextColor(Color.parseColor("#FFFFFF"));
                            numLikesView.setCompoundDrawableTintList(null);
                        } else {
                            userLikeRef.setValue("");
                            numLikesView.setText(String.valueOf(Integer.parseInt(numLikesView.getText().toString()) + 1));
                            numLikesView.setTextColor(Color.parseColor("#FF9800"));
                            numLikesView.setCompoundDrawableTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


    }


    public void setPost(Post post) {
        this.clickedPost = post;
    }

    public void setUser(User user) {
        this.postAuthor = user;
    }

}
