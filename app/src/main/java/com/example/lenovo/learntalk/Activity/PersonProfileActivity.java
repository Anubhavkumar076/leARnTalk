package com.example.lenovo.learntalk.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.lenovo.learntalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView username,fullname,status,country,birthdate,gender,relationship;
    private Button sendRequest,declineRequest;
    private CircleImageView ProfileCircleImage;
    private DatabaseReference FriendRequestRef,UserReference,FriendsRef,NotificationRef;
    private FirebaseAuth mAuth;
    private String senderUserId,recieverUserId,CURRENT_STATE,SaveCurrentdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        username=findViewById(R.id.Person_Profile_Username);
        fullname=findViewById(R.id.Person_Profile_Fullname);
        status=findViewById(R.id.Person_Profile_Status);
        country=findViewById(R.id.Person_Profile_Country);
        birthdate=findViewById(R.id.Person_Profile_DateofBirth);
        gender=findViewById(R.id.Person_Profile_Gender);
        relationship=findViewById(R.id.Person_Profile_Relationship);
        sendRequest=findViewById(R.id.Person_Send_Button);
        declineRequest=findViewById(R.id.Person_Profile_Decline);
        ProfileCircleImage=findViewById(R.id.Person_Profile_Image);

        CURRENT_STATE="not_friends";

        mAuth=FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();
        recieverUserId= getIntent().getExtras().get("visit_user_id").toString();

        UserReference= FirebaseDatabase.getInstance().getReference().child("users");
        FriendRequestRef=FirebaseDatabase.getInstance().getReference().child("friendRequests");
        FriendsRef=FirebaseDatabase.getInstance().getReference().child("friends");
        NotificationRef=FirebaseDatabase.getInstance().getReference().child("notifications");

       fetchingInformationSearchPeople();

    }

    private void fetchingInformationSearchPeople() {
        UserReference.child(recieverUserId).addValueEventListener(new ValueEventListener() {
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

                    buttonValidation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        declineRequest.setVisibility(View.INVISIBLE);
        declineRequest.setEnabled(false);


        if(!senderUserId.equals(recieverUserId))
        {
            sendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendRequest.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends"))
                    {
                     sendFriendRequestOption();   
                    }
                    if(CURRENT_STATE.equals("request_sent"))
                    {
                        cancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_recieved"))
                    {
                        acceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends"))
                    {
                        unfriendExistingFriend();
                    }
                }
            });
        }
        else
        {
            declineRequest.setVisibility(View.INVISIBLE);
            sendRequest.setVisibility(View.INVISIBLE);
        }

    }

    private void unfriendExistingFriend() {

        FriendsRef.child(senderUserId).child(recieverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            FriendsRef.child(recieverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                sendRequest.setEnabled(true);
                                                CURRENT_STATE="not_friends";

                                                sendRequest.setText("SEND REQUEST");

                                                declineRequest.setVisibility(View.INVISIBLE);
                                                declineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void acceptFriendRequest() {

        Calendar calDate=Calendar.getInstance();
        Calendar calendarForDate=Calendar.getInstance();
        SimpleDateFormat calendarDate=new SimpleDateFormat("dd-MMMM-yyyy");
        SaveCurrentdate=calendarDate.format(calendarForDate.getTime());


        FriendsRef.child(senderUserId).child(recieverUserId).child("date").setValue(SaveCurrentdate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {

                            FriendsRef.child(recieverUserId).child(senderUserId).child("date").setValue(SaveCurrentdate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                FriendRequestRef.child(senderUserId).child(recieverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful())
                                                                {
                                                                    FriendRequestRef.child(recieverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        sendRequest.setEnabled(true);
                                                                                        CURRENT_STATE="friends";

                                                                                        sendRequest.setText("UNFRIEND");

                                                                                        declineRequest.setVisibility(View.INVISIBLE);
                                                                                        declineRequest.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void cancelFriendRequest() {

        FriendRequestRef.child(senderUserId).child(recieverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            FriendRequestRef.child(recieverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                sendRequest.setEnabled(true);
                                                CURRENT_STATE="not_friends";

                                                sendRequest.setText("SEND REQUEST");

                                                declineRequest.setVisibility(View.INVISIBLE);
                                                declineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void buttonValidation() {
        FriendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(recieverUserId))
                {
                    String request_type=dataSnapshot.child(recieverUserId).child("request_type").getValue().toString();

                    if(request_type.equals("sent"))
                    {
                        CURRENT_STATE="request_sent";
                        sendRequest.setText("Cancel Request");

                        declineRequest.setVisibility(View.INVISIBLE);
                        declineRequest.setEnabled(false);
                    }
                    else if (request_type.equals("recieved"))
                    {
                        CURRENT_STATE="request_recieved";
                        sendRequest.setText("ACCEPT REQUEST");

                        declineRequest.setVisibility(View.VISIBLE);
                        declineRequest.setEnabled(true);

                        declineRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelFriendRequest();
                            }
                        });
                    }


                }
                else
                {
                    FriendsRef.child(senderUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(recieverUserId))
                                    {
                                        CURRENT_STATE="friends";
                                        sendRequest.setText("UNFRIEND");

                                        declineRequest.setVisibility(View.INVISIBLE);
                                        declineRequest.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendFriendRequestOption() {

        FriendRequestRef.child(senderUserId).child(recieverUserId).
                child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            FriendRequestRef.child(recieverUserId).child(senderUserId)
                                    .child("request_type").setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {

                                                HashMap<String,String> friendRequestNotification=new HashMap<>();
                                                friendRequestNotification.put("from",senderUserId);
                                                friendRequestNotification.put("type","friend_request");

                                                NotificationRef.child(recieverUserId).push()
                                                        .setValue(friendRequestNotification)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful())
                                                                {
                                                                    sendRequest.setEnabled(true);
                                                                    CURRENT_STATE="request_sent";

                                                                    sendRequest.setText("CANCEL REQUEST");

                                                                    declineRequest.setVisibility(View.INVISIBLE);
                                                                    declineRequest.setEnabled(false);
                                                                }
                                                            }
                                                        });


                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
