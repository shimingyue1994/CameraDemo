package com.yue.camerademo.camera1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import com.yue.camerademo.R;
import com.yue.camerademo.databinding.ActivityCameraSimpleBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author shimy
 * @create 2019/8/1 15:03
 * @desc 设备设想头的简单使用--预览
 * 绘制都是camera对象调用surfaceview或者TextureView的相关方法显示上去的 不用管
 */
public class CameraSimpleActivity extends AppCompatActivity {

    private ActivityCameraSimpleBinding mBinding;
    /*surfaceview的控制器*/
    private SurfaceHolder mSurfaceHolder;

    private Camera mCamera;
    private int numberOfCameras;//可用摄像头的数量
    private int cameraCurrentlyLocked;//当前使用中的摄像头的id
    // 默认摄像头
    int defaultCameraId;
    /*传感器 获取方向和陀螺仪*/
    private SensorManager mSensorManager;
    /**
     * 屏幕旋转的监听
     */
    private OrientationEventListener orientationEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera_simple);
        mSurfaceHolder = mBinding.surfaceView.getHolder();
        mSurfaceHolder.addCallback(callback2);

        // 1.查找可用摄像头的数量
        numberOfCameras = Camera.getNumberOfCameras();

        // 2.查找默认摄像头的id(前置摄像头和后置摄像头的id)
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
                mBinding.tvShowCamerafx.setText("摄像头方向：" + cameraInfo.orientation);
            }
        }


        initSensor();
        /*屏幕旋转*/
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int i) {
                setCameraDisplayOrientation(CameraSimpleActivity.this, cameraCurrentlyLocked, mCamera);
                mBinding.tvShowSceen.setText("\n屏幕旋转：" + i);
            }
        };
        orientationEventListener.enable();

        initClick();
    }


    private void initClick() {
        /*切换摄像头*/
        mBinding.btnSwitch.setOnClickListener(view -> {
            releaseCameraAndPreview();
            // Acquire the next camera and request Preview to reconfigure
            // parameters.
            mCamera = Camera
                    .open((cameraCurrentlyLocked + 1) % numberOfCameras);
            /*注意CameraInfo中的方向为摄像头采集方向这个值不会因为setDisplayOrientation的调用而发生改变，setDisplayOrientation方法改变的只是界面上的预览方向*/
            Camera.CameraInfo cameraInfos = new Camera.CameraInfo();
            Camera.getCameraInfo((cameraCurrentlyLocked + 1) % numberOfCameras, cameraInfos);
            if ((cameraCurrentlyLocked + 1) % numberOfCameras == 0) {
                mBinding.tvShowCamerafx.setText("后置摄像头方向：" + cameraInfos.orientation);
            } else {
                mBinding.tvShowCamerafx.setText("前置摄像头方向：" + cameraInfos.orientation);
            }
            cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
                    % numberOfCameras;
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            mCamera.startPreview();
        });

        /*拍照*/
        mBinding.btnTakePicture.setOnClickListener(view -> {
            // get an image from the camera
            mCamera.takePicture(null, null, mPicture);
//            try {
//                mCamera.setPreviewDisplay(mSurfaceHolder);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            // Important: Call startPreview() to start updating the preview
//            // surface. Preview must be started before you can take a picture.
//            mCamera.startPreview();
        });
    }

    private void initSensor() {
        // 获取传感器管理对象
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // 为重力传感器注册监听器
        mSensorManager.registerListener(sensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);

        // 为方向传感器注册监听器
        mSensorManager.registerListener(sensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);


        // 为陀螺仪传感器注册监听器
        mSensorManager.registerListener(sensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onResume() {
        super.onResume();
        /*3. 打开获取到的默认摄像头的id*/
        if (safeCameraOpen(defaultCameraId)) {
            try {
                /*设置摄像头捕捉到的画面预览在什么地方 与surfaceview相对的还可以显示到TextureView上去 */
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            //开始和更新预览的方法 这样捕获的画面就显示在surfaceview上了
            mCamera.startPreview();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        releaseCameraAndPreview();
    }

    /**
     * 打开一个摄像头
     * setDisplayOrientation：此方法设置摄像头预览方向 顺时针旋转
     *
     * @param id
     * @return
     */
    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);

            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(id, cameraInfo);
            mBinding.tvShowCamerafx.setText("后置摄像头方向：" + cameraInfo.orientation);
            cameraCurrentlyLocked = id;
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
        return qOpened;
    }

    /**
     * 释放掉摄像头
     */
    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private SurfaceHolder.Callback2 callback2 = new SurfaceHolder.Callback2() {
        @Override
        public void surfaceRedrawNeeded(SurfaceHolder surfaceHolder) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            // The Surface has been created, acquire the camera and tell it where
            // to draw.
            Log.i("CameraSimpleActivity", "surfaceCreated");
            try {
                if (mCamera != null) {
                    /*设置camerab捕获到的画面在哪里显示（surffaceholder 或TextureView上都可以）*/
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                }
            } catch (IOException exception) {
//                Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            /*开始预览*/
            // Now that the size is known, set up the camera parameters and begin
            // the preview.
            Camera.Parameters parameters = mCamera.getParameters();
//            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);

            // Important: Call startPreview() to start updating the preview surface.
            // Preview must be started before you can take a picture.
            mCamera.startPreview();
            Log.i("CameraSimpleActivity", "surfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            // Surface will be destroyed when we return, so stop the preview.
            if (mCamera != null) {
                // Call stopPreview() to stop updating the preview surface.
                mCamera.stopPreview();
            }
            Log.i("CameraSimpleActivity", "surfaceDestroyed");
        }
    };


    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            float[] values = sensorEvent.values;

            // 获取传感器类型

            int type = sensorEvent.sensor.getType();

            StringBuilder sb;

            switch (type) {
                case Sensor.TYPE_ORIENTATION://方向
                    sb = new StringBuilder();

                    sb.append("\n方向传感器返回数据：");

                    sb.append("\n绕Z轴转过的角度：");

                    sb.append(values[0]);

                    sb.append("\n绕X轴转过的角度：");

                    sb.append(values[1]);

                    sb.append("\n绕Y轴转过的角度：");

                    sb.append(values[2]);

                    mBinding.tvShowOrientation.setText(sb.toString());

                    break;

                case Sensor.TYPE_GRAVITY://重力
                    sb = new StringBuilder();

                    sb.append("\n重力传感器返回数据：");

                    sb.append("\nX轴方向上的重力：");

                    sb.append(values[0]);

                    sb.append("\nY轴方向上的重力：");

                    sb.append(values[1]);

                    sb.append("\nZ轴方向上的重力：");

                    sb.append(values[2]);

                    mBinding.tvShowGravity.setText(sb.toString());
                    break;
                case Sensor.TYPE_GYROSCOPE://陀螺仪

                    sb = new StringBuilder();

                    sb.append("\n陀螺仪传感器返回数据：");

                    sb.append("\n绕X轴旋转的角速度：");

                    sb.append(values[0]);

                    sb.append("\n绕Y轴旋转的角速度：");

                    sb.append(values[1]);

                    sb.append("\n绕Z轴旋转的角速度：");

                    sb.append(values[2]);

                    mBinding.tvShowGyroscope.setText(sb.toString());
                    break;
            }
        }

        // 当传感器精度发生改变时回调该方法
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "TEST_IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "TEST_VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.stopPreview();//关闭预览 处理数据
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d("CameraSimpleActivity", "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                camera.startPreview();//数据处理完后继续开始预览
            } catch (FileNotFoundException e) {
                Log.d("CameraSimpleActivity", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("CameraSimpleActivity", "Error accessing file: " + e.getMessage());
            }
        }
    };


    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        if (camera != null) {
            camera.setDisplayOrientation(result);
            Log.i("CameraSimpleActivity", "相机预览方向发生改变：setCameraDisplayOrientation"+"degrees:"+degrees+" result:"+result);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orientationEventListener != null) {
            orientationEventListener.disable();
            releaseCameraAndPreview();
        }
    }
}
