package com.bara.bara.feed;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bara.bara.R;
import com.bara.bara.camera.CameraActivity;
import com.bara.bara.profile.ProfileActivity;
import com.bara.bara.profile.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Upload> mUploads;

    public ImageAdapter(Context context, List<Upload> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.post, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Upload uploadCurrent = mUploads.get(position);
        holder.textViewMessage.setText(uploadCurrent.getMessage());

        FirebaseDatabase.getInstance()
            .getReference("users")
                .orderByChild("email")
                .equalTo(uploadCurrent.getEmail())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot usernameSnapshot : dataSnapshot.getChildren()) {
//                        Log.i(ImageAdapter.class.getSimpleName(), "asdf:" + usernameSnapshot.getValue(UserData.class)));
                        String username = usernameSnapshot.getValue(UserData.class).getName();
                        holder.textViewUser.setText(username);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    holder.textViewUser.setText("-");
                }
            });

        //Create activity to selected user profile.
        holder.textViewUser.setOnClickListener(v -> {
            final Intent intent = new Intent(mContext, ProfileActivity.class);
            intent.putExtra("POST_EMAIL",uploadCurrent.getEmail());
            v.getContext().startActivity(intent);
        });
        Picasso.get()
                .load(uploadCurrent.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
//                .fit()
//                .centerCrop()
                .into(holder.imageView);
    }
    public void goToProfile() {
        final Intent intent = new Intent(mContext, ProfileActivity.class);
        mContext.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewMessage;
        public TextView textViewUser;
        public ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.post_message);
            imageView = itemView.findViewById(R.id.image_view_upload);
            textViewUser = itemView.findViewById(R.id.post_user);

        }
    }


}
