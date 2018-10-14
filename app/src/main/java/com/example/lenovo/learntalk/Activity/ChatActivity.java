package com.example.lenovo.learntalk.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.learntalk.Adapters.MessageAdapter;
import com.example.lenovo.learntalk.R;
import com.example.lenovo.learntalk.Singleton.messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolbar;
    private RecyclerView userMessageList;
    private ImageButton chatImages,chatSend;
    private EditText chatEdittext;
    private TextView recieverName;
    private CircleImageView recieverProfilePicture;
    private String messageRecieverID,messageRecieverName,messageSenderName,messageSenderID,SaveCurrentdate,SaveCurrentTime,downloadUrl;
    private DatabaseReference rootReference;
    private FirebaseAuth mAuth;
    private List<messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private StorageReference imageStorageReference;
    ProgressDialog progressBar;
    private static int GALLERY_PICK=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootReference=FirebaseDatabase.getInstance().getReference();

        mAuth=FirebaseAuth.getInstance();
        messageSenderID=mAuth.getCurrentUser().getUid();   //Your Name

        messageRecieverID=getIntent().getExtras().get("visit_user_id").toString();  //Id of user which is selecting by you for send msg.
        messageRecieverName=getIntent().getExtras().get("Username").toString();  //Name of that user



        chatToolbar=findViewById(R.id.chatactivity_actionbar);
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setTitle("");

        ActionBar actionBar=getSupportActionBar();

//        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView=layoutInflater.inflate(R.layout.chatbar,null);
        actionBar.setCustomView(actionbarView);

        rootReference= FirebaseDatabase.getInstance().getReference();
        progressBar=new ProgressDialog(this);

        userMessageList=findViewById(R.id.chatactivity_chatfield);
        chatImages=findViewById(R.id.chatactivity_camera);
        chatSend=findViewById(R.id.chatactivity_message);
        chatEdittext=findViewById(R.id.chatactivity_edittext);
        recieverName=findViewById(R.id.chatbar_profilename);
        recieverProfilePicture=findViewById(R.id.chatbar_ProfileImage);



        messageAdapter=new MessageAdapter(messagesList);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);

        imageStorageReference= FirebaseStorage.getInstance().getReference().child("messagespicture");





        displayRecieverInfo();

        chatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();
            }
        });


        chatImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_PICK);
            }
        });

        
        fetchMessages();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK && data!=null)
        {

            progressBar.setTitle("sending pics");
            progressBar.setMessage("Wait For A While");
            progressBar.show();
            progressBar.setCanceledOnTouchOutside(true);
            Uri uriImage = data.getData();

            final String messageSenderReference="messages/"+messageSenderID+"/"+messageRecieverID;
            final String messageRecieverReference="messages/"+messageRecieverID+"/"+messageSenderID;

            DatabaseReference userMessageKey=rootReference.child("messages").child(messageSenderID)
                    .child(messageRecieverID).push();

            final String messagePushID=userMessageKey.getKey();

            final StorageReference filePath=imageStorageReference.child(messagePushID+".jpg");

            filePath.putFile(uriImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful())
                    {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl=uri.toString();
                                Log.d("download",""+downloadUrl);
                            }
                        });

                        Map messageTextBody=new HashMap();
                        messageTextBody.put("message",downloadUrl);
                        messageTextBody.put("type","image");
                        messageTextBody.put("from",messageSenderID);
                        messageTextBody.put("date",SaveCurrentdate);
                        messageTextBody.put("time",SaveCurrentTime);

                        Map messageBodyDetails=new HashMap();
                        messageBodyDetails.put(messageSenderReference+"/"+messagePushID,messageTextBody);
                        messageBodyDetails.put(messageRecieverReference+"/"+messagePushID,messageTextBody);

                        rootReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError!=null)
                                {
                                    Log.d("image_log",databaseError.getMessage().toString());
                                }
                                chatEdittext.setText("");
                                progressBar.dismiss();

                            }
                        });

                        Toast.makeText(getApplicationContext(),"picture sent Successfully",Toast.LENGTH_SHORT).show();
                        progressBar.dismiss();
                    }
                    else
                    {
                            Toast.makeText(getApplicationContext(),"picture sent Successfully",Toast.LENGTH_SHORT).show();
                            progressBar.dismiss();

                    }


                }
            });
        }
    }



    private void fetchMessages() {

        rootReference.child("messages").child(messageSenderID).child(messageRecieverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        messages message=dataSnapshot.getValue(messages.class);
                        messagesList.add(message);
                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }



//****************************************Program of button to send messages***********************************************************
    private void sendMessage()
    {
        String messageText=chatEdittext.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(getApplicationContext(),"chatbox is empty",Toast.LENGTH_SHORT).show();
        }
        else {

            String messageSenderReference="messages/"+messageSenderID+"/"+messageRecieverID;
            String messageRecieverReference="messages/"+messageRecieverID+"/"+messageSenderID;

            DatabaseReference userMessageKey=rootReference.child("messages").child(messageSenderID)
                                                .child(messageRecieverID).push();

            String messagePushID=userMessageKey.getKey();

            Calendar calendarForDate=Calendar.getInstance();
            SimpleDateFormat calendarDate=new SimpleDateFormat("dd-MMMM-yyyy");
            SaveCurrentdate=calendarDate.format(calendarForDate.getTime());

            Calendar calendarForTime=Calendar.getInstance();
            SimpleDateFormat calendarTime=new SimpleDateFormat("HH:mm aa");
            SaveCurrentTime=calendarTime.format(calendarForTime.getTime());

            Map messageTextBody=new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("date",SaveCurrentdate);
            messageTextBody.put("time",SaveCurrentTime);

            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderReference+"/"+messagePushID,messageTextBody);
            messageBodyDetails.put(messageRecieverReference+"/"+messagePushID,messageTextBody);


            rootReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if(databaseError!=null)
                    {
                        Log.d("chat_log",databaseError.getMessage().toString());
                    }

                    chatEdittext.setText("");
                }
            });
        }
    }


    //********************************SHOW USER PROFILE PIC AND NAME ON CHAT BAR*******************************
    private void displayRecieverInfo() {

        recieverName.setText(messageRecieverName);
        rootReference.child("users").child(messageRecieverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    final String profileImage=dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.get().load(profileImage).into(recieverProfilePicture);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
