package com.example.fyp.mechanica;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;

public class MainActivity extends FragmentActivity {

    public static final int ERROR_DIALOG_REQUEST = 9001;

    @BindView(R.id.tv_username)
    TextView tvUsername;
    @BindView(R.id.btn_log_out)
    Button btnLogOut;
    Button btnMap;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            tvUsername.setText(user.getPhoneNumber());
        }

        if (isServiceOk()) {
            init();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        User currUser = Paper.book().read(Constants.CURR_USER_KEY);
        if (currUser.userRole.equals("Customer")) {
            btnMap.setText("See Nearest Mechanics");

        } else {
            btnMap.setText("My Location");
        }
    }

    public void init() {

        btnMap = findViewById(R.id.btn_map);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MapActivity.class));
            }
        });
    }

    public boolean isServiceOk() {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            return true;

        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();

        } else {
            Toast.makeText(this, "You can't make request", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @OnClick(R.id.btn_log_out)
    public void setBtnLogOut() {
        auth.signOut();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
