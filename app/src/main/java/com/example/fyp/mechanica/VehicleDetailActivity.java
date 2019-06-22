package com.example.fyp.mechanica;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.models.User;
import com.example.fyp.mechanica.models.Vehicle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;

public class VehicleDetailActivity extends AppCompatActivity {

    @BindView(R.id.et_vehicle) EditText etVehicle;
    @BindView(R.id.et_vehicle_registration_no) EditText etRegistrationNo;
    @BindView(R.id.et_vehicle_color) EditText etVehicleColor;
    @BindView(R.id.et_vehicle_model) EditText etVehicleModel;
    @BindView(R.id.btn_submit) Button btnSubmit;

    DatabaseReference dbRef;
    User currUser;
    Vehicle vehicle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detail);

        ButterKnife.bind(this);
        currUser = Paper.book().read(Constants.CURR_USER_KEY);
        dbRef = FirebaseDatabase.getInstance().getReference();

        dbRef.child("users").child(currUser.id).child("vehicle")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                vehicle = dataSnapshot.getValue(Vehicle.class);

                if (vehicle != null) {
                    if (vehicle.type != null)
                        etVehicle.setText(vehicle.type);

                    if (vehicle.registrationNo != null)
                        etRegistrationNo.setText(vehicle.registrationNo);

                    if (vehicle.color != null)
                        etVehicleColor.setText(vehicle.color);

                    if (vehicle.model != null)
                    etVehicleModel.setText(vehicle.model);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @OnClick(R.id.btn_submit)
    public void submit() {
        boolean flag = true;

        if (
                etVehicleModel.getText().toString().isEmpty() ||
                etVehicle.getText().toString().isEmpty() ||
                etRegistrationNo.getText().toString().isEmpty() ||
                etVehicleColor.getText().toString().isEmpty()
        ) {
            etVehicleModel.setError("Required");
            flag = false;
        }

        if (!flag) {
            return;
        }

        Vehicle vehicle = new Vehicle();
        vehicle.type = etVehicle.getText().toString();
        vehicle.color = etVehicleColor.getText().toString();
        vehicle.model = etVehicleModel.getText().toString();
        vehicle.registrationNo = etRegistrationNo.getText().toString();

        dbRef.child("users").child(currUser.id).child("vehicle").setValue(vehicle)
            .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(VehicleDetailActivity.this, "Vehicle Detail has been submitted", Toast.LENGTH_SHORT).show();

                }
            });

    }

}
