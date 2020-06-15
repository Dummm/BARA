package com.bara.bara.feed;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bara.bara.R;
import com.bara.bara.helper.DoubleClickListener;
import com.bara.bara.model.Post;
import com.bara.bara.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Set;

public class ImageViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewMessage;
    public TextView textViewUser;
    public TextView numLikesView;
    public ImageView imageView;
    private DatabaseReference dbReference;
    private Post clickedPost;
    private User postAuthor;
    private Set<String> usersWhoLiked;

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

                DatabaseReference dbReference = FirebaseDatabase.getInstance()
                        .getReference("posts")
                        .child(clickedPost.getId())
                        .child("likes");
                final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (!usersWhoLiked.contains(currentUserId)) {
                    final String newLikeId = dbReference.push().getKey();
                    dbReference.child(newLikeId).setValue(currentUserId);
                } else {
                    dbReference.orderByValue().equalTo(currentUserId).getRef().removeValue();
                }

            }
        });

    }

    public void setUsersWhoLiked(Set<String> usersWhoLiked) {
        this.usersWhoLiked = usersWhoLiked;
    }

    public void setPost(Post post) {
        this.clickedPost = post;
    }

    public void setUser(User user) {
        this.postAuthor = user;
    }

}
