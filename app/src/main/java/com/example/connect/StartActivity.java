package com.example.connect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button regBtn;
    private Button start_loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        regBtn = (Button)findViewById(R.id.reg_btn);
        start_loginBtn = (Button)findViewById(R.id.start_login_btn);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent  = new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(reg_intent);

            }
        });

       start_loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login_intent  = new Intent(StartActivity.this,LoginActivity.class);
                startActivity(login_intent);

            }
        });


    }
}
