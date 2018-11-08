package com.example.irfan.mechanica;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.et_username) EditText etUsername;
    @BindView(R.id.et_email) EditText etEmail;
    @BindView(R.id.et_mobile_num) EditText etMobileNumber;
    @BindView(R.id.et_password) EditText etPassword;
    @BindView(R.id.btn_signup) Button btnSignUp;
    @BindView(R.id.btn_back) Button btnBack;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        auth = FirebaseAuth.getInstance();

    }

    @OnClick(R.id.btn_signup)
    public void setBtnSignUp() {
        boolean flag = true;

        if (etUsername.getText().toString().isEmpty()) {
            etUsername.setError("Required!");
            flag = false;
        } else {
            etUsername.setError(null);
        }

        if (etEmail.getText().toString().isEmpty()) {
            etEmail.setError("Required");
            flag = false;

        } else {
            etEmail.setError(null);
        }

        if (etMobileNumber.getText().toString().isEmpty()) {
            etMobileNumber.setError("Required!");
            flag = false;

        } else {
            etMobileNumber.setError(null);
        }

        if (etPassword.getText().toString().isEmpty()) {
            etPassword.setError("Required!");
            flag = false;

        } else {
            etPassword.setError(null);
        }

      if (!flag) {
          return;
      }

      auth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
              .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                  @Override
                  public void onComplete(@NonNull Task<AuthResult> task) {
                      if (task.isSuccessful()) {
                          FirebaseUser firebaseUser = auth.getCurrentUser();

                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                      } else {
                          // if sign up fails, display a message
                          Toast.makeText(SignupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                      }
                  }
              });
    }

    @OnClick(R.id.btn_back)
    public void setBtnBack() {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
//        finish();
    }
}
