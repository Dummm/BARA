package com.bara.bara.feed;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bara.bara.R;
import com.bara.bara.model.Post;
import com.bara.bara.model.User;
import com.bara.bara.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    private Context context;
    Map<String, Set<String>> usersWhoLiked = new HashMap<>();
    private List<Post> posts;


    public ImageAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.post, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Post currentPost = posts.get(position);
        usersWhoLiked.put(currentPost.getId(), new HashSet<>());
        holder.textViewMessage.setText(currentPost.getMessage());
        FirebaseDatabase.getInstance()
                .getReference("users")
                .orderByChild("email")
                .equalTo(currentPost.getEmail())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot usernameSnapshot : dataSnapshot.getChildren()) {
                            final User currentUser = usernameSnapshot.getValue(User.class);
                            final String username = currentUser.getName();
                            holder.setPost(currentPost);
                            holder.setUser(currentUser);
                            addNumberOfLikesForPost(currentPost, holder);

                            holder.textViewUser.setText(username);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        holder.textViewUser.setText("-");
                    }
                });


        holder.textViewUser.setOnClickListener(v -> {
            final Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("POST_EMAIL", currentPost.getUser().getEmail());
            intent.putExtra("USER_ID", currentPost.getUser().getUuid());
            v.getContext().startActivity(intent);
        });
        Picasso.get()
                .load(currentPost.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imageView);
    }

    private void addNumberOfLikesForPost(Post currentPost, @NonNull ImageViewHolder holder) {
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("likes");
        DatabaseReference postRef = dbReference.child(currentPost.getId());
        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int numLikes = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(currentUserId)) {
                        holder.numLikesView.setTextColor(Color.parseColor("#FF9800"));
                        holder.numLikesView.setCompoundDrawableTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
                    }
                    numLikes++;
                }
                holder.numLikesView.setText(String.valueOf(numLikes));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void goToProfile() {
        final Intent intent = new Intent(context, ProfileActivity.class);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }


}
