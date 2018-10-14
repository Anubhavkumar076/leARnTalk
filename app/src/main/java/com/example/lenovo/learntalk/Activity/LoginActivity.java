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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.learntalk.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
private EditText logusername,logpassword;
private Button login;
ImageView googleSignInButton;
private TextView newAccountLink;
ProgressDialog progressBar;
private FirebaseAuth mAuth;
private DatabaseReference userRef;
private  int RC_SIGN_IN=1;
private GoogleApiClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        logusername=findViewById(R.id.login_email);
        logpassword=findViewById(R.id.login_password);
        login=findViewById(R.id.login_sign);
        newAccountLink=findViewById(R.id.login_create);
        googleSignInButton=findViewById(R.id.google);
        progressBar=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();

        userRef= FirebaseDatabase.getInstance().getReference().child("users");

        newAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToRegisterActivity();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginAuthentication();
            }
        });

//***************************************SIGN IN WITH GOOGLE***********************************************************************************
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(getApplicationContext(),"connection to Google Sign up",Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if(result.isSuccess())
            {
                progressBar.setTitle("Google Sign In");
                progressBar.setMessage("Wait For A While");
                progressBar.show();
                progressBar.setCanceledOnTouchOutside(true);

                GoogleSignInAccount account=result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(getApplicationContext(),"please wait... Authentication is in process",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
                progressBar.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("aaya hai","aaya1");
                            Log.d("LoginActivity", "signInWithCredential:success");
                            progressBar.dismiss();
                            sendToHomepageActivity();

                        } else {
                            String message=task.getException().toString();
                            progressBar.dismiss();
                            Toast.makeText(getApplicationContext(),"Error"+message,Toast.LENGTH_SHORT).show();
                            Log.w("LoginActivity", "signInWithCredential:failure", task.getException());

                        }


                    }
                });
    }



//********************************************GET INFORMATION EITHER THERE IS EXSISTING USER OR NOT**********************************
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null)
        {
            Log.d("aaya hai","aaya");
            sendToHomepageActivity();
        }
//        else
//        {
//            Intent i=new Intent(LoginActivity.this,LoginActivity.class);
//            startActivity(i);
//        }
    }

    //*******************************CONTAINS AUTHENTICATION PROCESS*********************************************
    private void loginAuthentication() {
        String name=logusername.getText().toString().trim();
        String pass=logpassword.getText().toString().trim();

        if(TextUtils.isEmpty(name))
        {
            Toast.makeText(getApplicationContext(),"Name is empty",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(pass))
        {
            Toast.makeText(getApplicationContext(),"Password is empty",Toast.LENGTH_SHORT).show();
        }

        else
        {
            progressBar.setTitle("Creating Account");
            progressBar.setMessage("Wait For A While");
            progressBar.show();
            progressBar.setCanceledOnTouchOutside(true);
            mAuth.signInWithEmailAndPassword(name, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {


                                String currentUserId=mAuth.getCurrentUser().getUid();
                                String deviceToken= FirebaseInstanceId.getInstance().getToken();

                                userRef.child(currentUserId).child("device_token").setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {


                                                if(task.isSuccessful())
                                                {
                                                    progressBar.dismiss();
                                                    Log.d("unique", "loginAuthentication");
                                                    Log.d("aaya hai","aaya");
                                                    sendToHomepageActivity();
                                                    // Sign in success, update UI with the signed-in user's information
                                                    Log.d("unique", "signInWithEmail:success");
                                                }
                                            }
                                        });




                            } else {
                               String message=task.getException().toString();
                                Log.d("unique", ""+message);
                                progressBar.dismiss();
                                Toast.makeText(getApplicationContext(),"unique",Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });




        }
    }



    //**********************************************CODE USED IN BUTTON*********************************************************
    private void sendToHomepageActivity() {
        Intent i=new Intent(LoginActivity.this,Homepage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

//********************************************FUNCTION USED IN TEXTVIEW*************************************************
    private void sendToRegisterActivity() {
        Intent i=new Intent(LoginActivity.this,RegisterActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
