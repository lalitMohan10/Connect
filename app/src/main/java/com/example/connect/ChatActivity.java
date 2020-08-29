package com.example.connect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private String userName,userId;
    private Toolbar chatToolbar;
    private TextView titleView;
    private TextView lastSeenView;
    private CircleImageView profileImage;
    private DatabaseReference rootRef,userRef;


    private FirebaseAuth auth;
    private  String currentUserId;

    private ImageButton chatAddBtn;
    private  ImageButton chatSendBtn;
    private EditText chatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout refreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayout;
    private MessageAdapter adapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private int itemPos = 0;

    private String lastKey = "";
    private String prevKey = "";

    private static final int GALLERY_PICK = 1;

    private StorageReference imageStorage;


    private DatabaseReference messageDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());

        rootRef = FirebaseDatabase.getInstance().getReference();

        userName = getIntent().getStringExtra("user_name");
        userId = getIntent().getStringExtra("user_id");

        chatToolbar = (Toolbar)findViewById(R.id.chat_appBar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        titleView = (TextView)findViewById(R.id.custom_bar_title);
        lastSeenView = (TextView)findViewById(R.id.custom_bar_seen);
        profileImage = (CircleImageView)findViewById(R.id.custom_bar_image);

        chatAddBtn =(ImageButton)findViewById(R.id.chat_add_btn);
        chatSendBtn = (ImageButton)findViewById(R.id.chat_send_btn);
        chatMessageView =(EditText)findViewById(R.id.chat_message_view);

        adapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView)findViewById(R.id.messages_list);
        refreshLayout = findViewById(R.id.message_swipe_layout);
        linearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(linearLayout);

        mMessagesList.setAdapter(adapter);
        loadMessages();
        titleView.setText(userName);

        imageStorage = FirebaseStorage.getInstance().getReference();
        rootRef.child("Chat").child(currentUserId).child(userId).child("seen").setValue(true);

        rootRef.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                if(online.equals("true")) {

                    lastSeenView.setText("Online");

                } else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                   lastSeenView.setText(lastSeenTime);

                }

                Picasso.get().load(thumb_image).placeholder(R.drawable.avatar).into(profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rootRef.child("Chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(userId)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                    chatAddMap.put("from",currentUserId);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + currentUserId + "/" + userId, chatAddMap);
                    chatUserMap.put("Chat/" + userId + "/" + currentUserId , chatAddMap);

                    chatMessageView.setText("");

                    rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null)
                            {


                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        chatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendMessage();
            }
        });


        chatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });



        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {
              mCurrentPage++;

              itemPos = 0;
              loadMoreMessages();
            }

        });






    }

    @Override
    protected void onResume() {
        super.onResume();

        userRef.child("online").setValue(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        userRef.child("online").setValue(ServerValue.TIMESTAMP);
    }

    private void loadMoreMessages()
    {
        DatabaseReference messageRef = rootRef.child("messages").child(currentUserId).child(userId);

        Query messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!prevKey.equals(messageKey)){

                    messagesList.add(itemPos++, message);

                } else {

                    prevKey = lastKey;

                }


                if(itemPos == 1) {

                    lastKey = messageKey;

                }

                adapter.notifyDataSetChanged();

                refreshLayout.setRefreshing(false);

                linearLayout.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




    private void loadMessages()
    {

        DatabaseReference messageRef = rootRef.child("messages").child(currentUserId).child(userId);

        Query messageQuery = messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);


                itemPos++;

                if(itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    lastKey = messageKey;
                    prevKey = messageKey;

                }




                messagesList.add(message);
                adapter.notifyDataSetChanged();

              mMessagesList.scrollToPosition(messagesList.size()-1);

              refreshLayout.setRefreshing(false);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendMessage()
    {
        String message = chatMessageView.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/" + currentUserId + "/" + userId;
            String chat_user_ref = "messages/" + userId + "/" + currentUserId;

            DatabaseReference user_message_push = rootRef.child("messages")
                    .child(currentUserId).child(userId).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", currentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            chatMessageView.setText("");

            rootRef.child("Chat").child(currentUserId).child(userId).child("seen").setValue(true);
            rootRef.child("Chat").child(currentUserId).child(userId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            rootRef.child("Chat").child(userId).child(currentUserId).child("seen").setValue(false);
            rootRef.child("Chat").child(userId).child(currentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null){

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + currentUserId + "/" + userId;
            final String chat_user_ref = "messages/" + userId + "/" + currentUserId;

            DatabaseReference user_message_push =rootRef.child("messages")
                    .child(currentUserId).child(userId).push();

            final String push_id = user_message_push.getKey();


             final StorageReference filepath = imageStorage.child("message_images").child( push_id + ".jpg");

             filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                 @Override
                 public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                 {
                     filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                         @Override
                         public void onSuccess(Uri uri)
                         {
                             String download_url = uri.toString();


                             Map messageMap = new HashMap();
                             messageMap.put("message", download_url);
                             messageMap.put("seen", false);
                             messageMap.put("type", "image");
                             messageMap.put("time", ServerValue.TIMESTAMP);
                             messageMap.put("from", currentUserId);


                             Map messageUserMap = new HashMap();
                             messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                             messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                             chatMessageView.setText("");

                             rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                 @Override
                                 public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference)
                                 {
                                     if(databaseError != null){

                                         Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                     }
                                 }
                             });



                         }
                     });

                 }
             });



        }


    }
}
