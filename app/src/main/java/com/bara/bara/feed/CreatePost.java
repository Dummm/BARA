package com.bara.bara.feed;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bara.bara.R;
import com.bara.bara.camera.CameraActivity;
import com.bara.bara.model.Post;
import com.bara.bara.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class CreatePost extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private Button mButtonChooseImage;
    private Button mButtonUpload;
    private EditText mEditTextMessage;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mButtonUpload = findViewById(R.id.button_upload);
        mEditTextMessage = findViewById(R.id.edit_text_file_message);
        mProgressBar = findViewById(R.id.progress_bar);
        mImageView = findViewById(R.id.image_view);

        mStorageRef = FirebaseStorage.getInstance().getReference("posts");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("posts");

        mButtonChooseImage.setOnClickListener(v -> openFileChooser());

        mButtonUpload.setOnClickListener(v -> {
            if (mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(CreatePost.this, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadFile();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            mImageUri = data.getData();
            Picasso.get().load(mImageUri).into(mImageView);
        }
    }

    private String getFileExtension(Uri uri) {
        final ContentResolver contentResolver = getContentResolver();
        final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadFile() {
        if (mImageUri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageReference fileReference =
                mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

        mUploadTask = fileReference.putFile(mImageUri)
                .addOnSuccessListener(uploadSuccessful())
                .addOnFailureListener(
                        e -> Toast.makeText(CreatePost.this, e.getMessage(), Toast.LENGTH_SHORT).show()
                )
                .addOnProgressListener(updateProgress());
    }

    @NotNull
    private OnProgressListener<UploadTask.TaskSnapshot> updateProgress() {
        return taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            mProgressBar.setProgress((int) progress);
        };
    }

    @NotNull
    private OnSuccessListener<UploadTask.TaskSnapshot> uploadSuccessful() {
        return taskSnapshot -> {
            final Handler handler = new Handler();
            handler.postDelayed(() -> mProgressBar.setProgress(0), 500);

            Toast.makeText(CreatePost.this, "Upload successful", Toast.LENGTH_SHORT).show();
            final StorageReference storageReference =
                    requireNonNull(taskSnapshot.getMetadata(), "Null TaskSnapshot metadata")
                            .getReference();
            requireNonNull(storageReference, "Null storage reference")
                    .getDownloadUrl()
                    .addOnSuccessListener(saveToDatabase());

            Intent intent = new Intent(getApplicationContext(), Feed.class);
            startActivity(intent);
        };
    }

    @NotNull
    private OnSuccessListener<Uri> saveToDatabase() {
        return uri -> {
            final FirebaseAuth mAuth = FirebaseAuth.getInstance();
            final FirebaseUser currentUser = mAuth.getCurrentUser();
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    final Post post = new Post(mEditTextMessage.getText().toString().trim(), uri.toString(), user, user.getEmail());
                    final String uploadId = requireNonNull(mDatabaseRef.push().getKey(),
                            "Database reference key is null.");
                    post.setId(uploadId);
                    mDatabaseRef.child(uploadId).setValue(post);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });


        };
    }
}