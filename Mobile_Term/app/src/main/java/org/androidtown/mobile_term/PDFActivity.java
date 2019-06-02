package org.androidtown.mobile_term;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
    String[] arrayList;
    int State = -1; //만약 DB가 이미 존재하면 0 존재하지 않아서 새로 생성한다면 1
    int Pressed = 0; //PDF 뷰어 가 뒤일때 0 앞일 때 1

    DBHelper dbHelper;
    String testing = "writing";
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
        Log.i("정보", "파일 이름" + FILE_TITLE);
        dbHelper = new DBHelper(getApplicationContext(), "PDFWRITING.db", null, 1);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mDrawingView = (DrawingView) findViewById(R.id.drawingView);

        mDrawingView.setShape(R.drawable.ic_action_undo, R.drawable.ic_action_brush);
        mDrawingView.setDrawingColor(Color.BLACK);
        mBrushStroke = (SeekBar) findViewById(R.id.brush_stroke);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        pageCount();

        //Log.i("정보","삭제되었습니다");

        arrayList = new String[count];
        Arrays.fill(arrayList, "");
        //dbHelper.delete(FILE_TITLE);
        if (dbHelper.search(FILE_TITLE, testing)) { // db에 내용이 있음
            for (int i = 0; i < count; i++) {
                Log.i("정보", "반복문 입성");
                String getvalue = dbHelper.getResult(FILE_TITLE, i);
                Log.i("정보", "스트링저장");
                arrayList[i] += getvalue;
                Log.i("정보", arrayList[i]);
            }
            State = 0;
        } else { //db에 내용 없음
            State = 1;
        }
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

    boolean result = true;
    boolean strokeResult = true;

    /*편집, 브러쉬색, 굵기, 전, 후, 저장기능 toolbar*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_button: //이거눌러야지 편집 가능
                if (result) {
                    mDrawingView.bringToFront();
                    item.setIcon(getResources().getDrawable(R.drawable.edit_icon));
                    result = false;
                    Pressed = 0;
                } else {
                    pdfView.bringToFront();
                    item.setIcon(getResources().getDrawable(R.drawable.edit_icon2));
                    result = true;

                    mDrawingView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    mDrawingView.setDrawingCacheEnabled(true);
                    mDrawingView.buildDrawingCache();
                    Bitmap viewCache = mDrawingView.getDrawingCache();
                    bitmap = viewCache.copy(viewCache.getConfig(), false);
                    mDrawingView.setDrawingCacheEnabled(false);

                    String image = Convert.bitmapToString(getApplicationContext(), bitmap);
                    arrayList[pageNumber] = image;
                    Pressed = 1;
                }
                return true;
            case R.id.action_draw: //굵기
                if (strokeResult) {
                    mBrushStroke.bringToFront();
                    mBrushStroke.setVisibility(View.VISIBLE);
                    mDrawingView.bringToFront();
                    //  strokeResult = false;
                    strokeResult = false;
                }
                else{
                    mBrushStroke.setVisibility(View.INVISIBLE);
                    strokeResult = true;
                }
                return true;
            case R.id.action_brush:
                showColorPickerDialog();
                mDrawingView.bringToFront();
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
                        dbHelper.insert(FILE_TITLE, i, arrayList[i],testing);
                        Log.i("정보", i + "값이 들어감");
                    }
                } else if (State == 0) {
                    dbHelper.delete(FILE_TITLE);
                    for (int i = 0; i < count; i++) {
                        Log.i("정보", "업데이트 들어감");
                        dbHelper.insert(FILE_TITLE, i, arrayList[i],testing);
                        Log.i("정보", "업데이트 하나 받아옴");
                    }
                }

                finish();
            }
            return true;
            case R.id.action_cancel:
                mDrawingView.clearDrawing();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    boolean selectResult = false;
    /*color picker 색깔 선택*/
    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        selectedColor = color;
        selectResult = true;
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
        if(selectResult == false){
            selectedColor = Color.BLACK;
        }


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