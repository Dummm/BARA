package com.bara.bara.photo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bara.bara.R;

public class ImageViewer extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("filepath");
        Uri imageUri = Uri.parse(fileName);

        setContentView(R.layout.activity_image_viewer);

        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);

        final ImageButton imageDiscardButton = findViewById(R.id.image_discard_button);
        imageDiscardButton.setOnClickListener(v -> discardImage(this, imageUri));

        final ImageButton imageSaveButton = findViewById(R.id.image_save_button);
        imageSaveButton.setOnClickListener(v -> saveImage(this));

        final ImageButton imageShareButton = findViewById(R.id.share_button);
        imageShareButton.setOnClickListener(v -> shareImage(this, imageUri));
    }

    private void discardImage(ImageViewer callingActivity, Uri image) {
        getContentResolver().delete(image, null, null);
        callingActivity.finish();
    }

    private void saveImage(ImageViewer callingActivity) {
        callingActivity.finish();
    }

    private void shareImage(ImageViewer callingActivity, Uri image) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, image);
        callingActivity.startActivity(intent);
    }
}
