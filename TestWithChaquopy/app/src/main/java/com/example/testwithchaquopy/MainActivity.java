package com.example.testwithchaquopy;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    ImageView iv0, iv1;
    Button btn;
    BitmapDrawable drawable;
    Bitmap bitmap;
    String imgString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv0 = (ImageView)findViewById(R.id.img0);
        iv1 = (ImageView)findViewById(R.id.img1);
        btn = (Button)findViewById(R.id.submit);

        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        final Python py = Python.getInstance();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawable = (BitmapDrawable) iv0.getDrawable();
                bitmap = drawable.getBitmap();
                imgString = getStringImage(bitmap);
                
                PyObject pyo = py.getModule("IG");
                PyObject obj = pyo.callAttr("main", imgString);

                String str = obj.toString();
                byte[] data = Base64.decode(str, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                iv1.setImageBitmap(bmp);
            }
        });
    }

    private String getStringImage (Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imgBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imgBytes, Base64.DEFAULT);
        return encodedImage;
    }
}