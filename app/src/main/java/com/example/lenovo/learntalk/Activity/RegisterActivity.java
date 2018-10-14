package com.example.lenovo.learntalk.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.lenovo.learntalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {
private EditText email,password,confirmPassword;
private Button sign;
private FirebaseAuth mAuth;
private ProgressDialog progressBar;
private DatabaseReference rootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        confirmPassword=findViewById(R.id.confirm);
        sign=findViewById(R.id.sign);
        mAuth=FirebaseAuth.getInstance();
        progressBar=new ProgressDialog(this);

        rootRef= FirebaseDatabase.getInstance().getReference();

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null)
        {
            sendToHomepageActivity();
        }
    }

    //**********************************************CODE USED IN BUTTON*********************************************************
    private void sendToHomepageActivity() {
        Intent i=new Intent(RegisterActivity.this,Homepage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }


    //*******************************************

    private void createNewAccount() {
        String name=email.getText().toString().trim();
        String pass=password.getText().toString().trim();
        String confirmpass=confirmPassword.getText().toString().trim();

        if(TextUtils.isEmpty(name))
        {
            Toast.makeText(getApplicationContext(),"Name is empty",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(pass))
        {
            Toast.makeText(getApplicationContext(),"Password is empty",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(confirmpass))
        {
            Toast.makeText(getApplicationContext(),"confirm Password is empty",Toast.LENGTH_SHORT).show();
        }
        else if(!pass.equalsIgnoreCase(confirmpass))
        {
            Toast.makeText(getApplicationContext(),"confirm Password does not match with password",Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressBar.setTitle("Creating Account");
            progressBar.setMessage("Wait For A While");
            progressBar.show();
            progressBar.setCanceledOnTouchOutside(true);


            mAuth.createUserWithEmailAndPassword(name,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //FirebaseUser user = mAuth.getCurrentUser();
                            String currentUser=mAuth.getCurrentUser().getUid();
                            String deviceToken= FirebaseInstanceId.getInstance().getToken();

                            rootRef.child("users").child(currentUser).setValue("");
                            rootRef.child("users").child(currentUser).child("device_token")
                                    .setValue(deviceToken);

                            Toast.makeText(getApplicationContext(),"Account Created",Toast.LENGTH_SHORT).show();

                            progressBar.dismiss();
                            sendToSetupActivity();
                        } else {
                            progressBar.dismiss();
                            String message=task.getException().getMessage();
                            // If sign in fails, display a message to the user.
                           Toast.makeText(getApplicationContext(), message,
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
        }
    }

    private void sendToSetupActivity() {
        Intent i=new Intent(RegisterActivity.this,Setup.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
