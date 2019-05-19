package org.androidtown.mobile_term;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SplashLogin extends AppCompatActivity {

    ImageView splash;
    RelativeLayout login;
    Button grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_login);
        try {
            PackageInfo info = getPackageManager().getPackageInfo("org.androidtown.mobile_term", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        grid = (Button)findViewById(R.id.btnLogin);
        grid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SplashLogin.this,MainActivity.class);
                startActivity(myIntent);
            }
        });

        start();
    }

    public void start() { //이미지 올라가고, 로그인창 띄우기
        splash = (ImageView)findViewById(R.id.splash_ikon);
        login = (RelativeLayout)findViewById(R.id.login);

        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.5f);
        animation.setFillAfter(true); // 이동 이후 자리에 고정
        animation.setDuration(3000); // 3초 간 이동
        splash.startAnimation(animation);

        Animation loginani = AnimationUtils.loadAnimation(this,R.anim.alpha);
        login.startAnimation(loginani);
    }
}
