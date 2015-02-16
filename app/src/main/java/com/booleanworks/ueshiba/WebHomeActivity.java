package com.booleanworks.ueshiba;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;

import java.io.IOException;


public class WebHomeActivity extends ActionBarActivity implements Camera.PictureCallback {

    public SensorManager sensorManager = null;
    public SensorEventListener gyroListener = null;
    public Camera usedCamera = null;
    public CameraPreview previewSurfaceView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_home);
        WebView webView = (WebView) this.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "activity");
        webView.loadData("<h1 id='test' onclick='alert(activity.doCapture())'>default text 32</h1><script type='text/javascript'>alert(activity.testJs());</script>", "text/html", "UTF-8");


        for (int ctCam = 0; ctCam < Camera.getNumberOfCameras(); ctCam++) {
            Camera cCam = Camera.open(ctCam);

            //cCam.lock();
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(ctCam, camInfo);
            if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                this.usedCamera = cCam;

            }

        }

        this.previewSurfaceView = new CameraPreview(this, this.usedCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreviewFrame);
        preview.addView(this.previewSurfaceView);



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
        return "Hello from activity ! (7)";
    }

    @JavascriptInterface
    public void doCapture() {
        try {

            if (this.usedCamera != null) {

                this.usedCamera.takePicture(null, null, this);

            }


        } catch (RuntimeException e) {
            if (this.usedCamera != null) {
                //this.usedCamera.unlock();
                try {
                    this.usedCamera.reconnect();
                    this.usedCamera.release();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                this.usedCamera.release();
            }
            e.printStackTrace();
        }
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            //camera.stopPreview();
            //camera.release();
            //this.previewSurfaceView.getHolder().getSurface().release();


            int[] colorData = new int[bitmap.getHeight() * bitmap.getWidth()];
            bitmap.getPixels(colorData, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), colorData);

            MultiFormatReader multiFormatReader = new MultiFormatReader();
            GlobalHistogramBinarizer globalHistogramBinarizer = new GlobalHistogramBinarizer(rgbLuminanceSource);

            Result decodeResult = multiFormatReader.decode(new BinaryBitmap(globalHistogramBinarizer));

            Log.d("onPictureTaken", "text:" + decodeResult.getText());
            ;
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        //camera.stopPreview();
        //camera.unlock();
        //camera.release();


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.usedCamera != null) {
            //this.usedCamera.reconnect();
            //this.usedCamera.unlock();
            //this.usedCamera.release();
            try {
                //this.usedCamera.reconnect();
                this.usedCamera.release();

            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.usedCamera != null) {
            //this.usedCamera.reconnect();
            //this.usedCamera.unlock();
            //this.usedCamera.release();
            try {
                //this.usedCamera.reconnect();
                this.usedCamera.release();

            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.usedCamera != null) {
            //this.usedCamera.reconnect();
            //this.usedCamera.unlock();
            //this.usedCamera.release();
            try {
                //this.usedCamera.reconnect();
                try {
                    this.usedCamera.reconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }
}
