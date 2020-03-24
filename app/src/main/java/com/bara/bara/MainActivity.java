package com.bara.bara;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.AugmentedFaceNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private List<ModelRenderable> models = new ArrayList<>();
    private boolean changeModel = false;
    private int modelIndex = 0;

    private ModelRenderable faceRegionsRenderable;
    private final HashMap<AugmentedFace, AugmentedFaceNode> faceNodeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_main);

        ImageButton btn = findViewById(R.id.button_next);
        btn.setOnClickListener(v -> {
            changeModel = !changeModel;
            modelIndex++;
            if (modelIndex > models.size() - 1) {
                modelIndex = 0;
            }
            faceRegionsRenderable = models.get(modelIndex);
        });

        FaceArFragment arFragment = (FaceArFragment) getSupportFragmentManager().findFragmentById(R.id.face_fragment);
        buildModel(R.raw.hors);
        buildModel(R.raw.glasses);

        final ArSceneView sceneView = arFragment.getArSceneView();
        sceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);
        final Scene scene = sceneView.getScene();

        scene.addOnUpdateListener(
                (FrameTime frameTime) -> {
                    if (faceRegionsRenderable == null) {// || faceMeshTexture == null) {
                        return;
                    }
                    final Collection<AugmentedFace> faceList = sceneView.getSession()
                            .getAllTrackables(AugmentedFace.class);
                    // Make new AugmentedFaceNodes for any new faces.
                    makeAugumentedFaceNodes(scene, faceList);
                    changeModel = false;
                    // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking.
                    removeAugumentedFaceNodes();
                });
    }

    private void removeAugumentedFaceNodes() {
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

    private void makeAugumentedFaceNodes(Scene scene, Collection<AugmentedFace> faceList) {
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
}