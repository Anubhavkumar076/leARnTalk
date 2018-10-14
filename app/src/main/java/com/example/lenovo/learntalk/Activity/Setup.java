package com.example.lenovo.learntalk.Activity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Setup extends AppCompatActivity {
private EditText Username,Fullname,Country;

private Button save;
private CircleImageView profile;
private DatabaseReference userref;
private FirebaseAuth mAuth;
protected ProgressDialog progressBar;
private String currentUserId;
final static int GALLERY_PICK=1;
private String downloadUrl="";
private StorageReference userProfileStorageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Username=findViewById(R.id.username);
        Fullname=findViewById(R.id.full_name);
        Country=findViewById(R.id.country);
        save=findViewById(R.id.save);
        profile=findViewById(R.id.photo);
        progressBar=new ProgressDialog(this);


        userProfileStorageRef= FirebaseStorage.getInstance().getReference().child("profileimage");

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();

        userref= FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);// form one subfolder in database.



        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateAccountInformation();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent.createChooser(galleryIntent, "Select Picture"),GALLERY_PICK);
            }
        });

        userref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("profileimage")) {
                        final String image = dataSnapshot.child("profileimage").toString();
                        Log.d("unique", "" + image);
                        Picasso.get().load(image).placeholder(R.mipmap.eclipse).into(profile);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Please select profile Image first",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
//**************************************************To save the data in database*****************************************
    private void UpdateAccountInformation() {

        String username=Username.getText().toString().trim();
        String fullname=Fullname.getText().toString().trim();
        String country=Country.getText().toString().trim();


        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(getApplicationContext(),"Username is empty",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(fullname))
        {
            Toast.makeText(getApplicationContext(),"fullname is empty",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(getApplicationContext(),"country is empty",Toast.LENGTH_SHORT).show();
        }
        else
        {

            progressBar.setTitle("Creating Account");
            progressBar.setMessage("Wait For A While");
            progressBar.show();
            progressBar.setCanceledOnTouchOutside(true);
            HashMap usermap=new HashMap();
            usermap.put("username",username);
            usermap.put("fullname",fullname);
            usermap.put("country",country);
            usermap.put("status","Hey! I am using leARn Talk");
            usermap.put("gender","Gender");
            usermap.put("dob","Date of Birth");
            usermap.put("relationshipstatus","Relationship Status");

            //*************************************************************UPDATE ALL THE DATA IN RUNTIME DATABASE*******************************************
            userref.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful())
                    {
                        progressBar.dismiss();
                        Toast.makeText(getApplicationContext(),"Account Created",Toast.LENGTH_SHORT).show();
                        sendToHomepage();
                    }
                    else
                    {
                        String message=task.getException().toString();
                        Log.d("unique", ""+message);
                        progressBar.dismiss();
                        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendToHomepage() {

        Intent i=new Intent(Setup.this,Homepage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }



    //**********************************************TO PICK A PHOTO FROM GALLERY AND CROP IT******************************************


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK && data!=null) {


            Uri uriImage = data.getData();
            CropImage.activity()                  //// start picker to get image for cropping and then use the image in cropping activity
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {                                 //GET CROP RESULT

                Log.d("unique","done");
                CropImage.ActivityResult result = CropImage.getActivityResult(data);                        //DATA=CROPPED PHOTO
                Log.d("unique","done2");
                if (resultCode == RESULT_OK) {     //CHECK WHETHER PIC CROPPED OR NOT

                    progressBar.setTitle("Updating Profile photo");
                    progressBar.setMessage("Wait For A While");
                    progressBar.show();
                    progressBar.setCanceledOnTouchOutside(true);
                    Uri resultUri = result.getUri();
   //********************************CODE TO SAVE PHOTO IN STORAGE**********************************************************************************
                    final StorageReference filePath=userProfileStorageRef.child(currentUserId+".jpg");

                    filePath.putFile(resultUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl=uri.toString();
                                    Log.d("download",""+downloadUrl);
                                    Toast.makeText(getApplicationContext(),"Profile pic sucessfully stored in storage",Toast.LENGTH_SHORT).show();





                                    userref.child("profileimage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        Intent intent=new Intent(Setup.this,Setup.class);
                                                        startActivity(intent);

                                                        Toast.makeText(getApplicationContext(), "Profile pic sucessfully stored in database", Toast.LENGTH_SHORT).show();
                                                        progressBar.dismiss();



                                                    }
                                                    else
                                                    {

                                                        String message=task.getException().toString();
                                                        Toast.makeText(getApplicationContext(), "Error occured"+message, Toast.LENGTH_SHORT).show();
                                                        progressBar.dismiss();

                                                    }
                                                }
                                            });
                                }
                            });

                            if(task.isSuccessful())
                            {


                            }
                        }
                    });
                }
                else
                {

                    Toast.makeText(getApplicationContext(), "Error: Image not cropped", Toast.LENGTH_SHORT).show();

                }




        }

    }

}
