package org.androidtown.mobile_term;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class Gachon_Login extends AppCompatActivity {

    EditText logid;
    EditText logpass;
    Button login;
    Button cancel;

    String loginID;
    String loginPASS;

    int check = 0;
    ArrayList<String> arr = new ArrayList<String>();

    private FirebaseUser user;
    private FirebaseAuth mAuth; //Firebase로 로그인한 사용자 정보 알기 위해
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gachon__login);

        mAuth = FirebaseAuth.getInstance(); // 인증
        user = mAuth.getCurrentUser(); //현재 로그인한 유저

        logid = (EditText)findViewById(R.id.type_id);
        logpass = (EditText)findViewById(R.id.type_password);
        login = (Button)findViewById(R.id.loginBt);
        cancel = (Button)findViewById(R.id.cancelBt);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginID = logid.getText().toString();
                loginPASS = logpass.getText().toString();

                new Description().execute();

                finish();
            }
        });
    }

    private class Description extends AsyncTask<Void,Void,Void> {
        private ProgressDialog progressDialog;

        @Override
        protected  void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(Gachon_Login.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("잠시 기다려 주세요.");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            /*String tr = user.getEmail();
            int get = tr.indexOf("@");
            tr = tr.substring(0,get);
            int pic_num = 0;*/

            try {
                Connection.Response res = Jsoup.connect("https://cyber.gachon.ac.kr/login.php")
                        .data("username",loginID,"password",loginPASS)
                        .method(Connection.Method.POST)
                        .execute();

                 Map<String, String> loginCookie = res.cookies();

                Document doc = Jsoup.connect("http://cyber.gachon.ac.kr")
                        .cookies(loginCookie)
                        .get();

                Elements els = doc.select("div[class=course-title]");
                check = els.size();

                Intent myIntent = new Intent(Gachon_Login.this,select_course.class);

                for(Element elem : els) {
                    String mytitle = elem.select("h3").text();
                    arr.add(new String(mytitle));
                }

                myIntent.putStringArrayListExtra("arr",arr);
                startActivity(myIntent); //select_course

            } catch (IOException e) { //네트워크 혹은 홈페이지 오류
                Toast.makeText(getApplicationContext(),"로그인 오류", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(check==0)
                Toast.makeText(getApplicationContext(),"ID/PW 오류 혹은 저장된 과목이 없음", Toast.LENGTH_SHORT).show();
        }

    }

}
