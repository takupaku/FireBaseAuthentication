package com.example.student.firebaseauthuntication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class MainActivity extends AppCompatActivity {

    private EditText userEmail,userpASSWORD;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadProfile();
        initView();
        intVariable();
    }

    private void loadProfile() {
        if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(this,ProfileActivity.class));
            finish();
        }
    }

    private void intVariable() {
        mAuth= FirebaseAuth.getInstance();
    }

    private void initView() {
        userEmail = findViewById(R.id.userEmail);
        userpASSWORD= findViewById(R.id.userPassword);
    }


    public void loginBtn(View view) {
        mAuth.signInWithEmailAndPassword(userEmail.getText().toString().trim(),userpASSWORD.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void signUpPage(View view) {
        startActivity(new Intent(this,SignUpActivity.class));
        finish();

    }

    public void FORGOTpASSWORD(View view) {
        final String _email = userEmail.getText().toString().trim();

        if(_email.isEmpty()){
            userEmail.setError("enter email");
            userEmail.requestFocus();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("you are resetting your old password, proceed?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.sendPasswordResetEmail(_email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "email sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("NO",null);
        builder.show();
    }
}
