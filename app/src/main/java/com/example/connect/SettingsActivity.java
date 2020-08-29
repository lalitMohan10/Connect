package com.example.connect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private DatabaseReference userDatabase;

    private CircleImageView displayImage;
    private TextView name;
    private TextView status;
    private  Button image_btn;
    private Button status_btn;
    private static final int GALLERY_PICK = 1;
    private ProgressDialog progressDialog;

    private StorageReference imageStorage;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        displayImage = (CircleImageView)findViewById(R.id.settings_image);
        name = (TextView)findViewById(R.id.settings_display_name);
        status = (TextView)findViewById(R.id.settings_status);
        image_btn = (Button)findViewById(R.id.settings_image_btn);
        status_btn = (Button)findViewById(R.id.settings_status_btn);

        imageStorage = FirebaseStorage.getInstance().getReference();


        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        String uid = currentUser.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        userDatabase.keepSynced(true);

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String settings_name = dataSnapshot.child("name").getValue().toString();
                final String settings_image = dataSnapshot.child("image").getValue().toString();
                String settings_status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                name.setText(settings_name);
                status.setText(settings_status);

                    if(!settings_image.equals("default"))
                    Picasso.get().load(settings_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar).into(displayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(settings_image).placeholder(R.drawable.avatar).into(displayImage);
                        }
                    });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        status_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = status.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("status_value",status_value);
                startActivity(statusIntent);
            }
        });

        image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        userDatabase.child("online").setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(currentUser != null) {

            userDatabase.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait while we upload and process the image");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                final File thumb_filePath = new File(resultUri.getPath());

                String user_id = currentUser.getUid();
                Bitmap thumb_bitmap = null;


                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                final StorageReference filepath = imageStorage.child("profile_images").child(user_id + ".jpg");
                final StorageReference thumb_filepath = imageStorage.child("profile_images").child("thumbs").child(user_id + ".jpg");

                final Bitmap finalThumb_bitmap = thumb_bitmap;
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful()) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String download_url = uri.toString();

                                    thumb_filepath.putBytes(thumb_byte).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                thumb_filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri)
                                                    {
                                                        final String thumb_downloadUrl = uri.toString();

                                                        Map update_hashMap = new HashMap();
                                                        update_hashMap.put("image", download_url);
                                                        update_hashMap.put("thumb_image", thumb_downloadUrl);

                                                        userDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task)
                                                            {

                                                                if(task.isSuccessful()){

                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(SettingsActivity.this, "Success Uploading.", Toast.LENGTH_LONG).show();

                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });


                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this,"Error in uploading",Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


    }
}
