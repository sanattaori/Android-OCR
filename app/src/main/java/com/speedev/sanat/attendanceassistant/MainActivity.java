package com.speedev.sanat.attendanceassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    public static final String TESS_DATA = "/tessdata";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString();
    private TextView tv;
    private TessBaseAPI tessBaseAPI;
    private Uri outputFileDir;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.display_text);
        final Activity activity = this;
        checkPermission();

    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 120);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 121);
        }
    }

    public void run_capture(View view) {
        start_camera();
    }

    void start_camera() {

     try {

         String imagePath = DATA_PATH;
         String imageFilePath = imagePath + "/ocr.jpg";
         outputFileDir = Uri.fromFile(new File(imageFilePath));

         Intent pic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         pic.putExtra(MediaStore.EXTRA_OUTPUT, outputFileDir);
         if (pic.resolveActivity(getPackageManager()) != null) {
             startActivityForResult(pic, 100);
         }

         File dir = new File(imagePath);
         if (!dir.exists()) {
             dir.mkdir();
         }

     } catch (Exception e){
        Log.e(TAG, e.getMessage() );
     }


    }

    @SuppressLint("ShowToast")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            prepareTessData();
            startOcr(outputFileDir);


    }
    @SuppressLint("ShowToast")
    private void prepareTessData() {
        try{
            File dir = new File(DATA_PATH + TESS_DATA);
            if(!dir.exists()){
                dir.mkdir();
            }
            //get file list
            String FileList[] = getAssets().list("");
            for (String fileName : FileList){
                //get file name
                String pathToDataFile = DATA_PATH + TESS_DATA + "/" + fileName;
                if(!(new File(pathToDataFile)).exists()) {
                    //write data to file
                    InputStream in = getAssets().open(fileName);
                    OutputStream out = new FileOutputStream(pathToDataFile);
                    byte [] buff = new byte[1024];
                    int len ;
                    while ((len = in.read(buff)) > 0) {
                        out.write(buff,0,len);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT);
        }
    }

    @SuppressLint("ShowToast")
    private void startOcr(Uri imageUrl) {
        try{
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 7;
            Bitmap bitmap = BitmapFactory.decodeFile(imageUrl.getPath(),options);
            String result = this.getText(bitmap);
            tv.setText(result);
        } catch (Exception e) {
            Toast.makeText(this,"Errorsss",Toast.LENGTH_SHORT);

        }
    }

    private String getText(Bitmap bitmap){
        try{
            tessBaseAPI = new TessBaseAPI();
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
        tessBaseAPI.init(DATA_PATH,"eng");
        tessBaseAPI.setImage(bitmap);
        String retStr = "No result";
        try{
            retStr = tessBaseAPI.getUTF8Text();
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
        tessBaseAPI.end();
        return retStr;
    }


}
