package com.example.connect;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> mMessageList;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;


    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);

        auth = FirebaseAuth.getInstance();

        return new MessageViewHolder(v);

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int i) {


        String messageSenderId = auth.getCurrentUser().getUid();
        Messages messages = mMessageList.get(i);

        String fromUserId = messages.getFrom();

        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String receiverImage = dataSnapshot.child("image").getValue().toString();

                Picasso.get().load(receiverImage).placeholder(R.drawable.avatar).into(holder.profileImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.receiverMessage.setVisibility(View.GONE);
        holder.profileImage.setVisibility(View.GONE);
        holder.senderMessage.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);

        if(fromMessageType.equals("text"))
        {
            holder.receiverMessage.setVisibility(View.INVISIBLE);
            holder.profileImage.setVisibility(View.INVISIBLE);
            holder.senderMessage.setVisibility(View.INVISIBLE);

            if(fromUserId.equals(messageSenderId))
            {
                holder.senderMessage.setVisibility(View.VISIBLE);
                holder.senderMessage.setText(messages.getMessage());

            }
            else
            {
                holder.receiverMessage.setVisibility(View.VISIBLE);
                holder.profileImage.setVisibility(View.VISIBLE);
                holder.receiverMessage.setText(messages.getMessage());
            }
        }
        else
        {
            if(fromUserId.equals(messageSenderId))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);

            }
            else
            {
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.profileImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);


            }
        }



    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessage;
        public CircleImageView profileImage;
        public TextView receiverMessage;
        public ImageView messageSenderPicture, messageReceiverPicture;

        public MessageViewHolder(View view) {
            super(view);


            senderMessage = (TextView) view.findViewById(R.id.sender_message);
            receiverMessage= (TextView) view.findViewById(R.id.receiver_message);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);

        }
    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}