package org.androidtown.mobile_term;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TestMake extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener, OnDrawListener {
    String FILE_NAME;
    PDFView pdfView;
    Integer pageNumber = 0;
    String folder;
    Toolbar toolbar;
    Button button;
    private DrawingView mDrawingView;
    private SeekBar mBrushStroke;
    boolean result = true;
    String state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(Utils.createCheckerBoard(getResources(), 30));
        setContentView(R.layout.activity_make_test);

        Intent intent = getIntent();
        FILE_NAME = intent.getStringExtra("filename");
        Log.i("정보",FILE_NAME+"인텐트 넘어옴" );
        folder = intent.getStringExtra("folder");
        state = intent.getStringExtra("state");
        mDrawingView = (DrawingView) findViewById(R.id.test_drawingView);
        mDrawingView.setShape(R.drawable.ic_action_undo, R.drawable.ic_action_brush);

        if(state.equals("false"))
            mDrawingView.setDrawingColor(Color.WHITE);
        mBrushStroke = (SeekBar) findViewById(R.id.test_brush_stroke);
        pdfView = (PDFView) findViewById(R.id.test_pdfView);
        toolbar = (Toolbar) findViewById(R.id.test_toolbar);
        pageCount();
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
        this.invalidateOptionsMenu();

    }

    Button.OnClickListener click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (result) {
                mDrawingView.bringToFront();
                button.setText("시험지 보기");
                result = false;
            } else {
                pdfView.bringToFront();
                button.setText("정답 확인");
                result = true;
            }

        }
    };

    /*toolbar*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.test_toolbar, menu);
        MenuItem item = menu.findItem(R.id.action_edit_button);
        button = (Button) item.getActionView();
        button.setText("시험지보기");
        button.setOnClickListener(click);

        if (state.equals("true")) {
            MenuItem a = menu.findItem(R.id.action_draw);
            a.setVisible(false);
            MenuItem b = menu.findItem(R.id.action_eraser);
            b.setVisible(false);
            MenuItem c = menu.findItem(R.id.action_cancel);
            c.setVisible(false);
            MenuItem d = menu.findItem(R.id.action_undo);
            d.setVisible(false);
            MenuItem e = menu.findItem(R.id.action_redo);
            e.setVisible(false);
            MenuItem f = menu.findItem(R.id.action_save);
            f.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu) | true;
    }

    public void pageCount() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folder + "/" + FILE_NAME);
        try {
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfiumCore pdfiumCore = new PdfiumCore(getApplicationContext());
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            int count = pdfiumCore.getPageCount(pdfDocument);
            Log.i("정보", count + "count");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean strokeResult = true;

    /*편집, 브러쉬색, 굵기, 전, 후, 저장기능 toolbar*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_draw: //굵기
                if (strokeResult) {
                    mBrushStroke.bringToFront();
                    mDrawingView.bringToFront();
                    //  strokeResult = false;
                }
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
            case R.id.action_save:  //이미지 저장하는곳
                mDrawingView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                mDrawingView.setDrawingCacheEnabled(true);
                mDrawingView.buildDrawingCache();
                Bitmap viewCache = mDrawingView.getDrawingCache();
                Bitmap bitmap = viewCache.copy(viewCache.getConfig(), false);
                mDrawingView.setDrawingCacheEnabled(false);
                new SaveTask().execute(bitmap);

                return true;
            case R.id.action_cancel:
                mDrawingView.clearDrawing();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class SaveTask extends AsyncTask<Bitmap, Void, File> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(TestMake.this);
            mProgressDialog.setMessage(getString(R.string.saving));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(File result) {
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(TestMake.this, getString(R.string.saved_as) +
                        result.getName(), Toast.LENGTH_LONG).show();
                check();
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

    public void check() {
        AlertDialog.Builder ad = new AlertDialog.Builder(TestMake.this);
        ad.setTitle("알림");
        ad.setMessage("시험지 생성이 완료되었습니다.\n생성된 시험지를 확인하시겠습니까? ");
        ad.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                FileList fl = (FileList) FileList._FileList;
                fl.finish();
                makeTest(FILE_NAME, folder);

                Intent intent = new Intent(getApplicationContext(), TestFileList.class);
                intent.putExtra("filename", FILE_NAME);
                intent.putExtra("folder", folder);
                startActivity(intent);
            }
        });
        ad.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                makeTest(FILE_NAME, folder);
                finish();
            }
        });
        ad.show();
    }

    private void makeTest(String FILE_NAME, String folder) {
        String newName = "[시험지]" + FILE_NAME;
        String TestFolderpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/시험지";
        String Location = TestFolderpath + "/" + newName;
        String beforelocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folder + "/" + FILE_NAME;
        File requestedFolder = new File(TestFolderpath);
        if (requestedFolder.exists()) {
            copyFile(beforelocation, Location);
        } else {
            createNewFolder(TestFolderpath);
            copyFile(beforelocation, Location);
        }
    }

    void createNewFolder(String filename) {
        try {
            File file = new File(filename);
            file.mkdirs();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error:" + e.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private boolean copyFile(String strSrc, String save_file) {
        File file = new File(strSrc);

        boolean result;
        if (file != null && file.exists()) {

            try {
                FileInputStream fis = new FileInputStream(file);
                FileOutputStream newfos = new FileOutputStream(save_file);
                int readcount = 0;
                byte[] buffer = new byte[1024];

                while ((readcount = fis.read(buffer, 0, 1024)) != -1) {
                    newfos.write(buffer, 0, readcount);
                }
                newfos.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            result = true;
        } else {
            result = false;
        }
        return result;
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
        pageNumber = page;
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