package org.androidtown.mobile_term;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class SplashMain extends Activity {

    private FirebaseAuth mAuth;
    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_main);

        mAuth = FirebaseAuth.getInstance();

        try {
            Thread.sleep(2000); //Stay for 2 seconds
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        if(mAuth.getCurrentUser() != null){  //If Login, go to mainActivity.
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        //If not, log in to activity.
        startActivity(new Intent(this, SplashLogin.class));
        finish();
    }

}
