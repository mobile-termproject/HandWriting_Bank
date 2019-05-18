package org.androidtown.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int SDCARD_PERMISSION = 1,
            FILE_PICKER_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkStoragePermission();
        initUI();
    }

    /*permission 보내는 코드 */
    void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        SDCARD_PERMISSION);
            }
        }

    }

    /*책클릭했을때*/
    void initUI() {
        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFile();
            }
        });
    }

    /*location에 폴더 이름*/
    void pickFile() {
        Intent intent = new Intent(this, BookFileList.class);
        intent.putExtra("location", "/혜연/");
        startActivityForResult(intent, FILE_PICKER_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILE_PICKER_CODE) {
            if (resultCode == Activity.RESULT_OK && intent.hasExtra("data")) {

            } else if (resultCode == Activity.RESULT_CANCELED) {
            }
        }

    }

}
