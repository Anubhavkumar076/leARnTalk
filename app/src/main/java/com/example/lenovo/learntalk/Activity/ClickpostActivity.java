package com.example.lenovo.learntalk.Activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.learntalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickpostActivity extends AppCompatActivity {

    ImageView postImage;
    TextView postDescription;
    Button editPost,deletePost;
    private String postKey,currentUserID,databaseUserID,description,image;
    DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clickpost);
        postImage=findViewById(R.id.post);
        postDescription=findViewById(R.id.status);
        editPost=findViewById(R.id.edit_post);
        deletePost=findViewById(R.id.delete_post);

        editPost.setVisibility(View.INVISIBLE);
        deletePost.setVisibility(View.INVISIBLE);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();

        postKey= getIntent().getExtras().get("postkey").toString();                            //TO GATHER THE KEY COMING FROM HOMEPAGE(FIREBASE RECYCLERVIEW)
        clickPostRef= FirebaseDatabase.getInstance().getReference().child("posts").child(postKey);



        clickPostRef.addValueEventListener(new ValueEventListener() {                               //GATHER PHOTO AND SET IT ON THE IMAGEVIEW
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    databaseUserID=dataSnapshot.child("userid").getValue().toString();
                    description=dataSnapshot.child("description").getValue().toString();
                    image=dataSnapshot.child("images").getValue().toString();

                    postDescription.setText(description);
                    Picasso.get().load(image).into(postImage);


                    if(currentUserID.equals(databaseUserID))
                    {
                        editPost.setVisibility(View.VISIBLE);
                        deletePost.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deletePostFromDatabase();
            }
        });

        editPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editPostFromDatabase(description);
            }
        });
    }

    private void editPostFromDatabase(String description) {

        AlertDialog.Builder builder=new AlertDialog.Builder(ClickpostActivity.this);
        builder.setTitle("Edit Post");

        final EditText input=new EditText(ClickpostActivity.this);
        input.setText(description);
        builder.setView(input);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clickPostRef.child("description").setValue(input.getText().toString());
                Toast.makeText(getApplicationContext(),"Post Updated successfully...",Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                Toast.makeText(getApplicationContext(),"Cancel",Toast.LENGTH_SHORT).show();
            }
        });
        Dialog dialog=builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.color.colorPrimaryDark);

    }

    private void deletePostFromDatabase() {


        clickPostRef.removeValue();

        sendToHomePage();
    }

    private void sendToHomePage() {
        Intent i=new Intent(ClickpostActivity.this,Homepage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
