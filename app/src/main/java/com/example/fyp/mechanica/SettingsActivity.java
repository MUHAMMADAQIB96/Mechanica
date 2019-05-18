package com.example.fyp.mechanica;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.et_username) EditText etUsername;
    @BindView(R.id.et_email) EditText etEmail;
//    @BindView(R.id.et_password) EditText etPassword;
    @BindView(R.id.et_phone_num) EditText etPhoneNumber;

    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        user = Paper.book().read(Constants.CURR_USER_KEY);

        etUsername.setText(user.name);
        etEmail.setText(user.email);
        etPhoneNumber.setText(user.phoneNumber);
    }
}
