package org.androidtown.mobile_term;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static android.os.ParcelFileDescriptor.open;

public class PDFActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener, OnDrawListener, ColorPickerDialogListener {

    public static int startbrush = 0;
    String FILE_NAME;
    String FILE_TITLE;
    PDFView pdfView;
    int pageNumber = 0;
    String folder;
    Toolbar toolbar;
    Spinner spinner;
    private DrawingView mDrawingView;
    private SeekBar mBrushStroke;
    private SharedPreferences preferences;
    private int selectedColor;
    static String[] arrayList;
    String[] testArray;
    int State = -1; //만약 DB가 이미 존재하면 0 존재하지 않아서 새로 생성한다면 1
    int Pressed = 0; //PDF 뷰어 가 뒤일때 0 앞일 때 1

    DBHelper dbHelper;

    int count;
    static Bitmap bitmap;
    static int num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(Utils.createCheckerBoard(getResources(), 30));
        setContentView(R.layout.activity_play);

        Intent intent = getIntent();
        FILE_NAME = intent.getStringExtra("name");
        folder = intent.getStringExtra("folder");
        FILE_TITLE = FILE_NAME;

        dbHelper = new DBHelper(getApplicationContext(), "PDFWRITING.db", null, 1);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mDrawingView = (DrawingView) findViewById(R.id.drawingView);

        mDrawingView.setShape(R.drawable.ic_action_undo, R.drawable.ic_action_brush);
        mDrawingView.setDrawingColor(Color.BLACK);
        mBrushStroke = (SeekBar) findViewById(R.id.brush_stroke);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        pageCount();

        //dbHelper.delete(FILE_TITLE);
        //Log.i("정보","삭제되었습니다");

        arrayList = new String[count];
        Arrays.fill(arrayList, "");

        if (dbHelper.search(FILE_TITLE)) { // db에 내용이 있음
            for (int i = 0; i < count; i++) {
                Log.i("정보", "반복문 입성");
                String getvalue = dbHelper.getResult(FILE_TITLE, i);
                Log.i("정보", "스트링저장");
                arrayList[i] += getvalue;
                Log.i("정보", arrayList[i]);
            }
            State = 0;
        } else { //db에 내용 없음
            //arrayList = new String[count];
            //Arrays.fill(arrayList,"");
            State = 1;
        }
        //testArray = new String[count];
        //Arrays.fill(testArray,"");
        /*toolbar 설정*/
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        /*초기 상태 지정 (가로모드, pdfview호출)*/
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        displayFromAsset_landscape(FILE_NAME, folder);
        pdfView.bringToFront();
        /*브러쉬 굵기*/
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

    /*toolbar*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.pdf_toolbar, menu);
        MenuItem item = menu.findItem(R.id.action_spinner);
        spinner = (Spinner) item.getActionView();
        int[] spinnerImages = new int[]{
                R.drawable.ic_fiber_manual_record_black_24dp
                , R.drawable.ic_edit_black_24dp
                , R.drawable.ic_edit_green_24dp};
        HighlightAdapter adapter = new HighlightAdapter(this, spinnerImages);
        adapter.setDropDownViewResource(R.layout.dropdown_highlight);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(onItemSelectedListener);
        return super.onCreateOptionsMenu(menu) | true;
    }

    public void pageCount() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folder + "/" + FILE_NAME);
        try {
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfiumCore pdfiumCore = new PdfiumCore(getApplicationContext());
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            count = pdfiumCore.getPageCount(pdfDocument);
            Log.i("정보", count + "count");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*형광펜*/
    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter, View v, int i, long lng) {
            if (i == 0) {
                if (startbrush == 0) {
                    mDrawingView.setDrawingColor(Color.BLACK);
                    pdfView.bringToFront();
                    startbrush++;
                } else {
                    mDrawingView.setDrawingColor(Color.parseColor("#5EFFF800"));
                    mDrawingView.bringToFront();
                }
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
    boolean strokeResult = true;

    /*편집, 브러쉬색, 굵기, 전, 후, 저장기능 toolbar*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_button: //이거눌러야지 편집 가능
                if (result) {
                    mDrawingView.bringToFront();
                    result = false;
                    Pressed = 0;
                } else {
                    pdfView.bringToFront();
                    result = true;

                    mDrawingView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    mDrawingView.setDrawingCacheEnabled(true);
                    mDrawingView.buildDrawingCache();
                    Bitmap viewCache = mDrawingView.getDrawingCache();
                    bitmap = viewCache.copy(viewCache.getConfig(), false);
                    mDrawingView.setDrawingCacheEnabled(false);

                    String image = Convert.bitmapToString(getApplicationContext(), bitmap);
                    //Log.i("정보",0+"번 칸에 비트맵 이미지가 저장되었습니다.");
                    arrayList[pageNumber] = image;
                    //testArray[pageNumber] = pageNumber + "번째 배열";
                    Pressed = 1;
                }
                return true;
            case R.id.action_draw: //굵기
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
            case R.id.action_save: { //이미지 저장하는곳

                if (Pressed == 0) {
                    mDrawingView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    mDrawingView.setDrawingCacheEnabled(true);
                    mDrawingView.buildDrawingCache();
                    Bitmap viewCache = mDrawingView.getDrawingCache();
                    bitmap = viewCache.copy(viewCache.getConfig(), false);
                    mDrawingView.setDrawingCacheEnabled(false);

                    String image = Convert.bitmapToString(getApplicationContext(), bitmap);
                    //Log.i("정보",0+"번 칸에 비트맵 이미지가 저장되었습니다.");
                    arrayList[pageNumber] = image;
                }

                if (State == 1) {
                    for (int i = 0; i < count; i++) {
                        dbHelper.insert(FILE_TITLE, i, arrayList[i]);
                        Log.i("정보", i + "값이 들어감");
                    }
                } else if (State == 0) {
                    dbHelper.delete(FILE_TITLE);
                    for (int i = 0; i < count; i++) {
                        Log.i("정보", "업데이트 들어감");
                        dbHelper.insert(FILE_TITLE, i, arrayList[i]);
                        Log.i("정보", "업데이트 하나 받아옴");
                    }
                }

                finish();
                /*mDrawingView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                mDrawingView.setDrawingCacheEnabled(true);
                mDrawingView.buildDrawingCache();
                Bitmap viewCache = mDrawingView.getDrawingCache();
                bitmap = viewCache.copy(viewCache.getConfig(), false);
                mDrawingView.setDrawingCacheEnabled(false);

                String image = Convert.bitmapToString(this, bitmap);

                num = 2;
                Intent intent = new Intent(this,Testing.class);
                intent.putExtra("image",image);
                startActivity(intent);*/

                //new SaveTask().execute(bitmap);
            }
            return true;
            case R.id.action_cancel:
                mDrawingView.clearDrawing();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*color picker 색깔 선택*/
    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        selectedColor = color;
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
            mProgressDialog = new ProgressDialog(PDFActivity.this);
            mProgressDialog.setMessage(getString(R.string.saving));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(File result) {
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(PDFActivity.this, getString(R.string.saved_as) +
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

    private void displayFromAsset_landscape(String pdfFileName, String folder) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folder + "/" + pdfFileName);
        pdfView.fromFile(file)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .swipeHorizontal(true)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .onDraw(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
        pdfView.setBackgroundColor(getResources().getColor(R.color.pdf_background));
    }


    @Override
    public void onPageChanged(int page, int pageCount) {
        Log.i("정보", "받아온 page 값 : " + page);

        if (pageNumber != page) {

            if (arrayList[page].equals("")) {
                Log.i("정보", "기존값 없음");
                num = 0;
            } else {
                num = 2;
                Log.i("정보", "기존값 있음");
                bitmap = Convert.stringToBitmap(arrayList[page]);
                //Log.i("정보",testArray[page]);
            }
        } else {
            if (arrayList[page].equals("")) {
                num = 0;
                Log.i("정보", "기존값 없음");
            } else {
                num = 2;
                Log.i("정보", "기존값 있음");
                bitmap = Convert.stringToBitmap(arrayList[0]);
                //Log.i("정보",testArray[0]);
            }
        }

        pageNumber = page;

        mDrawingView.clearDrawing();
        //mDrawingView.setShape(R.drawable.ic_action_undo, R.drawable.ic_action_brush);
        mDrawingView.setShape(R.drawable.inner, R.drawable.outer);
        mDrawingView.setDrawingColor(Color.BLACK);
    }


    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
        //Log.i("정보", pageWidth + "/" + pageHeight);
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e("내용: ", String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }
}