package com.example.connect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;


public class StatusActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText status_input;
    private Button saveBtn;
    private ProgressDialog progressDialog;

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        toolbar = (Toolbar)findViewById(R.id.status_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String string_value = getIntent().getStringExtra("status_value");

        status_input = (EditText)findViewById(R.id.status_input);
        saveBtn = (Button)findViewById(R.id.status_save_btn);

        status_input.setText(string_value);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(StatusActivity.this);
                 progressDialog.setTitle("Saving Changes");
                 progressDialog.setMessage("Please wait while we are saving changes");
                 progressDialog.show();

                 String status = status_input.getText().toString();
                 databaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                         if(task.isSuccessful())
                         {
                             progressDialog.dismiss();
                             Intent settingsIntent = new Intent(StatusActivity.this,SettingsActivity.class);
                             startActivity(settingsIntent);

                         }
                         else
                         {
                             Toast.makeText(getApplicationContext(),"There was some error ins aving changes",Toast.LENGTH_LONG).show();
                         }
                     }
                 });
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();

        databaseReference.child("online").setValue(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        databaseReference.child("online").setValue(ServerValue.TIMESTAMP);
    }
}
