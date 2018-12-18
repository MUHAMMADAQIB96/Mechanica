package com.example.fyp.mechanica;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();

        RelativeLayout relativeLayout = findViewById(R.id.rl_splash);

        relativeLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (auth.getCurrentUser() != null) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);

                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();

            }
        }, 3000);
    }
}
