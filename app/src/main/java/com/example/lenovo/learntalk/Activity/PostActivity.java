package com.example.lenovo.learntalk.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.lenovo.learntalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static com.example.lenovo.learntalk.Activity.Setup.GALLERY_PICK;

public class PostActivity extends AppCompatActivity {
private Toolbar toolbar;
private ImageView post;
private Button add;
private EditText status;
private int GALLERY_PICK=2;
Uri imageUri;
private long countPosts=0;
    private FirebaseAuth mAuth;
    String Description,SaveCurrentdate,SaveCurrentTime,PostRandom,Downloadurl,currentUserID;
    private StorageReference postStorageReference;
    private DatabaseReference userRef,postRef;
    private ProgressDialog progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        post=findViewById(R.id.Add_post);
        add=findViewById(R.id.update);
        status=findViewById(R.id.status);
        progressBar=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getUid().toString();
        postStorageReference= FirebaseStorage.getInstance().getReference();
        postRef= FirebaseDatabase.getInstance().getReference().child("posts");
        userRef= FirebaseDatabase.getInstance().getReference().child("users");

        toolbar=findViewById(R.id.addPostTool);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Update Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setTitle("Updating Photos");
                progressBar.setMessage("Wait For A While");
                progressBar.show();
                progressBar.setCanceledOnTouchOutside(true);
                 Description=status.getText().toString().trim();
                if(imageUri==null)
                {
                    Toast.makeText(PostActivity.this, "Enter the Image first", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    storeToFireBaseStorage();

                }
            }
        });

    }

    private void storeToFireBaseStorage() {

        //************************CALENDAR AS A STRING FOR UNIQUE NAME OF PICTURE***********************************************************
        Calendar calendarForDate=Calendar.getInstance();
        SimpleDateFormat calendarDate=new SimpleDateFormat("dd-MMMM-yyyy");
        SaveCurrentdate=calendarDate.format(calendarForDate.getTime());

        Calendar calendarForTime=Calendar.getInstance();
        SimpleDateFormat calendarTime=new SimpleDateFormat("HH:mm");
        SaveCurrentTime=calendarTime.format(calendarForTime.getTime());

        PostRandom=SaveCurrentdate+SaveCurrentTime;
//*********************************************SAVE POST PICTURE IN STORAGE***************************************
        final StorageReference filePath=postStorageReference.child("postimage").child(imageUri.getLastPathSegment()+PostRandom+".jpg");

        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Downloadurl=uri.toString();
                        Log.d("download",""+Downloadurl);
                        Toast.makeText(getApplicationContext(),"Profile pic sucessfully stored in storage",Toast.LENGTH_SHORT).show();
                        savingPostInformationToDatabase();

                    }
                });
            }
        });

    }

    private void savingPostInformationToDatabase() {

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    countPosts=dataSnapshot.getChildrenCount();
                }
                else
                {
                    countPosts=0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {

                    final String userFullName=dataSnapshot.child("fullname").getValue().toString();
                    final String userProfilePicture=dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postMap=new HashMap();
                    postMap.put("userid",currentUserID);
                    postMap.put("fullname",userFullName);
                    postMap.put("profileimage",userProfilePicture);
                    postMap.put("description",Description);
                    postMap.put("images",Downloadurl);
                      postMap.put("date",SaveCurrentdate);
                      postMap.put("time",SaveCurrentTime);
                      postMap.put("counter",countPosts);



//**********************************SAVING INFORMATIOn*********************************************************
                    postRef.child(currentUserID+PostRandom).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        Log.d("khatam","done3");
                                        progressBar.dismiss();
                                        Toast.makeText(getApplicationContext(),"post Information Saved",Toast.LENGTH_SHORT).show();
                                        sendToHomePage();
                                    }
                                    else
                                    {
                                        Toast.makeText(getApplicationContext(),"Error in post Information Saving",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void openGallery() {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent.createChooser(galleryIntent, "Select Picture"),GALLERY_PICK);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK&&resultCode==RESULT_OK)
        {
            imageUri=data.getData();
            post.setImageURI(imageUri);
        }
    }

    //****************************************USED FOR BACK BUTTON ON TOOLBAR***********************************
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();

        if (id==R.id.home)
        {
            sendToHomePage();
        }

        return super.onOptionsItemSelected(item);

    }
//**********************************SEND TO HOMEPAGE****************************************************************
    private void sendToHomePage() {
        Intent i=new Intent(PostActivity.this,Homepage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
