package com.example.connect;


import android.graphics.Color;
import android.graphics.fonts.Font;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {

    private View mainView;
    private RecyclerView requestList;

    private DatabaseReference requestsRef,usersRef,friendsRef;
    private FirebaseAuth auth;

    private String current_user_id;


    public RequestsFragment()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_requests, container, false);

        requestList = (RecyclerView)mainView.findViewById(R.id.requestList);
        requestList.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();

        current_user_id = auth.getCurrentUser().getUid();
        requestsRef = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");




        return mainView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<Requests> options = new FirebaseRecyclerOptions.Builder<Requests>()
                .setQuery(requestsRef.child(current_user_id),Requests.class)
                .build();

        FirebaseRecyclerAdapter<Requests,RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int i, @NonNull Requests requests)
            {

                 final String list_user_id = getRef(i).getKey();

                 DatabaseReference getTypeRef = getRef(i).child("request_type").getRef();

                 getTypeRef.addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                     {
                         if(dataSnapshot.exists())
                         {
                             String type = dataSnapshot.getValue().toString();

                             if(type.equals("received"))
                             {

                                  usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                      @Override
                                      public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                      {
                                          final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                          final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                          final String requestUserImage = dataSnapshot.child("image").getValue().toString();

                                          holder.userName.setText(requestUserName);
                                          holder.userStatus.setText(requestUserStatus);

                                          Picasso.get().load(requestUserImage).placeholder(R.drawable.avatar).into(holder.profileImage);

                                      }

                                      @Override
                                      public void onCancelled(@NonNull DatabaseError databaseError) {

                                      }
                                  });
                             }
                             else
                             {
                                 Button request_sent_btn = holder.itemView.findViewById(R.id.request_decline_btn);
                                 request_sent_btn.setBackgroundColor(Color.RED);
                                 request_sent_btn.setText("Cancel Request");

                                 holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.INVISIBLE);

                                 usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                     @Override
                                     public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                     {
                                         final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                         Picasso.get().load(requestProfileImage).placeholder(R.drawable.avatar).into(holder.profileImage);
                                         final String requestUserName = dataSnapshot.child("name").getValue().toString();

                                         final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                         holder.userName.setText(requestUserName);
                                         holder.userStatus.setText("You Have Sent A Request To " + requestUserName);


                                     }

                                     @Override
                                     public void onCancelled(@NonNull DatabaseError databaseError) {

                                     }
                                 });

                             }

                             holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v)
                                 {
                                     final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                                   friendsRef.child(current_user_id).child(list_user_id).child("date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                friendsRef.child(list_user_id).child(current_user_id).child("date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful())
                                                        {
                                                           requestsRef.child(current_user_id).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task)
                                                               {
                                                                   if(task.isSuccessful())
                                                                   {
                                                                       requestsRef.child(list_user_id).child(current_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                           @Override
                                                                           public void onComplete(@NonNull Task<Void> task)
                                                                           {
                                                                               if(task.isSuccessful())
                                                                               {
                                                                                   Toast.makeText(getContext(),"Friend Request Accepted",Toast.LENGTH_LONG).show();
                                                                               }
                                                                           }
                                                                       });
                                                                   }
                                                               }
                                                           });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                 }
                             });

                             holder.declineButton.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v)
                                 {
                                     requestsRef.child(current_user_id).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task)
                                     {
                                         if(task.isSuccessful())
                                         {
                                             requestsRef.child(list_user_id).child(current_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                 @Override
                                                 public void onComplete(@NonNull Task<Void> task)
                                                 {
                                                     if(task.isSuccessful())
                                                     {
                                                         Toast.makeText(getContext(),"Friend Request Cancelled",Toast.LENGTH_LONG).show();
                                                     }
                                                 }
                                             });
                                         }
                                     }
                                 });
                                 }
                             });



                         }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });

            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
               View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_request_layout,parent,false);
               RequestsViewHolder holder = new RequestsViewHolder(view);
               return holder;
            }
        };

        requestList.setAdapter(adapter);
        adapter.startListening();


    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button acceptButton , declineButton;


        public RequestsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus =itemView.findViewById(R.id.user_profile_status);
            profileImage =itemView.findViewById(R.id.user_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_btn);
            declineButton = itemView.findViewById(R.id.request_decline_btn);

        }
    }

}
