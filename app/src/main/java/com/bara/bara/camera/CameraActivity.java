package com.bara.bara.camera;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.PixelCopy;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bara.bara.R;
import com.bara.bara.feed.Feed;
import com.bara.bara.filter.CameraFilterProvider;
import com.bara.bara.filter.FilterSelectorList;
import com.bara.bara.photo.ImageViewer;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.AugmentedFaceNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CameraActivity extends AppCompatActivity implements FilterSelectorList.OnListFragmentInteractionListener {
    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private List<ModelRenderable> models = new ArrayList<>();
    private HashMap<Integer, Integer> indexes = new HashMap<>();
    private boolean changeModel = false;


    private ModelRenderable faceRegionsRenderable;
    private final HashMap<AugmentedFace, AugmentedFaceNode> faceNodeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton goToPosts = findViewById(R.id.go_to_posts);
        goToPosts.setOnClickListener(v -> openFeed());

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        final ImageButton btn = findViewById(R.id.switchCamera);
        btn.setOnClickListener(v -> takePhoto());

        FaceArFragment arFragment = (FaceArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.face_fragment);

        buildModel(R.raw.cat);
        buildModel(R.raw.hors);
        buildModel(R.raw.glasses);
        buildModel(R.raw.sunglasses);

        final ArSceneView sceneView = arFragment.getArSceneView();
        sceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);
        final Scene scene = sceneView.getScene();

        scene.addOnUpdateListener((FrameTime frameTime) -> applyModel(sceneView, scene));
    }

    private void applyModel(ArSceneView sceneView, Scene scene) {
        if (faceRegionsRenderable == null) {
            return;
        }
        final Collection<AugmentedFace> faceList = sceneView.getSession()
                .getAllTrackables(AugmentedFace.class);
        makeAugmentedFaceNodes(scene, faceList);
        changeModel = false;
        removeAugmentedFaceNodes();
    }

    private void removeAugmentedFaceNodes() {
        final Iterator<Map.Entry<AugmentedFace, AugmentedFaceNode>> iter = faceNodeMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<AugmentedFace, AugmentedFaceNode> entry = iter.next();
            AugmentedFace face = entry.getKey();
            if (face.getTrackingState() == TrackingState.STOPPED) {
                AugmentedFaceNode faceNode = entry.getValue();
                faceNode.setParent(null);
                iter.remove();
            }
        }
    }

    private void makeAugmentedFaceNodes(Scene scene, Collection<AugmentedFace> faceList) {
        for (AugmentedFace face : faceList) {
            if (!faceNodeMap.containsKey(face)) {
                AugmentedFaceNode faceNode = new AugmentedFaceNode(face);
                faceNode.setParent(scene);
                faceNode.setFaceRegionsRenderable(faceRegionsRenderable);
                faceNodeMap.put(face, faceNode);
            } else if (changeModel) {
                faceNodeMap.get(face).setFaceRegionsRenderable(faceRegionsRenderable);
            }
        }
    }

    private void buildModel(int modelId) {
        ModelRenderable.builder()
                .setSource(this, modelId)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            indexes.put(modelId, models.size());
                            models.add(modelRenderable);
                            faceRegionsRenderable = modelRenderable;
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });
    }

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (ArCoreApk.getInstance().checkAvailability(activity)
                == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
            Log.e(TAG, "Augmented Faces requires ARCore.");
            Toast.makeText(activity, "Augmented Faces requires ARCore", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    public void onListFragmentInteraction(CameraFilterProvider.FilterSelectorItem item) {
        changeModel = !changeModel;
        int modelChosen = getModelId(item.name);

        faceRegionsRenderable = models.get(modelChosen);
    }

    private int getModelId(String name) {
        switch (name) {
            case "cat":
                return indexes.get(R.raw.cat);

            case "horse":
                return indexes.get(R.raw.hors);

            case "glasses":
                return indexes.get(R.raw.sunglasses);

            case "sunglasses":
                return indexes.get(R.raw.glasses);

            default:
                return indexes.get(R.raw.hors);
        }
    }

    private void takePhoto() {
        FaceArFragment arFragment = (FaceArFragment) getSupportFragmentManager().findFragmentById(R.id.face_fragment);
        final String filename = arFragment.generateFilename();
        ArSceneView view = arFragment.getArSceneView();

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        PixelCopy.request(view, bitmap, new PixelCopy.OnPixelCopyFinishedListener() {
            @Override
            public void onPixelCopyFinished(int copyResult) {
                if (copyResult == PixelCopy.SUCCESS) {
                    String path;
                    try {
                        path = arFragment.saveBitmapToDisk(bitmap, filename);
                    } catch (IOException e) {
                        Toast toast = Toast.makeText(CameraActivity.this, e.toString(),
                                Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }

                    Intent intent = new Intent(getApplicationContext(), ImageViewer.class);
                    intent.putExtra("filepath", path);
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(CameraActivity.this,
                            "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                    toast.show();
                }
                handlerThread.quitSafely();
            }
        }, new Handler(handlerThread.getLooper()));
    }

    public void openFeed() {
        Intent intent = new Intent(this, Feed.class);
        startActivity(intent);
    }
}
