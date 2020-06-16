package com.bara.bara.camera;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.ar.core.Config;
import com.google.ar.core.Config.AugmentedFaceMode;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

public class FaceArFragment extends ArFragment {

    @Override
    protected Config getSessionConfiguration(Session session) {
        final Config config = new Config(session);
        config.setAugmentedFaceMode(AugmentedFaceMode.MESH3D);
        return config;
    }

    @Override
    protected Set<Session.Feature> getSessionFeatures() {
        return EnumSet.of(Session.Feature.FRONT_CAMERA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final FrameLayout frameLayout =
                (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);

        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);

        return frameLayout;
    }

    @Override
    public String[] getAdditionalPermissions() {
        String[] additionalPermissions = super.getAdditionalPermissions();
        int permissionLength = additionalPermissions != null ? additionalPermissions.length : 0;
        String[] permissions = new String[permissionLength + 2];
        permissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
        permissions[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (permissionLength > 0) {
            System.arraycopy(additionalPermissions, 0, permissions, 2, additionalPermissions.length);
        }

        return permissions;
    }

    public boolean hasWritePermission() {
        return ActivityCompat.checkSelfPermission(
                this.requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Launch Application Setting to grant permissions. */
    public void launchPermissionSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", requireActivity().getPackageName(), null));
        requireActivity().startActivity(intent);
    }

    String generateFilename() {
        String date = new SimpleDateFormat(
                "yyyyMMddHHmmss",
                java.util.Locale.getDefault())
            .format(new Date());
        return date + "_screenshot.png";
    }

    String saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {
        OutputStream fos;
        Uri filePath;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "Sceneform");

            ContentResolver resolver = getContext().getContentResolver();
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
            filePath = imageUri;
        } else {
            String storagePath = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString();
            String imagesDir = storagePath + File.separator + "Sceneform";

            File file = new File(imagesDir);
            if (!file.exists()) {
                file.mkdir();
            }

            File image = new File(imagesDir, filename);
            fos = new FileOutputStream(image);
            filePath = Uri.fromFile(image);
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();

        return filePath.toString();
    }
    public String saveVideoToDisk(String filename) throws IOException {
        OutputStream fos;
        Uri filePath;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "Sceneform");

            ContentResolver resolver = getContext().getContentResolver();
            Uri videoUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(videoUri);
            filePath = videoUri;
        } else {
            String storagePath = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString();
            String imagesDir = storagePath + File.separator + "Sceneform";

            File file = new File(imagesDir);
            if (!file.exists()) {
                file.mkdir();
            }

            File image = new File(imagesDir, filename);
            fos = new FileOutputStream(image);
            filePath = Uri.fromFile(image);
        }
        fos.flush();
        fos.close();

        return convertMediaUriToPath(filePath);
    }
    public String convertMediaUriToPath(Uri uri) {
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(uri, proj,  null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
}
