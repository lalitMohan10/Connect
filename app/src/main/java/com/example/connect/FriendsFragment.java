package com.example.connect;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendsList;

    private DatabaseReference friendsDatabase;
    private DatabaseReference usersDatabase;
    private FirebaseAuth auth;

    private String current_user_id;

    private View mainView;

    public FriendsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        friendsList = (RecyclerView) mainView.findViewById(R.id.friends_list);
        auth = FirebaseAuth.getInstance();

        current_user_id = auth.getCurrentUser().getUid();

        friendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user_id);
        friendsDatabase.keepSynced(true);

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        usersDatabase.keepSynced(true);

        friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(friendsDatabase, Friends.class)
                .build();


        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model)
            {

                final String list_user_id = getRef(position).getKey();

                usersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setOnline_status(userOnline);

                        }


                        holder.setName(userName);
                        holder.setThumb_image(userThumb, getContext());
                        holder.setDate(status);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        if (i == 0) {
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        if (i == 1) {

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);

                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);
                return new FriendsViewHolder(view);
            }
        };
        friendsList.setAdapter(friendsRecyclerViewAdapter);
        friendsRecyclerViewAdapter.startListening();

    }

    public  class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date) {
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(date);
        }

        public  void setName(String name) {

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public  void setThumb_image(String thumb_image, Context ctx) {

            CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.avatar).into(userImageView);

        }

        public void setOnline_status(String online_status) {

            ImageView userOnlineView = mView.findViewById(R.id.user_single_status_icon);

            if (online_status.equals("true")) {

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);


            }
        }
    }
}

