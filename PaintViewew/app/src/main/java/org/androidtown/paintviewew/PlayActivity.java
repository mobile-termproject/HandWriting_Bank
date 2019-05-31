package org.androidtown.paintviewew;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;


import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MotionEvent;

import android.view.Window;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.github.clans.fab.FloatingActionButton;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.shockwave.pdfium.PdfDocument;


import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;


public class PlayActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener, OnDrawListener, ColorPickerDialogListener {
    boolean result_rotation = true;
    String SAMPLE_FILE = "4년치 품사변화자료.pdf";
    PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;
    FloatingActionButton screen_rotation;


    Toolbar toolbar;
    private static final String TAG = "HelloAndroid";

    private DrawingView mDrawingView;
    private ViewGroup mBrushPanel;
    private ViewGroup mBrushColors;
    private SeekBar mBrushStroke;

    private SharedPreferences preferences;
    private int selectedColor;
    // see: http://stackoverflow.com/questions/25758294/how-to-fill-different-color-on-same-area-of-imageview-color-over-another-color
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawable(Utils.createCheckerBoard(getResources(), 30));
        setContentView(R.layout.activity_play);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mDrawingView = (DrawingView) findViewById(R.id.drawingView);
        mDrawingView.setShape(R.drawable.ic_action_undo, R.drawable.ic_action_brush);
        mDrawingView.setDrawingColor(Color.BLACK);
        mBrushStroke = (SeekBar) findViewById(R.id.brush_stroke);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        screen_rotation = (FloatingActionButton) findViewById(R.id.floating_screen_rotation);
        screen_rotation.setColorNormal(R.color.screen_rotation);
        screen_rotation.setColorRipple(R.color.screen_rotation);
        screen_rotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (result_rotation) {
                    displayFromAsset_landscape(SAMPLE_FILE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    result_rotation = false;
                    Log.i("정보", "가로됐다.");
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    displayFromAsset_list(SAMPLE_FILE);
                    result_rotation = true;
                    Log.i("정보", "세로됐다.");
                }
            }
        });
        displayFromAsset_list(SAMPLE_FILE);
        pdfView.bringToFront();
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        mBrushStroke.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDrawingView.setDrawingStroke(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
        });
        mBrushStroke.setProgress(30);

    }

    private boolean isLandscape() {
        return getResources().getBoolean(R.bool.is_landscape);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_play, menu);
        MenuItem item = menu.findItem(R.id.action_spinner);
        spinner = (Spinner) item.getActionView();
        int[] spinnerImages = new int[]{
                R.drawable.ic_fiber_manual_record_black_24dp
                , R.drawable.ic_edit_black_24dp
                , R.drawable.ic_edit_green_24dp};
        CustomAdapter adapter = new CustomAdapter(this, spinnerImages);
        adapter.setDropDownViewResource(R.layout.dropdown_highlight);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(onItemSelectedListener);
        return super.onCreateOptionsMenu(menu) | true;
    }
    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> adapter, View v, int i, long lng) {
            if (i == 0) {
               mDrawingView.setDrawingColor(Color.parseColor("#5EFFF800"));
               mDrawingView.bringToFront();
            } else if (i == 1) {
                mDrawingView.setDrawingColor(Color.parseColor("#3111FF00"));
                mDrawingView.bringToFront();
            } else if (i == 2) {
                mDrawingView.setDrawingColor(Color.parseColor("#2CFF02F2"));
                mDrawingView.bringToFront();
            }
            mDrawingView.bringToFront();
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // Do nothing
        }
    };

    boolean result = true;
    boolean current_show = true;
    boolean strokeResult = true;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_one:
                if (current_show) {
                    displayFromAsset_one(SAMPLE_FILE);
                    item.setIcon(R.drawable.show_listpdf);
                    current_show = false;
                } else {
                    displayFromAsset_list(SAMPLE_FILE);
                    item.setIcon(R.drawable.extend_pdf);
                    current_show = true;
                }
                return true;
            case R.id.action_edit_button:
                if (result) {
                    mDrawingView.bringToFront();
                    result = false;
                } else {
                    pdfView.bringToFront();
                    result = true;
                }
                return true;
            case R.id.action_draw:
                if (strokeResult) {
                    mBrushStroke.bringToFront();
                    mDrawingView.bringToFront();
                    //  strokeResult = false;
                }
                return true;
            case R.id.action_brush:
                showColorPickerDialog();
                mDrawingView.bringToFront();
                //   mBrushStroke.bringToFront();
                result = false;
                return true;
            case R.id.action_eraser:
                mDrawingView.enableEraser();
                return true;

            case R.id.action_undo:
                mDrawingView.undoOperation();
                return true;
            case R.id.action_redo:
                mDrawingView.redoOperation();
                return true;
            case R.id.action_save: {
                mDrawingView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                mDrawingView.setDrawingCacheEnabled(true);
                mDrawingView.buildDrawingCache();
                Bitmap viewCache = mDrawingView.getDrawingCache();
                Bitmap bitmap = viewCache.copy(viewCache.getConfig(), false);
                mDrawingView.setDrawingCacheEnabled(false);
                new SaveTask().execute(bitmap);
            }
            return true;
            case R.id.action_cancel:
                mDrawingView.clearDrawing();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }


    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {

        selectedColor = color;
        Log.i("정보", color + "");
        mDrawingView.setDrawingColor(Color.parseColor(toHex(color)));
        if (!arrayContains(ColorPickerDialog.MATERIAL_COLORS, color)) {
            // add the custom color to shared preferences so we can use it in the presets later
            Set<String> presets = preferences.getStringSet("custom_colors", null);
            if (presets == null) {
                presets = new LinkedHashSet<>();
            }
            presets.add(toHex(color));
            preferences.edit().putStringSet("custom_colors", presets).apply();
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    private void showColorPickerDialog() {
        // colors used in presets
        int[] colors = ColorPickerDialog.MATERIAL_COLORS;

        // replace any custom colors
        Set<String> customColors = preferences.getStringSet("custom_colors", null);
        if (customColors != null) {
            int index = 0;

            // reverse order
            List<String> list = new ArrayList<>(customColors);
            Collections.sort(list, Collections.<String>reverseOrder());
            customColors = new LinkedHashSet<>(list);

            for (String hex : customColors) {
                colors[index++] = Color.parseColor(hex);
                if (index >= colors.length) break;
            }
        }

        // show the dialog
        ColorPickerDialog.newBuilder().setDialogType(ColorPickerDialog.TYPE_CUSTOM).
                setAllowPresets(false).
                setShowAlphaSlider(true).
                setColor(selectedColor).show(this);

    }

    private String toHex(int color) {
        if (Color.alpha(color) != 255) {
            return "#" + Integer.toHexString(color).toUpperCase(Locale.US);
        } else {
            return "#" + String.format("%06X", 0xFFFFFF & color).toUpperCase(Locale.US);
        }
    }

    private boolean arrayContains(int[] array, int value) {
        if (array != null) {
            int length = array.length;
            for (int i = 0; i < length; i++) {
                if (array[i] == value) {
                    return true;
                }
            }
        }
        return false;
    }

    private class SaveTask extends AsyncTask<Bitmap, Void, File> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(PlayActivity.this);
            mProgressDialog.setMessage(getString(R.string.saving));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(File result) {
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(PlayActivity.this, getString(R.string.saved_as) +
                        result.getName(), Toast.LENGTH_LONG).show();
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
                    if (params[0].compress(CompressFormat.PNG, 75, stream)) {
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

    private void displayFromAsset_one(String assetFileName) {
        pdfFileName = assetFileName;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/KakaoTalkDownload/mobile term.pdf");
        pdfView.fromFile(file)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .swipeHorizontal(true)
                .onPageChange(this)
                .onDraw(this)
                //   .enableAnnotationRendering(true)
                .onLoad(this)
                .pageFitPolicy(FitPolicy.WIDTH)
                .scrollHandle(new DefaultScrollHandle(this))
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .load();
        pdfView.setBackgroundColor(getResources().getColor(R.color.pdf_background));


    }

    private void displayFromAsset_list(String assetFileName) {
        pdfFileName = assetFileName;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/KakaoTalkDownload/mobile term.pdf");
        pdfView.fromFile(file)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .onPageChange(this)
                .onDrawAll(this)
                //   .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
        pdfView.setBackgroundColor(getResources().getColor(R.color.pdf_background));
        //     Log.i("정보", String.valueOf(pdfView.getZoom()));

    }

    private void displayFromAsset_landscape(String assetFileName) {
        pdfFileName = assetFileName;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/KakaoTalkDownload/mobile term.pdf");
        pdfView.fromFile(file)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .swipeHorizontal(true)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
        pdfView.setBackgroundColor(getResources().getColor(R.color.pdf_background));

        Log.i("정보", "들어왔따 landscape");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //  displayFromAsset_landscape(SAMPLE_FILE);
        super.onConfigurationChanged(newConfig);
        Log.i("정보", "들어왔따 config");
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //  displayFromAsset_landscape(SAMPLE_FILE);
        } else {
            // displayFromAsset_list(SAMPLE_FILE);
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
    }


    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    Bitmap mBitmap;


    /* protected void onDraw(Canvas canvas) {
         if (mBitmap != null) {
             canvas.drawBitmap(mBitmap, 0, 0, null);
         }
     }*/
    float x;
    float y;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        Log.i("정보", event.getAction() + "탭성공");
        Log.i("정보", x + "ㄹㅇ2");
        Log.i("정보", y + "ㄹㅇ2");
       /* switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getHistorySize(); i++) {
                 //   mCanvas.drawLine(lastX, lastY, X, Y, mPaint);
                }
          mCanvas.drawLine(lastX, lastY, X, Y, mPaint);
                onLayerDrawn(mCanvas,1080 ,763,2);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
          //      pdfView.onTouchEvent()
        }*/
        return true;
    }

    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
      /*  Log.i("정보", "클릭!");
          //  canvas.drawLine(10, 10, 100, 100, mPaint);
            Log.i("정보", pageWidth+" " +pageHeight+" "+displayedPage);
         //    canvas.drawLine(lastX, lastY, X, Y, mPaint);
            float xLeft = -1 * displayedPage * pageWidth;
            float yTop = displayedPage * pageHeight;
            float xRight = (-1 * displayedPage * pageWidth) + pageWidth;
            float yBottom = (displayedPage + 1) * pageHeight;
           // canvas.drawLine(xLeft, yBottom, xRight, yBottom, mPaint);*/
        Log.i("정보", pageWidth + "/" + pageHeight);
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }
}