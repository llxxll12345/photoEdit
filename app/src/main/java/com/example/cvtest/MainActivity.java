package com.example.cvtest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks,
                    View.OnClickListener{
    private Button btnBlur;
    private Button btnMosaic;
    private Button btnGray;
    private Button btnRepair;
    private Button btnBlob;
    private Button btnContour;

    private String[] permissions = { Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();
        btnBlur = findViewById(R.id.btnBlur);
        btnGray = findViewById(R.id.btnGray);
        btnMosaic = findViewById(R.id.btnMosaic);
        btnRepair = findViewById(R.id.btnRepair);
        btnBlob = findViewById(R.id.btnBlob);
        btnContour = findViewById(R.id.btnContour);

        btnBlur.setOnClickListener(this);
        btnGray.setOnClickListener(this);
        btnMosaic.setOnClickListener(this);
        btnRepair.setOnClickListener(this);
        btnBlob.setOnClickListener(this);
        btnContour.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBlur: {
                Intent intent = new Intent(MainActivity.this, BlurActivity.class);
                startActivity(intent);
            } break;
            case R.id.btnGray: {
                Intent intent = new Intent(MainActivity.this, GrayActivity.class);
                startActivity(intent);
            } break;
            case R.id.btnMosaic: {
                Intent intent = new Intent(MainActivity.this, MosaicActivity.class);
                startActivity(intent);
            } break;
            case R.id.btnRepair: {
                Intent intent = new Intent(MainActivity.this, RepairActivity.class);
                startActivity(intent);
            } break;
            case R.id.btnBlob: {
                Intent intent = new Intent(MainActivity.this, BlobActivity.class);
                startActivity(intent);
            } break;
            case R.id.btnContour: {
                Intent intent = new Intent(MainActivity.this, PaintActivity.class);
                startActivity(intent);
            } break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void getPermission() {
        if (EasyPermissions.hasPermissions(this, permissions)) {
            Toast.makeText(this, "Permission to access album acquired!", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "Need permission to access album", 1, permissions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Using easyPermission
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    // Gained permission
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Permission acquired", Toast.LENGTH_SHORT).show();
    }

    // Didn't agree
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Please give permission to access photo & album in order to use the APP!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
