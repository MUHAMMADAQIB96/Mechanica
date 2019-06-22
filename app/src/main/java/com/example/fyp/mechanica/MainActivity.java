package com.example.fyp.mechanica;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;

public class MainActivity extends BaseDrawerActivity {

    public static final int ERROR_DIALOG_REQUEST = 9001;

    @BindView(R.id.tv_switch_label) TextView tvSwitchLabel;
    @BindView(R.id.sw) Switch aSwitch;

    Button btnMap;
    FirebaseAuth auth;
    DatabaseReference dbRef;
    User currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        auth = FirebaseAuth.getInstance();

        dbRef = FirebaseDatabase.getInstance().getReference();

        currUser = Paper.book().read(Constants.CURR_USER_KEY);

        FirebaseUser user = auth.getCurrentUser();
        if (isServiceOk()) {
            init();
        }

        dbRef.child("lives").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();

                    if (currUser.id.equals(id)) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                aSwitch.setChecked(true);

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currUser != null) {
            if (currUser.userRole.equals("Customer")) {
                btnMap.setText("See Nearest Mechanics");
                aSwitch.setVisibility(View.GONE);
                tvSwitchLabel.setVisibility(View.GONE);
                if (currUser.vehicle == null) {
                    startActivity(new Intent(this, UpdateProfileActivity.class));
                }

            } else {
                btnMap.setText("My Location");
                aSwitch.setVisibility(View.VISIBLE);
                tvSwitchLabel.setVisibility(View.VISIBLE);
            }


        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
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

    @OnClick(R.id.sw)
    public void setSwitch() {
        if (!aSwitch.isChecked()) {
            dbRef.child("lives").child(currUser.id).removeValue();
        }
    }
}
