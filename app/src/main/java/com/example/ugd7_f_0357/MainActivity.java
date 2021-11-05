package com.example.ugd7_f_0357;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int SHAKE_THRESHOLD = 600;
    private static final int PERMISSIONS_REQUEST_CAMERA = 100;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mProximity;
    private Camera mCamera = null;
    private CameraView mCameraView;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // Cek status permission camera
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Kalau belum granted request permission
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Kalau permission ditolak tampil toast "Permission denied"
                Toast.makeText(MainActivity.this, "Permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[0];
            float z = sensorEvent.values[0];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD && mCamera == null) {
                    setContentView(R.layout.camera_activity);

                    try {
                        mCamera = Camera.open(currentCameraId);
                    } catch (Exception e) {
                        Log.d("Error", "Failed to Get Camera" + e.getMessage());
                    }
                    if (mCamera != null) {
                        setContentView(R.layout.camera_activity);

                        mCameraView = new CameraView(this, mCamera);
                        FrameLayout camera_view = findViewById(R.id.FLCamera);
                        camera_view.addView(mCameraView);
                    }
                    ImageButton imageClose = (ImageButton) findViewById(R.id.imgClose);
                    imageClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Ketika tombol close ditekan, set mCamera menjadi null (menandakan
                            // kamera sudah tidak berjalan dan set view kembali menjadi layout
                            // main_activity
                            mCamera = null;
                            setContentView(R.layout.main_activity);
                        }
                    });
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        } else if (mySensor.getType() == Sensor.TYPE_PROXIMITY) {
            Log.d("TAG", "onSensorChanged: jarak(cm): " + sensorEvent.values[0]);
            // Cek apakah kamera sudah berjalan, dan cek jumlah kamera HP apakah lebih dari 1
            if (mCamera != null && Camera.getNumberOfCameras() > 1) {
                if (sensorEvent.values[0] == 0 &&
                        currentCameraId != Camera.CameraInfo.CAMERA_FACING_BACK) {
                    // Ganti ke kamera belakang jika jarak sama dengan 0 dan
                    // currentCameraId bukan CAMERA_FACING_BACK
                    switchCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                    Toast.makeText(MainActivity.this, "Kamera belakang",
                            Toast.LENGTH_SHORT).show();
                } else if (sensorEvent.values[0] > 0 &&
                        currentCameraId != Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    // Ganti ke kamera depan jika jarak lebih dari 0 dan
                    // currentCameraId bukan CAMERA_FACING_FRONT
                    switchCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    Toast.makeText(MainActivity.this, "Kamera depan",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Fungsi untuk ganti kamera
    private void switchCamera(int cameraId) {
        mCamera.release();

        try {
            // Ganti camera sesuai dengan id permintaan lalu simpan id pada
            // variabel currentCameraId
            mCamera = Camera.open(cameraId);
            currentCameraId = cameraId;
        } catch (Exception e) {
            Log.d("Error", "Failed to Get Camera" + e.getMessage());
        }

        if (mCamera != null) {
            mCameraView = new CameraView(this, mCamera);
            FrameLayout camera_view = findViewById(R.id.FLCamera);
            camera_view.addView(mCameraView);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}