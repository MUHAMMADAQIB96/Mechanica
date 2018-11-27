package com.example.fyp.mechanica;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserRoleActivity extends AppCompatActivity {

    @BindView(R.id.btn_customer) Button btnCustomer;
    @BindView(R.id.btn_mechanic) Button btnMechanic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_role);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_customer)
    public void setBtnCustomer() {
        Intent intent = new Intent(UserRoleActivity.this, SignupActivity.class);
        intent.putExtra("ROLE", "Customer");
        startActivity(intent);
    }

    @OnClick(R.id.btn_mechanic)
    public void setBtnMechanic() {
        Intent intent = new Intent(UserRoleActivity.this, SignupActivity.class);
        intent.putExtra("ROLE", "Mechanic");
        startActivity(intent);
    }
}
