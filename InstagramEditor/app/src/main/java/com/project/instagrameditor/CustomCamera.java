package com.project.instagrameditor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CustomCamera extends AppCompatActivity {

    android.hardware.Camera camera;
    FrameLayout frameLayout;
    Button btnCapture;
    ShowCamera showCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);
        frameLayout = (FrameLayout)findViewById(R.id.frameLayout);


        if ( checkCameraHardware(this))
        {
            try {
            camera = Camera.open();
            showCamera = new ShowCamera(this, camera);
            frameLayout.addView(showCamera);
                Button p1_button = (Button)findViewById(R.id.btnCapture);
                p1_button.setText("Here camera");
            }
            catch (Exception e)
            {
                System.out.println("Error " + e.getMessage());
            }
        }
        else {
            Button p1_button = (Button)findViewById(R.id.btnCapture);
            p1_button.setText("No camera");

        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File picture_file = getOutputMediaFile();
            if(picture_file == null){
                return;
            }
            else {
                try {
                    FileOutputStream fos = new FileOutputStream(picture_file);
                    fos.write(data);
                    fos.close();

                    camera.startPreview();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private File getOutputMediaFile() {
        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)){
            return null;
        }
        else {
            File folder_gui = new File(Environment.getExternalStorageDirectory() + File.separator + "GUI");

            if(!folder_gui.exists()){
                folder_gui.mkdirs();
            }
            File outputFile = new File(folder_gui, "temp.jpg");
            return outputFile;
        }
    }

    public void capture (View v) {
        if(camera != null){
            camera.takePicture(null, null, mPictureCallback);
        }
    }
}