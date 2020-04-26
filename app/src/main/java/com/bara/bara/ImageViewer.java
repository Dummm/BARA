package com.bara.bara;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.ImageView;

public class ImageViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("filepath");
        Uri imageUri = Uri.parse(fileName);

        setContentView(R.layout.activity_image_viewer);

        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);
    }
}
