package com.example.fyp.mechanica;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.et_phone_num) EditText etMobileNumber;
    @BindView(R.id.et_password) EditText etPassword;
    @BindView(R.id.btn_signin) Button btnSignUp;
    @BindView(R.id.btn_goto_signup) Button btnOnSignUp;

    private FirebaseAuth auth;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            startMainActivity();
        }
    }

    @OnClick(R.id.btn_signin)
    public void setBtnSignUp() {

        boolean flag = true;

        if (etMobileNumber.getText().toString().isEmpty()) {
            etMobileNumber.setError("Phone Number Required!");
            flag = false;

        } else {
            etMobileNumber.setError(null);
        }

        if (etPassword.getText().toString().isEmpty()) {
            etPassword.setError("Password Required!");
            flag = false;

        } else {
            etPassword.setError(null);
        }

        if (!flag) {
            return;
        }

        auth.signInWithEmailAndPassword(etMobileNumber.getText().toString(), etPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    getUserData(firebaseUser.getUid());
                    startMainActivity();

                } else {
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void getUserData(String userId) {
        dbRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null)
                    Paper.book().write(Constants.CURR_USER_KEY, user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    @OnClick(R.id.btn_goto_signup)
    public void setOpenSignUp() {
        Intent intent = new Intent(LoginActivity.this, UserRoleActivity.class);
        startActivity(intent);
    }
}
