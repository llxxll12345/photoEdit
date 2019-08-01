package com.example.cvtest;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

public class PaintActivity extends Activity implements View.OnClickListener {
    private ImageView image;
    private Paint paint;
    private Canvas canvas;
    private Bitmap bitmap;
    private Bitmap copyBitmap;
    private Bitmap srcBitmap;
    private Bitmap resMat;

    private Button choose;
    private Button save;
    private SeekBar thickBar;
    private final static int RESULT = 0;

    private float lastX = 0;
    private float lastY = 0;
    private float x = 0;
    private float y = 0;

    private int   thickness = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gray_layout);
        image = (ImageView) findViewById(R.id.image);
        choose = (Button) findViewById(R.id.btnChoose);
        save = (Button) findViewById(R.id.btnSave);
        thickBar = (SeekBar) findViewById(R.id.barThick);

        choose.setOnClickListener(this);
        save.setOnClickListener(this);

        thickness = thickBar.getProgress();


        thickBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                thickness = i;
                Paint paint = new Paint();
                paint.setStrokeWidth(thickness);
                paint.setColor(Color.GRAY);
                canvas.drawPaint(paint);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("Seekbar", "Start Moving.");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("Seekbar", "Stop Moving.");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    lastX = event.getX();
                    lastY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    x = event.getX();
                    y = event.getY();
                    canvas.drawLine(lastX, lastY, x, y, paint);
                    image.setImageBitmap(copyBitmap);
                    lastX = x;
                    lastY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    // choose file from the storage
    public void choose() {
        Log.d("Event", "choose");
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT);
    }

    // save picture into the storage
    public void save() {
        if (copyBitmap != null) {
            try {
                Uri imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                OutputStream outputStream = getContentResolver()
                        .openOutputStream(imageUri);
                copyBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                Toast.makeText(getApplicationContext(), "Saved!! " + imageUri.toString(), Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnChoose:
                choose();
                break;
            case R.id.btnSave:
                save();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri imageFileUri = data.getData();

            WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);

            float dw = dm.widthPixels;
            float dh = dm.heightPixels;

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                bitmap = BitmapFactory.decodeStream(getContentResolver()
                        .openInputStream(imageFileUri), null, options);
                int heightRatio = (int) Math.ceil(options.outHeight / dh * 0.8);
                int widthRatio = (int) Math.ceil(options.outWidth / dw * 0.8);
                if (heightRatio > 1 && widthRatio > 1) {
                    if (heightRatio > widthRatio) {
                        options.inSampleSize = heightRatio;
                    } else {
                        options.inSampleSize = widthRatio;
                    }
                }

                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeStream(getContentResolver()
                        .openInputStream(imageFileUri), null, options);
                copyBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                        bitmap.getHeight(), bitmap.getConfig());
                canvas = new Canvas(copyBitmap);

                paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStrokeWidth(10);

                canvas.drawBitmap(bitmap, new Matrix(), paint);
                image.setImageBitmap(copyBitmap);
                image.setOnTouchListener(new MyTouchListener());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
