package com.example.irfan.mechanica;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.et_username) EditText etUsername;
    @BindView(R.id.et_email) EditText etEmail;
    @BindView(R.id.et_phone_num) EditText etPhoneNumber;
    @BindView(R.id.et_password) EditText etPassword;
    @BindView(R.id.btn_signup) Button btnSignUp;
    @BindView(R.id.btn_back) Button btnBack;

    private String inputCode, verificationId;
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

        if (etPhoneNumber.getText().toString().isEmpty()) {
            etPhoneNumber.setError("Required!");
            flag = false;

        } else {
            etPhoneNumber.setError(null);
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

        showDialog();
        sendVerificationCode();

//      auth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
//              .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                  @Override
//                  public void onComplete(@NonNull Task<AuthResult> task) {
//                      if (task.isSuccessful()) {
//                          FirebaseUser firebaseUser = auth.getCurrentUser();
//
//                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
//                        startActivity(intent);
//                        finish();
//
//                      } else {
//                          // if sign up fails, display a message
//                          Toast.makeText(SignupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                      }
//                  }
//              });

//
    }

    @OnClick(R.id.btn_back)
    public void setBtnBack() {
        this.finish();
    }


    public void showDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SignupActivity.this);
        alertDialog.setTitle("Verify Number");
        alertDialog.setMessage("Enter Code");

        final EditText input = new EditText(SignupActivity.this);
        int dp = (int) getResources().getDimension(R.dimen.spacing_normal);
//        input.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_border));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
//        lp.setMargins(dp, dp, dp, dp);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.setPositiveButton("Verify",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        inputCode = input.getText().toString();
                        if (!inputCode.isEmpty()) {

                            verifySignInCode();
                        }
                    }
                });

        alertDialog.show();
    }

    public void verifySignInCode() {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, inputCode);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = task.getResult().getUser();
                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification inputCode entered was invalid
                                Toast.makeText(getApplicationContext(), "Invalid inputCode", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }


    public void sendVerificationCode() {

        String phoneNumber = etPhoneNumber.getText().toString();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,         // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,      // Unit of timeout
                this,                  // Activity (for callback binding)
               verificationStateChangedCallbacks);           // OnVerificationStateChangedCallbacks
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }
    };
}
