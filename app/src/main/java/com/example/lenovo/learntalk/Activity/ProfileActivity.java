package com.example.lenovo.learntalk.Activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.lenovo.learntalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView username,fullname,status,country,birthdate,gender,relationship;
    private CircleImageView ProfileCircleImage;
    private DatabaseReference profileUserReference;
    private FirebaseAuth mAuth;
    private String currentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        username=findViewById(R.id.Profile_Username);
        fullname=findViewById(R.id.Profile_Fullname);
        status=findViewById(R.id.Profile_Status);
        country=findViewById(R.id.Profile_Country);
        birthdate=findViewById(R.id.Profile_DateofBirth);
        gender=findViewById(R.id.Profile_Gender);
        relationship=findViewById(R.id.Profile_Relationship);
        ProfileCircleImage=findViewById(R.id.Profile_Image);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();

        profileUserReference= FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);


        profileUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    String username1=dataSnapshot.child("username").getValue().toString();
                    Log.d("dkho",""+username1);
                    String fullname1=dataSnapshot.child("fullname").getValue().toString();
                    String birthdate1=dataSnapshot.child("dob").getValue().toString();
                    String country1=dataSnapshot.child("country").getValue().toString();
                    String gender1=dataSnapshot.child("gender").getValue().toString();
                    String relationship1=dataSnapshot.child("relationshipstatus").getValue().toString();
                    String status1=dataSnapshot.child("status").getValue().toString();
                    String profileImage=dataSnapshot.child("profileimage").getValue().toString();

                    username.setText("@"+username1);
                    fullname.setText(fullname1);
                    birthdate.setText("D.o.B.: "+birthdate1);
                    country.setText("Country: "+country1);
                    gender.setText("Gender: "+gender1);
                    relationship.setText("Relationship: "+relationship1);
                    status.setText(status1);
                    Picasso.get().load(profileImage).into(ProfileCircleImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
