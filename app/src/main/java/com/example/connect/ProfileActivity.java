package com.example.connect;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView profileName,profileStatus,profileFriendCount;
    private Button sendRequestBtn;
    private Button decilneBtn;

    private String current_state;

    private ProgressDialog progressDialog;

    private DatabaseReference usersDatabase;
    private DatabaseReference friendReqDatabase;
    private DatabaseReference FriendDatabase;

    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        friendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        FriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        profileImage = (ImageView)findViewById(R.id.profile_image);
        profileName = (TextView)findViewById(R.id.profile_name);
        profileStatus = (TextView)findViewById(R.id.profile_status);
        profileFriendCount = (TextView)findViewById(R.id.profile_totalFriends);
        sendRequestBtn = (Button)findViewById(R.id.profile_send_request_btn);
        decilneBtn = (Button)findViewById(R.id.profile_decline_btn) ;

        current_state = "not_friends";

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please wait while we load user data");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();



        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image =                dataSnapshot.child("image").getValue().toString();

                profileName.setText(display_name);
                profileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.avatar).into(profileImage);
                decilneBtn.setVisibility(View.INVISIBLE);
                decilneBtn.setEnabled(false);

                //...............FRIENDS LIST / REQUEST FEATURE...............

                friendReqDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild(user_id))
                        {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received"))
                            {

                                current_state = "req_received";
                                sendRequestBtn.setText("ACCEPT FRIEND REQUEST");
                                decilneBtn.setVisibility(View.VISIBLE);
                                decilneBtn.setEnabled(true);
                            }
                            else if(req_type.equals("sent"))
                            {
                                current_state = "req_sent";
                                sendRequestBtn.setText("CANCEL FRIEND REQUEST");
                                decilneBtn.setVisibility(View.INVISIBLE);
                                decilneBtn.setEnabled(false);
                            }
                            progressDialog.dismiss();
                        }
                        else
                        {
                            FriendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                   if(dataSnapshot.hasChild(user_id))
                                   {
                                       current_state = "friends";
                                       sendRequestBtn.setText("UNFRIEND THIS PERSON");
                                       decilneBtn.setVisibility(View.INVISIBLE);
                                       decilneBtn.setEnabled(false);
                                   }
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {
                                    progressDialog.dismiss();
                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendRequestBtn.setEnabled(false);

                //...................NOT FRIENDS................

                 if(current_state.equals("not_friends"))
                 {
                       friendReqDatabase.child(currentUser.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task)
                           {
                               if(task.isSuccessful())
                               {
                                   friendReqDatabase.child(user_id).child(currentUser.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                       @Override
                                       public void onSuccess(Void aVoid)
                                       {
                                           Toast.makeText(ProfileActivity.this,"Friend Request Sent",Toast.LENGTH_LONG).show();
                                           sendRequestBtn.setEnabled(true);
                                           current_state = "req_sent";
                                           sendRequestBtn.setText("CANCEL FRIEND REQUEST");
                                           decilneBtn.setVisibility(View.INVISIBLE);
                                           decilneBtn.setEnabled(false);
                                       }
                                   });
                               }
                               else
                               {
                                   Toast.makeText(ProfileActivity.this,"Failed sending Request",Toast.LENGTH_LONG).show();
                               }
                           }
                       });
                 }



                //...................CANCEL REQUEST STATE................

                if(current_state.equals("req_sent"))
                {
                    friendReqDatabase.child(currentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            friendReqDatabase.child(user_id).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                    Toast.makeText(ProfileActivity.this,"Friend Request Cancelled",Toast.LENGTH_LONG).show();
                                      sendRequestBtn.setEnabled(true);
                                      current_state = "not_friends";
                                      sendRequestBtn.setText("SEND FRIEND REQUEST");
                                    decilneBtn.setVisibility(View.INVISIBLE);
                                    decilneBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }





                //...................REQ RECEIVED STATE................

                if(current_state.equals("req_received"))
                {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                      FriendDatabase.child(currentUser.getUid()).child(user_id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                          @Override
                          public void onSuccess(Void aVoid)
                          {
                            FriendDatabase.child(user_id).child(currentUser.getUid()).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                    friendReqDatabase.child(currentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid)
                                        {
                                            friendReqDatabase.child(user_id).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid)
                                                {
                                                    Toast.makeText(ProfileActivity.this,"Friend Request Accepted",Toast.LENGTH_LONG).show();
                                                    sendRequestBtn.setEnabled(true);
                                                    current_state = "friends";
                                                    sendRequestBtn.setText("UNFRIEND THIS PERSON");
                                                    decilneBtn.setVisibility(View.INVISIBLE);
                                                    decilneBtn.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                          }
                      });
                }

                //...................UNFRIEND ................

                if(current_state.equals("friends"))
                {
                    FriendDatabase.child(currentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            FriendDatabase.child(user_id).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                    Toast.makeText(ProfileActivity.this,"Removed As Your Friend",Toast.LENGTH_LONG).show();
                                    sendRequestBtn.setEnabled(true);
                                    current_state = "not_friends";
                                    sendRequestBtn.setText("SEND FRIEND REQUEST");
                                }
                            });
                        }
                    });
                }


            }
        });



        decilneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                decilneBtn.setEnabled(false);

                friendReqDatabase.child(currentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            friendReqDatabase.child(user_id).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(ProfileActivity.this,"Friend Request Declined",Toast.LENGTH_LONG).show();
                                        decilneBtn.setVisibility(View.INVISIBLE);
                                        current_state = "not_friends";
                                        sendRequestBtn.setText("SEND FRIEND REQUEST");


                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        usersDatabase.child("online").setValue(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        usersDatabase.child("online").setValue(ServerValue.TIMESTAMP);
    }

}
