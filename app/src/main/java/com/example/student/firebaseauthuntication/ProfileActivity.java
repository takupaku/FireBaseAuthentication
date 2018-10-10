package com.example.student.firebaseauthuntication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.app.ProgressDialog;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initView();
        addListener();
        initVariable();
        loadProfileInfo();
    }

    private void loadProfileInfo() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            email.setText(firebaseUser.getEmail().toString());

            if(firebaseUser.getDisplayName()!=null)
            name.setText(firebaseUser.getDisplayName().toString());

            //loading photo, with glide library by bumptech
            if(firebaseUser.getPhotoUrl()!=null){
                String url = firebaseUser.getPhotoUrl().toString();
                Glide.with(this).load(url).into(photo);
            }


        }

    }


    private void initVariable() {
        sReference = FirebaseStorage.getInstance().getReference("USER_PHOTO");
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String Uemail = firebaseUser.getEmail().toString();
        email.setText(Uemail);
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
        dia= new ProgressDialog(this);


        editBtn=findViewById(R.id.enableEdit);


    }

    public void updateProfile(View view) {

        dia.setMessage("loading...");
        dia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dia.setCancelable(false);
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
        firebaseUser.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.logOut){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this,MainActivity.class));
            finish();
            return true;
        }
        if(item.getItemId()==R.id.resetPass){
            resetPassword();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.reset_password,null);

        final EditText oldpass = view.findViewById(R.id.oldPass);
        final EditText newpass = view.findViewById(R.id.newPass);

        final EditText confirmpass = view.findViewById(R.id.confirmPass);

        Button button = view.findViewById(R.id.resetPassId);
        builder.setView(view);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String _email =  firebaseUser.getEmail().toString();
                String _oldPass = oldpass.getText().toString().trim();
                final String _newPass = newpass.getText().toString().trim();
                String confirm = confirmpass.getText().toString().trim();

                //check pass
                if(!_newPass.equals(confirm)){
                    confirmpass.setError("pass not matched");
                    confirmpass.requestFocus();
                    return;
                }
                        firebaseAuth.signInWithEmailAndPassword(_email,_oldPass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            firebaseUser.updatePassword(_newPass)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(ProfileActivity.this, "pass update", Toast.LENGTH_SHORT).show();
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
