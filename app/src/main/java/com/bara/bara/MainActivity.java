package com.bara.bara;
import com.bara.bara.CameraFilterProvider.FilterSelectorItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

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

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.AugmentedFaceNode;


public class MainActivity extends AppCompatActivity implements FilterSelectorList.OnListFragmentInteractionListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private static List<Integer> model_raws = new ArrayList<>();
    private List<ModelRenderable> models = new ArrayList<>();
    private HashMap<Integer, Integer> indexes = new HashMap<>();
    private boolean changeModel = false;
    static {
        model_raws.add(R.raw.cat);
//        model_raws.add(R.raw.feis);
        model_raws.add(R.raw.hors);
        model_raws.add(R.raw.glasses);
        model_raws.add(R.raw.sunglasses);
    }

    private ModelRenderable faceRegionsRenderable;
    private final HashMap<AugmentedFace, AugmentedFaceNode> faceNodeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_main);
        ImageButton btn = findViewById(R.id.switchCamera);
        btn.setOnClickListener(v ->takePhoto());

        FaceArFragment arFragment = (FaceArFragment) getSupportFragmentManager().findFragmentById(R.id.face_fragment);
        for (Integer raw : model_raws) {
            buildModel(raw);
        }

        final ArSceneView sceneView = arFragment.getArSceneView();
        sceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);
        final Scene scene = sceneView.getScene();

        scene.addOnUpdateListener(
            (FrameTime frameTime) -> {
                if (faceRegionsRenderable == null) {// || faceMeshTexture == null) {
                    return;
                }
                final Collection<AugmentedFace> faceList = sceneView
                        .getSession()
                        .getAllTrackables(AugmentedFace.class);

                // Make new AugmentedFaceNodes for any new faces.
                makeAugumentedFaceNodes(scene, faceList);
                changeModel = false;

                // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking.
                removeAugumentedFaceNodes();
            });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Session session = null;
        try {
            session = new Session(/* context= */ this);
        } catch (UnavailableArcoreNotInstalledException e) {
            e.printStackTrace();
        } catch (UnavailableApkTooOldException e) {
            e.printStackTrace();
        } catch (UnavailableSdkTooOldException e) {
            e.printStackTrace();
        } catch (UnavailableDeviceNotCompatibleException e) {
            e.printStackTrace();
        }
        // Create an ARCore session.
        // Create a camera config filter for the session.
        CameraConfigFilter filter = new CameraConfigFilter(session);
        // Return only camera configs that target 30 fps camera capture frame rate.
        filter.setTargetFps(EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30));
        // Return only camera configs that will not use the depth sensor.
        filter.setDepthSensorUsage(EnumSet.of(CameraConfig.DepthSensorUsage.DO_NOT_USE));
        // Get list of configs that match filter settings.
        // In this case, this list is guaranteed to contain at least one element,
        // because both TargetFps.TARGET_FPS_30 and DepthSensorUsage.DO_NOT_USE
        // are supported on all ARCore supported devices.
        List<CameraConfig> cameraConfigList = session.getSupportedCameraConfigs(filter);
        // Use element 0 from the list of returned camera configs. This is because
        // it contains the camera config that best matches the specified filter
        // settings.
        session.setCameraConfig(cameraConfigList.get(0));


        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

    }

    private void removeAugumentedFaceNodes() {
        final Iterator<Entry<AugmentedFace, AugmentedFaceNode>> iter = faceNodeMap.entrySet().iterator();

        while (iter.hasNext()) {
            Entry<AugmentedFace, AugmentedFaceNode> entry = iter.next();
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
                    indexes.put(modelId, models.size());
                    models.add(modelRenderable);
                    faceRegionsRenderable = modelRenderable;
                    modelRenderable.setShadowCaster(false);
                    modelRenderable.setShadowReceiver(false);
                });
    }

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (ArCoreApk.getInstance().checkAvailability(activity) == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
            Log.e(TAG, "Augmented Faces requires ARCore.");
            Toast.makeText(activity,
                    "Augmented Faces requires ARCore",
                    Toast.LENGTH_LONG)
                .show();
            activity.finish();
            return false;
        }
        String openGlVersionString = ((ActivityManager) activity
                .getSystemService(Context.ACTIVITY_SERVICE))
                .getDeviceConfigurationInfo()
                .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity,
                    "Sceneform requires OpenGL ES 3.0 or later",
                    Toast.LENGTH_LONG)
                .show();
            activity.finish();
            return false;
        }
        return true;
    }

    public void onListFragmentInteraction(FilterSelectorItem item) {
        changeModel = !changeModel;
        int modelChosen = getModelId(item.name);

        faceRegionsRenderable = models.get(modelChosen);
    }

    private int getModelId(String name) {
        switch (name) {
            case "cat":         return indexes.get(R.raw.cat);
            case "horse":       return indexes.get(R.raw.hors);
            case "glasses":     return indexes.get(R.raw.sunglasses);
            case "sunglasses":  return indexes.get(R.raw.glasses);
            default:            return indexes.get(R.raw.hors);
        }
    }

    private void takePhoto() {
        FaceArFragment arFragment = (FaceArFragment) getSupportFragmentManager().findFragmentById(R.id.face_fragment);
        final String filename = arFragment.generateFilename();
        ArSceneView view = arFragment.getArSceneView();

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(
            view.getWidth(),
            view.getHeight(),
            Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();

        // Make the request to copy.
        PixelCopy.request(view, bitmap, copyResult -> {
            if (copyResult == PixelCopy.SUCCESS) {
                String path;
                try {
                    path = arFragment.saveBitmapToDisk(bitmap, filename);
                } catch (IOException e) {
                    Toast toast = Toast.makeText(
                        MainActivity.this,
                        e.toString(),
                        Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), ImageViewer.class);
                intent.putExtra("filepath", path);
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(
                    MainActivity.this,
                    "Failed to copyPixels: " + copyResult,
                    Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }
}
