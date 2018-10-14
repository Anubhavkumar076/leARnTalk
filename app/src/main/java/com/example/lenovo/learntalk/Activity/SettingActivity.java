package com.example.lenovo.learntalk.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

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

public class SettingActivity extends AppCompatActivity {

    android.support.v7.widget.Toolbar mToolbar;
    EditText username,fullname,status,country,birthdate,gender,relationship;
    Button update;
    CircleImageView settingProfile;
    DatabaseReference settingUserReference;
    FirebaseAuth mAuth;
    String currentUserID,downloadUrl;
    int GALLERY_PICK=1;
    private StorageReference userProfileStorageRef;
    ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting2);

        mToolbar=findViewById(R.id.SettingToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar=new ProgressDialog(this);

        username=findViewById(R.id.SettingUsername);
        fullname=findViewById(R.id.SettingFullname);
        status=findViewById(R.id.SettingStatus);
        country=findViewById(R.id.SettingCountry);
        birthdate=findViewById(R.id.SettingBirthdate);
        gender=findViewById(R.id.SettingGender);
        relationship=findViewById(R.id.SettingRelationship);
        update=findViewById(R.id.SettingSubmit);
        settingProfile=findViewById(R.id.SettingProfileImage);


        userProfileStorageRef= FirebaseStorage.getInstance().getReference().child("profileimage");
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        settingUserReference= FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);


        settingUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String username1=dataSnapshot.child("username").getValue().toString();
                Log.d("dkho",""+username1);
                String fullname1=dataSnapshot.child("fullname").getValue().toString();
                String birthdate1=dataSnapshot.child("dob").getValue().toString();
                String country1=dataSnapshot.child("country").getValue().toString();
                String gender1=dataSnapshot.child("gender").getValue().toString();
                String relationship1=dataSnapshot.child("relationshipstatus").getValue().toString();
                String status1=dataSnapshot.child("status").getValue().toString();
                String profileImage=dataSnapshot.child("profileimage").getValue().toString();

                username.setText(username1);
                fullname.setText(fullname1);
                birthdate.setText(birthdate1);
                country.setText(country1);
                gender.setText(gender1);
                relationship.setText(relationship1);
                status.setText(status1);

                Picasso.get().load(profileImage).into(settingProfile);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        settingProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_PICK);
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName=username.getText().toString().trim();
                String fullName=fullname.getText().toString().trim();
                String dob=birthdate.getText().toString().trim();
                String status1=status.getText().toString().trim();
                String relationshipStatus=relationship.getText().toString().trim();
                String gender1=gender.getText().toString().trim();
                String country1=country.getText().toString().trim();
                validateActivity(userName,fullName,dob,status1,relationshipStatus,gender1,country1);
            }
        });




    }

    private void validateActivity(String userName, String fullName, String dob, String status1, String relationshipStatus, String gender1, String country1) {

        if(TextUtils.isEmpty(userName))
        {
            Toast.makeText(getApplicationContext(),"Username is Empty",Toast.LENGTH_SHORT).show();
        }
        else
        if(TextUtils.isEmpty(fullName))
        {
            Toast.makeText(getApplicationContext(),"Profile Name is Empty",Toast.LENGTH_SHORT).show();
        }
        else
        if(TextUtils.isEmpty(dob))
        {
            Toast.makeText(getApplicationContext(),"Date of Birth is Empty",Toast.LENGTH_SHORT).show();
        }
        else
        if(TextUtils.isEmpty(status1))
        {
            Toast.makeText(getApplicationContext(),"Status is Empty",Toast.LENGTH_SHORT).show();
        }
        else
        if(TextUtils.isEmpty(relationshipStatus))
        {
            Toast.makeText(getApplicationContext(),"Relationship Status is Empty",Toast.LENGTH_SHORT).show();
        }
        else
        if(TextUtils.isEmpty(gender1))
        {
            Toast.makeText(getApplicationContext(),"Gender is Empty",Toast.LENGTH_SHORT).show();
        }
        else
        if(TextUtils.isEmpty(country1))
        {
            Toast.makeText(getApplicationContext(),"Country is Empty",Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressBar.setTitle("Updating Information");
            progressBar.setMessage("Wait For A While");
            progressBar.show();
            progressBar.setCanceledOnTouchOutside(true);
            HashMap usermap=new HashMap();
            usermap.put("username",userName);
            usermap.put("fullname",fullName);
            usermap.put("dob",dob);
            usermap.put("status",status1);
            usermap.put("relationshipstatus",relationshipStatus);
            usermap.put("country",country1);
            usermap.put("gender",gender1);

            settingUserReference.updateChildren(usermap)
                    .addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {


                    if(task.isSuccessful())
                    {
                        progressBar.dismiss();
                        Toast.makeText(getApplicationContext(),"Information Updated",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        progressBar.dismiss();
                        String message=task.getException().toString();
                        Toast.makeText(getApplicationContext(),"Error:"+message,Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
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
                final StorageReference filePath=userProfileStorageRef.child(currentUserID+".jpg");

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





                                        settingUserReference.child("profileimage").setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()) {
                                                            Intent intent=new Intent(SettingActivity.this,SettingActivity.class);
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
