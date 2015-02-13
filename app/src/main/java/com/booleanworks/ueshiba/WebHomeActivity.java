package com.booleanworks.ueshiba;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.io.IOException;


public class WebHomeActivity extends ActionBarActivity implements Camera.PictureCallback{

    public SensorManager sensorManager;
    public SensorEventListener gyroListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_home);
        WebView webView = (WebView) this.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "activity");
        webView.loadData("<h1 id='test' onclick='alert(activity.testJs())'>default text 5</h1><script type='text/javascript'>alert(activity.testJs());</script>", "text/html", "UTF-8");

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor gyro = this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.gyroListener = new SensorEventListener() {

            public SensorManager sensorManager;
            public WebHomeActivity webHomeActivity;

            public SensorEventListener setup(SensorManager sensorManager, WebHomeActivity webHomeActivity) {
                this.sensorManager = sensorManager;
                this.webHomeActivity = webHomeActivity;
                return this;
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                float yRate = event.values[1];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }.setup(this.sensorManager, this);

        this.sensorManager.registerListener(this.gyroListener, gyro, 1000000);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @JavascriptInterface
    public String testJs() {
        return "Hello from activity ! (5)";
    }

    @JavascriptInterface
    public void doCapture() {
        try {
            for (int ctCam = 0; ctCam < Camera.getNumberOfCameras(); ctCam++) {
                Camera cCam = Camera.open(ctCam);
                Camera.CameraInfo camInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(ctCam, camInfo);
                if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cCam.enableShutterSound(true);
                    SurfaceView dummySurfaceView = new SurfaceView(this);

                    cCam.setPreviewDisplay(dummySurfaceView.getHolder());
                    cCam.startPreview();
                    cCam.takePicture(null,null,this);

                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        camera.release();

    }
}
