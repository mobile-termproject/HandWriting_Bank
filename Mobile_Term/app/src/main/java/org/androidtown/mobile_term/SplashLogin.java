package org.androidtown.mobile_term;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class SplashLogin extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    SignInButton Google_Login;

    private static final int RC_SIGN_IN = 1000;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private Handler mHandler = new Handler();

    ImageView splash;
    ImageView login;

    private static final int SDCARD_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_login);

        Google_Login = (SignInButton) findViewById(R.id.googleLogin);
        mAuth = FirebaseAuth.getInstance();

        start();
        mHandler.postDelayed(mMyTask, 5000);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("955122216724-q4rm89tcl2f2hb3727pgcf31km3kv6ri.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        //Google_Login = findViewById(R.id.googleLogin);
        Google_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermission();
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });
    }

    public void start() { //이미지 올라가고, 로그인창 띄우기
        splash = (ImageView) findViewById(R.id.splash_ikon);
        login = (ImageView) findViewById(R.id.mentimg);
        Animation loginani = AnimationUtils.loadAnimation(this, R.anim.alpha);

        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f);
        animation.setFillAfter(true); // 이동 이후 자리에 고정
        animation.setDuration(2000); // 3초 간 이동
        splash.startAnimation(animation);
        login.startAnimation(loginani);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(SplashLogin.this, "구글 로그인 실패", Toast.LENGTH_SHORT).show();
                ; // 구글 로그인 실패
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SplashLogin.this, "인증 실패", Toast.LENGTH_SHORT).show();
                        } else {
                            finish();
                            Intent myIntent = new Intent(SplashLogin.this, MainActivity.class);
                            startActivity(myIntent);
                            //Toast.makeText(SplashLogin.this, "구글 로그인 인증 성공", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        ;
    }

    /*permission 보내는 코드 */
    void checkStoragePermission() { //스플레쉬 화면으로 옮기는게 좋을듯
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        SDCARD_PERMISSION);
            }
        }
    }

    private Runnable mMyTask = new Runnable() {
        @Override
        public void run() {
            if (mAuth.getCurrentUser() != null) {
                //이미 로그인 되었다면 이 액티비티를 종료함
                finish();
                //그리고 profile 액티비티를 연다.
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            } else {
                Google_Login.setVisibility(View.VISIBLE);
                login.setVisibility(View.GONE);
            }
        }
    };

}