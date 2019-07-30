package com.yue.camerademo.camera1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.yue.camerademo.R;
import com.yue.camerademo.databinding.ActivityCameraTest01Binding;

import java.io.IOException;
import java.util.List;

public class CameraTest01Activity extends AppCompatActivity {

    private Camera mCamera;
    private Preview preview;
    private ActivityCameraTest01Binding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera_test01);
        preview = new Preview(this);
        mBinding.flContainer.addView(preview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        safeCameraOpen(1000);
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        preview.setCamera(null);
        if (ca != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    class Preview extends ViewGroup implements SurfaceHolder.Callback {

        SurfaceView surfaceView;
        SurfaceHolder holder;

        Preview(Context context) {
            super(context);

            surfaceView = new SurfaceView(context);
            addView(surfaceView);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            holder = surfaceView.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // Now that the size is known, set up the camera parameters and begin
            // the preview.
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            requestLayout();
            mCamera.setParameters(parameters);

            // Important: Call startPreview() to start updating the preview surface.
            // Preview must be started before you can take a picture.
            mCamera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            if (mCamera != null) {
                // Call stopPreview() to stop updating the preview surface.
                mCamera.stopPreview();
            }
        }

        public void setCamera(Camera camera) {
            if (mCamera == camera) {
                return;
            }

            stopPreviewAndFreeCamera();

            mCamera = camera;

            if (mCamera != null) {
                List<Camera.Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
                supportedPreviewSizes = localSizes;
                requestLayout();

                try {
                    mCamera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Important: Call startPreview() to start updating the preview
                // surface. Preview must be started before you can take a picture.
                mCamera.startPreview();
            }
        }

        /**
         * When this function returns, mCamera will be null.
         */
        private void stopPreviewAndFreeCamera() {

            if (mCamera != null) {
                // Call stopPreview() to stop updating the preview surface.
                mCamera.stopPreview();

                // Important: Call release() to release the camera for use by other
                // applications. Applications should release the camera immediately
                // during onPause() and re-open() it during onResume()).
                mCamera.release();

                mCamera = null;
            }
        }
    }

}
