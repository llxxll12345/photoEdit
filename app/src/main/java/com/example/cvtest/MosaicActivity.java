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

public class MosaicActivity extends Activity implements View.OnClickListener {
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
    private SeekBar levelBar;
    private final static int RESULT = 0;

    private float lastX = 0;
    private float lastY = 0;
    private float x = 0;
    private float y = 0;

    private int   thickness = 0;
    private int   level = 0;

    private Mat blurredMat;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("Blur", "OpenCV loaded successfully");
                    //mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(BlobActivity.this);
                } break;
                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blur_layout);
        image = (ImageView) findViewById(R.id.imageView);
        choose = (Button) findViewById(R.id.btnChoose);
        save = (Button) findViewById(R.id.btnSave);
        thickBar = (SeekBar) findViewById(R.id.barThickness);
        levelBar = (SeekBar) findViewById(R.id.barLevel);

        choose.setOnClickListener(this);
        save.setOnClickListener(this);

        thickness = thickBar.getProgress();

        thickBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                thickness = i;
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

        levelBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                level = i;
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
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            Toast.makeText(this, "Inernal Opencv not found.", Toast.LENGTH_SHORT).show();
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Toast.makeText(this, "Using opencv.", Toast.LENGTH_SHORT).show();
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
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
                    //canvas.drawLine(lastX, lastY, x, y, paint);

                    srcBitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
                    //Bitmap temp = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

                    Mat resMat = new Mat(srcBitmap.getHeight(), srcBitmap.getWidth(), CV_8UC3);
                    Utils.bitmapToMat(srcBitmap, resMat);//convert original bitmap to Mat, R G B.
                    //Imgproc.cvtColor(resMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat

                    int height = resMat.height();
                    int width = resMat.width();

                    int up = Math.max(0, (int)lastY);
                    int left = Math.max(0, (int)lastX);
                    int w = Math.min(width - left - 1, thickness * 50);
                    int h = Math.min(height - up - 1, thickness * 50);

                    if (w > 0 && h > 0 && up + h < height && left + w < width) {
                        Rect roi = new Rect(left, up, w, h);
                        Mat sub = resMat.submat(roi);
                        // Log.d("Rect", roi//.width() + ", " + roi.height());

                        //Mat sub = resMat.submat(roi);
                        //Imgproc.cvtColor(sub, sub, Imgproc.COLOR_RGBA2GRAY);
                        //Imgproc.cvtColor(sub, sub, Imgproc.COLOR_GRAY2RGBA);
                        //Imgproc.GaussianBlur(sub, sub, new Size(5, 5), 2, 2);

                        Scalar color = new Scalar(sub.get(sub.rows()/2, sub.height()/2));
                        sub.setTo(color);

                        //Mat mask = Mat.zeros(blurredMat.height(), blurredMat.width(), CV_8UC1);
                        //Imgproc.circle(mask, new Point(x, y),
                        //        thickness * 25, new Scalar(255), -1, 8);
                        //blurredMat.copyTo(resMat, mask);

                        Mat mask = Mat.zeros(sub.height(), sub.width(), CV_8UC1);
                        Imgproc.circle(mask, new Point(mask.height()/2, mask.width()/2),
                                mask.height()/2, new Scalar(255), -1, 8);
                        sub.copyTo(resMat.submat(roi), mask);


                        /*Mat temp = new Mat(roi.height(), roi.width(), CV_8UC3, new Scalar(50, 50, 50));

                        Imgproc.rectangle(mask, new Point(left, up), new Point(left + w, up + h), new Scalar(255));
                        .copyTo(resMat, mask);

                        mask.release();*/
                    }

                    Utils.matToBitmap(resMat, srcBitmap); //convert mat to bitmap
                    resMat.release();

                    image.setImageBitmap(srcBitmap);

                    image.invalidate();
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
                Toast.makeText(getApplicationContext(), "Saved!!", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        srcBitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        blurredMat = new Mat(srcBitmap.getHeight(), srcBitmap.getWidth(), CV_8UC3);
        int width = blurredMat.width();
        int height = blurredMat.height();
        Utils.bitmapToMat(srcBitmap, blurredMat);
        for (int i = 0; i < srcBitmap.getHeight(); i += level) {
            for (int j = 0; j < srcBitmap.getWidth(); j += level) {
                int w = Math.min(width - j - 1, level);
                int h = Math.min(height - i - 1, level);

                Rect roi = new Rect(j, i, w, h);
                Mat sub = blurredMat.submat(roi);
                Scalar color = new Scalar(sub.get(sub.rows()/2, sub.height()/2));
                sub.setTo(color);

                sub.copyTo(blurredMat.submat(roi));
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