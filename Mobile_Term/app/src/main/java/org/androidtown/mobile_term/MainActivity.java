package org.androidtown.mobile_term;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseUser user;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth; //Firebase로 로그인한 사용자 정보 알기 위해
    Bitmap bitmap; //프로필 uri이용해 bitmap으로

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private ChildEventListener mChild;

    static ArrayList<Book> data = new ArrayList<>();
    GridViewAdapter adapter;
    GridView gridView;

    private Integer[] colors =
            {R.drawable.red, R.drawable.orange, R.drawable.yellow,R.drawable.green,R.drawable.blue,R.drawable.purple,R.drawable.pink,R.drawable.black};

    private static final int FILE_PICKER_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance(); // 인증
        user = mAuth.getCurrentUser(); //현재 로그인한 유저

        init();
        navisetting();
        initDatabase();

        String tr = user.getEmail();
        int get = tr.indexOf("@");
        tr = tr.substring(0,get);
        databaseReference = firebaseDatabase.getReference(tr);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adapter.clear();

                for (DataSnapshot messageData : dataSnapshot.getChildren()) { // child 내에 있는 데이터만큼 반복합니다.
                    String hello = messageData.getValue().toString();
                    String num = messageData.getValue().toString();

                    hello = hello.substring(0,hello.length()-1); // 이름

                    num = num.substring(num.length()-1);
                    int col_num = Integer.parseInt(num);

                    data.add(new Book(hello,colors[col_num],col_num));
                }
                adapter.refresh();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("TAG: ", "Failed to read value", databaseError.toException());
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //기존 타이틀은 안보여주게

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab); // 책 추가하는 플로팅 버튼
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this,Book_Dialog.class);
                startActivity(myIntent); //Book_Dialog
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout); // 네이게이션바 드로어
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view); // 네이게이션바 세팅
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu); //액션바 오른쪽 점3개
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.word) {
            // Handle the camera action
        } else if (id == R.id.test) {

        } else if (id == R.id.scan) {

        } else if (id == R.id.app_use) {

        } else if (id == R.id.logout) { //로그아웃 선택 시 로그인 화면으로 이동
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
            alert_confirm.setMessage("로그아웃 하시겠습니까?").setCancelable(false).setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(MainActivity.this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(getApplicationContext(), SplashLogin.class));
                        }
                    }
            );
            alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(MainActivity.this, "취소", Toast.LENGTH_LONG).show();
                }
            });
            alert_confirm.show();
        } else if (id == R.id.revoke) { //탈퇴
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
            alert_confirm.setMessage("정말 계정을 삭제 할까요?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
                            users.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(MainActivity.this, "계정이 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                    startActivity(new Intent(getApplicationContext(), SplashLogin.class));
                                }
                            });

                            String tr = user.getEmail();
                            int get = tr.indexOf("@");
                            tr = tr.substring(0,get);

                            databaseReference = firebaseDatabase.getReference(tr);
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    ;
                                }
                            });
                        }
                    }
            );
            alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(MainActivity.this, "취소", Toast.LENGTH_LONG).show();
                }
            });
            alert_confirm.show();
        } else if (id == R.id.gachon_login) { //가천대 로그인
            Intent myIntent = new Intent(MainActivity.this,Gachon_Login.class);
            startActivity(myIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected  void onResume(){
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    public  void init() {
        gridView = (GridView)findViewById(R.id.book);
        adapter = new GridViewAdapter(data, this);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myIntent = new Intent(MainActivity.this,FileList.class);
                myIntent.putExtra("location", "/"+data.get(position).getName()+"/");
                myIntent.putExtra("name",data.get(position).getName());
                startActivityForResult(myIntent, FILE_PICKER_CODE);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                view.setBackgroundColor(Color.GRAY);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(data.get(position).getName() + " 과목을 삭제하시겠습니까?");
                builder.setMessage("삭제 시 핸드폰에 저장된 폴더는 유지됩니다.");
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String tr = user.getEmail();
                        int get = tr.indexOf("@");
                        tr = tr.substring(0,get);

                        final String check = data.get(position).getName() + data.get(position).getPicnum();

                        databaseReference = firebaseDatabase.getReference(tr);
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                                    String tag = snapshot.getValue().toString();
                                    if(check.equals(tag))
                                        snapshot.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                    ;
                            }
                        });
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                });
                view.setBackgroundColor(Color.WHITE);
                builder.show();
                return true;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILE_PICKER_CODE) {
            if (resultCode == Activity.RESULT_OK && intent.hasExtra("data")) {

            } else if (resultCode == Activity.RESULT_CANCELED) {
            }
        }
    }

    public void navisetting() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view); // 네비게이션바 연동
        navigationView.setNavigationItemSelectedListener(this);

        View nav_header_view = navigationView.getHeaderView(0); // 네비게이션 헤더 연동

        if(user != null) {
        TextView nav_header_name_text = (TextView) nav_header_view.findViewById(R.id.username); // 구글로그인 사용자 이름
        nav_header_name_text.setText(user.getDisplayName());
        TextView nav_header_email_text = (TextView) nav_header_view.findViewById(R.id.useremail); // 구글로그인 사용자 이메일
        nav_header_email_text.setText(user.getEmail());


        //프로필 사진 세팅 이미지뷰
        ImageView user_profile = (ImageView) nav_header_view.findViewById(R.id.imageView);
        Thread mThread= new Thread(){
            @Override
            public void run() {
                try{
                    //현재로그인한 사용자 정보를 통해 PhotoUrl 가져오기
                    URL url = new URL(user.getPhotoUrl().toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                } catch (MalformedURLException ee) {
                    ee.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        mThread.start();
        try{
            mThread.join();
            //변환한 bitmap적용
            user_profile.setImageBitmap(bitmap);
        }catch (InterruptedException e){
            e.printStackTrace();
        }}
    }

    public void initDatabase() {
        String tr = user.getEmail();
        int get = tr.indexOf("@");
        tr = tr.substring(0,get);
        databaseReference = firebaseDatabase.getReference(tr);
        mChild = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addChildEventListener(mChild);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(mChild);
    }
}