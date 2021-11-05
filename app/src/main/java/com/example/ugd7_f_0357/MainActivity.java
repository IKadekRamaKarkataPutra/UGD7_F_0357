package com.example.ugd7_f_0357;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private long lastUpdate=0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD=600;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Camera mCamera = null;
    //private static final String TAG = "MainActivity";
    //private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mSensorManager =
                (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer =
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //verifyPermissions();
    }
    //permission nya error tidak bisa menjalankan accelerometer
    //private void verifyPermissions(){
        //Log.d(TAG,"veryfyPermission : asking yuser for permissions");
        //String[] permissions = {Manifest.permission.CAMERA};

        //if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                //permissions[0])== PackageManager.PERMISSION_GRANTED){
            //Intent intent = new Intent(this,MainActivity.class);
            //startActivity(intent);
        //}else{
            //ActivityCompat.requestPermissions(MainActivity.this,permissions,REQUEST_CODE);
        //}
    //}

    //@Override
    //public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //verifyPermissions();
    //}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if(mySensor.getType()==Sensor.TYPE_ACCELEROMETER)
        {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[0];
            float z = sensorEvent.values[0];
            long curTime = System.currentTimeMillis();
            if((curTime-lastUpdate)>100)
            {
                long diffTime = (curTime-lastUpdate);
                lastUpdate = curTime;
                float speed = Math.abs(x+y+z-last_x-last_y-last_z)/diffTime*10000;
                if(speed>SHAKE_THRESHOLD)
                {
                    setContentView(R.layout.camera_activity);
                    try
                    {
                        mCamera = Camera.open();
                    }
                    catch(Exception e)
                    {
                        Log.d("Error", "Failed to Get Camera" + e.getMessage());
                    }
                    if(mCamera != null)
                    {
                        setContentView(R.layout.camera_activity);
                        CameraView mCameraView = new CameraView(this, mCamera);
                        FrameLayout camera_view =
                                findViewById(R.id.FLCamera);
                        camera_view.addView(mCameraView);
                    }
                    ImageButton imageClose =
                            (ImageButton)findViewById(R.id.imgClose);
                    imageClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view){
                            finish();
                        }
                    });
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}