package com.speedev.sanat.attendanceassistant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final String TESS_DATA = "/tessdata";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Tess";
    private TextView tv;
    private TessBaseAPI tessBaseAPI;
    private Uri outputFileDir;
    private String mCurrentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.display_text);


    }

    public void run_capture(View view) {
        start_camera();



    }

    void start_camera() {

     try {

         String imagePath = DATA_PATH + "/image";

         File dir = new File(imagePath);
         if (!dir.exists()) {
             dir.mkdir();
         }
         String imageFilePath = imagePath + "/ocr.jpg";
         outputFileDir = Uri.fromFile(new File(imageFilePath));
         final Intent pic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


         pic.putExtra(MediaStore.EXTRA_OUTPUT, outputFileDir);

         if (pic.resolveActivity(getPackageManager()) != null) {
             startActivityForResult(pic, 100);
         }
     } catch (Exception e){
        Log.e(TAG, e.getMessage() );
     }


    }

    @SuppressLint("ShowToast")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 100 && requestCode == Activity.RESULT_OK) {

        } else {
            Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();


        }

    }


}
