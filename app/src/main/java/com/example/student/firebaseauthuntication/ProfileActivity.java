package com.example.student.firebaseauthuntication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.app.ProgressDialog;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {
    private static final int IMAGE_CHOSER = 1010;
    private TextView name,email;
    private EditText updateName;
    private ImageView photo;
    private Button editBtn;
    private Bitmap bitmap;
    private Uri uri;
    private StorageReference sReference;
    private String downloadUrl;
    private ProgressBar dialog;
    private ProgressDialog dia;
    private FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initView();
        addListener();
        initVariable();
    }

    private void initVariable() {
        sReference = FirebaseStorage.getInstance().getReference("USER_PHOTO");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void addListener() {
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_CHOSER);
               // startActivityForResult(intent, IMAGE_);
            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name.setVisibility(View.GONE);
                updateName.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == IMAGE_CHOSER && resultCode == RESULT_OK && data!= null){
            uri =data.getData();
            selectImage();
        }
    }

    private void selectImage() {
        if(uri !=null){
           // Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);

                photo.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void initView() {
        name=findViewById(R.id.userName);
        updateName=findViewById(R.id.userNameEdit);
        photo=findViewById(R.id.userPhoto);

        email=findViewById(R.id.userEmailProfile);
        dialog= new ProgressBar(this);

        editBtn=findViewById(R.id.enableEdit);


    }

    public void updateProfile(View view) {

        dia.setMessage("loading...");
                dia.show();

        String imageName = System.currentTimeMillis() + ".jpg";
        sReference.child(imageName).putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        downloadUrl =  taskSnapshot.getDownloadUrl().toString();
                        updateTheUser();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                if(dia.isShowing())dia.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int progress= (int)(100 *taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                dialog.setProgress(progress);

                if(progress == 100){
                    if(dia.isShowing())
                        dia.dismiss();
                }
            }
        });

    }

    private void updateTheUser() {
        if(updateName.getText().toString().trim().isEmpty()){
            updateName.setError("ENTER NAME");
            updateName.requestFocus();
            return;
        }
        UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder().

        setPhotoUri(Uri.parse(downloadUrl)).setDisplayName(updateName.getText().toString().trim()).build();
        firebaseUser.updateProfile(changeRequest);
    }
}
