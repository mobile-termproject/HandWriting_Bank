package org.androidtown.mobile_term;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Testing extends AppCompatActivity {

    static Bitmap bitmap;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        button = (Button)findViewById(R.id.save);

        Intent intent = getIntent();
        String image = intent.getStringExtra("image");
        bitmap = Convert.stringToBitmap(image);

        final DrawingView mDrawingView = (DrawingView)findViewById(R.id.drawingView);
        mDrawingView.setShape(R.drawable.ic_action_undo, R.drawable.ic_action_brush);
        mDrawingView.setDrawingColor(Color.BLACK);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawingView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                mDrawingView.setDrawingCacheEnabled(true);
                mDrawingView.buildDrawingCache();
                Bitmap viewCache = mDrawingView.getDrawingCache();
                bitmap = viewCache.copy(viewCache.getConfig(), false);
                mDrawingView.setDrawingCacheEnabled(false);
                new SaveTask().execute(bitmap);
            }
        });
    }

    private class SaveTask extends AsyncTask<Bitmap, Void, File> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(Testing.this);
            mProgressDialog.setMessage(getString(R.string.saving));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(File result) {
            mProgressDialog.dismiss();
            if (result != null) {
            }
        }

        @SuppressLint("SimpleDateFormat")
        @Override
        protected File doInBackground(Bitmap... params) {
            String name = new SimpleDateFormat("'Painter_'yyyy-MM-dd_HH-mm-ss.S'.png'").format(new Date());
            File result = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name);

            FileOutputStream stream = null;
            try {
                try {
                    stream = new FileOutputStream(result);
                    if (params[0].compress(Bitmap.CompressFormat.PNG, 75, stream)) {
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(result)));
                    } else {
                        result = null;
                    }
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            } catch (IOException e) {
                result = null;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            return result;
        }
    }
}
