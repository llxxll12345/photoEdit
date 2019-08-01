package com.example.cvtest;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.example.cvtest.Utility.*;

import java.io.File;

public class CvActivity extends AppCompatActivity
    implements View.OnClickListener {
    private Button btnProcess, btnCamera, btnPhoto;
    private ImageView imgView;

    private Bitmap srcBitmap;
    private Bitmap grayBitmap;
    private static boolean flag = true;
    private static boolean isFirst = true;
    private static final String TAG = "gao_chun";

    private File cameraSavePath;
    private Uri uri;
    private String photoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cvmore_test_layout);

        imgView = (ImageView) findViewById(R.id.photoView);
        btnProcess = (Button) findViewById(R.id.process);
        btnCamera = (Button)  findViewById(R.id.photo);
        btnPhoto  = (Button)  findViewById(R.id.album);
        btnProcess.setOnClickListener(this);
        btnPhoto.setOnClickListener(this);
        btnCamera.setOnClickListener(this);

        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        cameraSavePath = new File(path, System.currentTimeMillis() + ".jpg");
    }


    @Override
    protected void onResume() {
        super.onResume();
        //load OpenCV engine and init OpenCV library
       /* OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, getApplicationContext(), mLoaderCallback);
        Log.i(TAG, "onResume sucess load OpenCV...");*/


        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


   private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "Successfully loaded");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "Fail");
                    break;
            }
        }
    };

    // Activate album
    private void goPhotoAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }

    // Activate camera
    private void goCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Above Android N
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(CvActivity.this, "com.example.example.fileprovider", cameraSavePath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(cameraSavePath);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        CvActivity.this.startActivityForResult(intent, 1);
    }


    public void procSrc2Gray() {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();

        //srcBitmap = BitmapFactory.decodeResource(getResources(), R.id.photoView);

        srcBitmap = ((BitmapDrawable)imgView.getDrawable()).getBitmap();
        grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);


        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat
        Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap
        Canvas c = new Canvas(grayBitmap);
        imgView.draw(c);

        Log.i(TAG, "process ...");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.process: {
                if (isFirst) {
                    procSrc2Gray();
                    isFirst = false;
                }
                if (flag) {
                    imgView.setImageBitmap(grayBitmap);
                    btnProcess.setText("original");
                    flag = false;
                } else {
                    imgView.setImageBitmap(srcBitmap);
                    btnProcess.setText("processed");
                    flag = true;
                }
            } break;
            case R.id.album:
                goPhotoAlbum();
                break;
            case R.id.photo:
                goCamera();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Camera mode
            // SDK check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoPath = String.valueOf(cameraSavePath);
            } else {
                photoPath = uri.getEncodedPath();
            }
            Log.d("Returned photo path:", photoPath);
            Glide.with(CvActivity.this).load(photoPath).into(imgView);
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            // Album mode
            photoPath = Utility.getRealPathFromUri(this, data.getData());
            Glide.with(CvActivity.this).load(photoPath).into(imgView);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
