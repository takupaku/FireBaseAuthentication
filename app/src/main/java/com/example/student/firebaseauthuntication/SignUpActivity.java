package com.example.student.firebaseauthuntication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUpActivity extends AppCompatActivity {

    EditText user,pass,confirmPass;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initView();
        initVariable();

    }

    private void initVariable() {
        mAuth = FirebaseAuth.getInstance();

    }

    private void initView() {
        user =  findViewById(R.id.userEmailSign);
        pass= findViewById(R.id.userPasswordSign);
        confirmPass= findViewById(R.id.informConformPass);
    }

    public void SignUpBtn(View view) {
        //to firebase
        if(!validata())
            return;
        signUpToFirebase();
    }

    private void signUpToFirebase() {
        mAuth.createUserWithEmailAndPassword(user.getText().toString().trim(),pass.getText().toString())

        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SignUpActivity.this, "sign Up successful", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof FirebaseAuthUserCollisionException){
                    Toast.makeText(SignUpActivity.this, "Already registered!!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(SignUpActivity.this, "Failed to signUp", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private boolean validata() {

        if (user.getText().toString().trim().isEmpty()) {
            user.setError("please enter email");
            user.requestFocus();
            return false;
        }

        if (Patterns.EMAIL_ADDRESS.matcher(user.getText().toString().trim()).matches()) {
            user.setError("enter valid email");
            user.requestFocus();
            return false;

        }
        if (pass.getText().toString().trim().isEmpty()) {


            pass.setError("enter pass");
            pass.requestFocus();
            return false;
        }


        if (pass.getText().toString().trim().length() < 6) {


            pass.setError("pass must be 6 digit");
            pass.requestFocus();
            return false;
        }
        if (!pass.getText().toString().trim().equals(confirmPass.getText().toString().trim())) {
            confirmPass.setError("pass not matched");
            confirmPass.requestFocus();
            return false;
        }
        return true;
    }






    public void logInPage(View view) {
        startActivity(new Intent(this,MainActivity.class));
        finish();

    }
}
