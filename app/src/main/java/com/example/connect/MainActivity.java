package com.example.connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private TabLayout tabLayout;

    private DatabaseReference userRef;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mToolbar  = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ConnecT");


        if (mAuth.getCurrentUser() != null) {


            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }


        viewPager = (ViewPager)findViewById(R.id.tabPager);
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);

        tabLayout = (TabLayout)findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

          currentUser = mAuth.getCurrentUser();


    }

    public void onStart() {
        super.onStart();


        if (currentUser == null)
        {
            sendToStart();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        userRef.child("online").setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(currentUser != null) {

            userRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

    private void sendToStart()
    {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
          if(item.getItemId() == R.id.log_out_btn)
          {
              FirebaseAuth.getInstance().signOut();
              sendToStart();
          }

        if(item.getItemId() == R.id.settings_btn)
        {
            Intent settings_intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settings_intent);
        }
        if(item.getItemId() == R.id.users_btn)
        {
            Intent usersIntent = new Intent(MainActivity.this,UsersActivity.class);
            startActivity(usersIntent);
        }



         return true;
    }
}
