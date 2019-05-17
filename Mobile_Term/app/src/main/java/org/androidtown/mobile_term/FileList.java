package org.androidtown.mobile_term;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FileList extends AppCompatActivity {

    private Spinner spinner;
    String FolderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);

        Intent myintent = getIntent();
        FolderName = myintent.getExtras().getString("name"); //MainActivity에서 받아온 선택 폴더 이름 - 앞에 / 없음

        setToolbar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void setToolbar() {
        Toolbar toolbar2 = (Toolbar) findViewById(R.id.toolbar2);
        spinner = (Spinner)findViewById(R.id.spinner_nav);

        setSupportActionBar(toolbar2);

        getSupportActionBar().setDisplayShowTitleEnabled(false); //기존 타이틀은 안보여주게
        TextView text = (TextView)findViewById(R.id.text);
        text.setText(FolderName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //뒤로가기 버튼 설정

        addItemToSpinner();
    }

    public void addItemToSpinner() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("전체");
        list.add("PDF");
        list.add("MP3");

        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(getApplicationContext(),list);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sort = parent.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(),"Selected : " + sort,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ;
            }
        });
    }
}
