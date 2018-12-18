package com.example.fyp.mechanica;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.models.User;
import com.example.fyp.mechanica.models.Vehicle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;

public class UpdateProfileActivity extends AppCompatActivity {

    @BindView(R.id.et_vehicle_make) EditText etMake;
    @BindView(R.id.et_vehicle_type) EditText etType;
    @BindView(R.id.et_vehicle_model) EditText etModel;
    @BindView(R.id.btn_submit) Button btnSubmit;

    User currUser;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        ButterKnife.bind(this);

        currUser = Paper.book().read(Constants.CURR_USER_KEY);
        dbRef = FirebaseDatabase.getInstance().getReference();


        if (currUser.vehicle != null) {
            etModel.setText(currUser.vehicle.model);
            etMake.setText(currUser.vehicle.make);
            etType.setText(currUser.vehicle.type);
        }
    }

    @OnClick(R.id.btn_submit)
    public void setData() {
        boolean flag = true;

        if (etMake.getText().toString().isEmpty()) {
            etMake.setError("Required");
            flag = false;

        } else {
            etMake.setError(null);
        }

        if (etType.getText().toString().isEmpty()) {
            etType.setError("Required");
            flag = false;

        } else {
            etType.setError(null);
        }

        if (etModel.getText().toString().isEmpty()) {
            etModel.setError("Required");
            flag = false;

        } else {
            etModel.setError(null);
        }


        if (!flag) {
            return;
        }

        final Vehicle vehicle = new Vehicle();
        vehicle.make = etMake.getText().toString();
        vehicle.model = etModel.getText().toString();
        vehicle.type = etType.getText().toString();

        dbRef.child("users").child(currUser.id).child("vehicle").setValue(vehicle)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                currUser.vehicle = vehicle;
                Paper.book().write(Constants.CURR_USER_KEY, currUser);
                startActivity(new Intent(UpdateProfileActivity.this, MainActivity.class));
            }
        });
    }
}
