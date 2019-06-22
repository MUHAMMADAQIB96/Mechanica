package com.example.fyp.mechanica;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.paperdb.Paper;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth auth;

    User currUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        currUser = Paper.book().read(Constants.CURR_USER_KEY);

        RelativeLayout relativeLayout = findViewById(R.id.rl_splash);

        relativeLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (auth.getCurrentUser() != null) {

                    if (currUser != null && currUser.userRole.equals("Mechanic")) {
                        intent = new Intent(SplashActivity.this, MechanicMapActivity.class);

                    } else {
                        intent = new Intent(SplashActivity.this, MapActivity.class);

                    }

                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }

                startActivity(intent);
                finish();

            }
        }, 3000);
    }
}
