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
    static final int PHOTO_REQUEST_CODE = 1;
    private static final String lang = "eng";



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

         String imagePath = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/imgs";
         String imageFilePath = imagePath + "/ocr.jpg";
         outputFileDir = Uri.fromFile(new File(imageFilePath));

         final Intent pic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         pic.putExtra(MediaStore.EXTRA_OUTPUT, outputFileDir);
         if (pic.resolveActivity(getPackageManager()) != null) {
             startActivityForResult(pic, PHOTO_REQUEST_CODE);
         }


     } catch (Exception e){
        Log.e(TAG, e.getMessage() );
     }


    }

    @SuppressLint("ShowToast")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

//            prepareTessData();
//            startOcr(outputFileDir);
        //making photo
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            doOCR();
        } else {
            Toast.makeText(this, "ERROR: Image was not obtained.", Toast.LENGTH_SHORT).show();
        }


    }
    private void doOCR() {
        prepareTessData();
        startOcr(outputFileDir);
    }

    @SuppressLint("ShowToast")
    private void prepareTessData() {
        try{
            File dir = new File(DATA_PATH + TESS_DATA);
            if(!dir.exists()){
                if (!dir.mkdirs()) {
                    Log.e(TAG, "ERROR: Creation of directory " + DATA_PATH+ TESS_DATA + " failed, check does Android Manifest have permission to write to external storage.");
                } else {
                    Log.i(TAG,"Created dir"+DATA_PATH+TESS_DATA);
                }
            }
            //copy tess data files
            String FileList[] = getAssets().list(TESS_DATA);
            for (String fileName : FileList){
                //get file name
                String pathToDataFile = DATA_PATH + TESS_DATA + "/" + fileName;
                if(!(new File(pathToDataFile)).exists()) {
                    //write data to file
                    InputStream in = getAssets().open(TESS_DATA+ "/"+fileName);
                    OutputStream out = new FileOutputStream(pathToDataFile);
                    byte [] buff = new byte[1024];
                    int len ;
                    while ((len = in.read(buff)) > 0) {
                        out.write(buff,0,len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG,"copied"+fileName+"totess");
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,"Error unable to copy",Toast.LENGTH_SHORT);
        }
    }

    @SuppressLint("ShowToast")
    private void startOcr(Uri imageUrl) {
        try{
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeFile(imageUrl.getPath(),options);
            String result =  extractText(bitmap);
            tv.setText(result);
        } catch (Exception e) {
            Toast.makeText(this,"error startOCR",Toast.LENGTH_SHORT);

        }
    }

    private String extractText(Bitmap bitmap){
        try{
            tessBaseAPI = new TessBaseAPI();
        }catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (tessBaseAPI == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }
        tessBaseAPI.init(DATA_PATH,"eng");
        Log.d(TAG, "Training file loaded");
        tessBaseAPI.setImage(bitmap);
        String retStr = "";
        try{
            retStr = tessBaseAPI.getUTF8Text();
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
        tessBaseAPI.end();
        return retStr;
    }


}
