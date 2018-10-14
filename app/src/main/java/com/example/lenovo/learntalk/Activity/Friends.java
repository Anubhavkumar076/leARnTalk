package com.example.lenovo.learntalk.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lenovo.learntalk.R;
import com.example.lenovo.learntalk.Singleton.AllFriends;
import com.example.lenovo.learntalk.Singleton.FindFriends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Friends extends AppCompatActivity {

    private RecyclerView friendList;
    private DatabaseReference friendsRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        friendList=findViewById(R.id.friendslist_recyclerview);



        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        friendsRef= FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID);
        userRef=FirebaseDatabase.getInstance().getReference().child("users");

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        friendList.setHasFixedSize(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        friendList.setLayoutManager(linearLayoutManager);


        displayAllFriends();
    }

    private void displayAllFriends() {

        FirebaseRecyclerOptions<AllFriends> options=                                         //FIREBASE RECYCLERVIEW
                new FirebaseRecyclerOptions.Builder<AllFriends>()
                        .setQuery(friendsRef,AllFriends.class)
                        .build();

        FirebaseRecyclerAdapter<AllFriends,Friends.FriendlistViewholder> FirebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<AllFriends, FriendlistViewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final Friends.FriendlistViewholder holder, final int position, @NonNull AllFriends model) {  //BIND DATA WITH LAYOUT

                        holder.Date.setText("Friends Since: "+model.getDate());
                        final String userIDs=getRef(position).getKey();

                        userRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists())
                                {
                                    final String username=dataSnapshot.child("fullname").getValue().toString();
                                    final String profileImage=dataSnapshot.child("profileimage").getValue().toString();

                                    holder.Username.setText(username);
                                    Picasso.get().load(profileImage).into(holder.profilepic);

                                    holder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Log.d("friendska","dialoguebox");
                                            CharSequence options[]=new CharSequence[]
                                                    {
                                                            username+"'s Profile",
                                                            "Send Message"
                                                    };

                                            AlertDialog.Builder builder=new AlertDialog.Builder(Friends.this);  //Show dialog box when click on it
                                            builder.setTitle("Select Options");


                                           // alert.setTitle("Select Options");
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    if(i==0)
                                                    {
                                                        Intent profileIntent=new Intent(Friends.this,PersonProfileActivity.class);
                                                        profileIntent.putExtra("visit_user_id",""+userIDs);
                                                        startActivity(profileIntent);
                                                    }
                                                    if(i==1)
                                                    {
                                                        Intent chatIntent=new Intent(Friends.this,ChatActivity.class);
                                                        chatIntent.putExtra("visit_user_id",""+userIDs);
                                                        chatIntent.putExtra("Username",""+username);
                                                        startActivity(chatIntent);
                                                    }
                                                }
                                            });

                                            AlertDialog alert=builder.create();
                                            alert.show();        //command to show dialog box after click
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });




                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String visit_user_id=getRef(position).getKey();

                                Intent i=new Intent(Friends.this,PersonProfileActivity.class);
                                i.putExtra("visit_user_id",visit_user_id);
                                startActivity(i);

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public FriendlistViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {                             //CREATE VIEWHOLDER TO ATTACH LAYOUT
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends,parent,false);
                        FriendlistViewholder findFriendViewHolder=new FriendlistViewholder(view);
                        return findFriendViewHolder;
                    }
                };

        friendList.setAdapter(FirebaseRecyclerAdapter);
        FirebaseRecyclerAdapter.startListening();
    }

    public class FriendlistViewholder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView Username,Date;
        CircleImageView profilepic;
        public FriendlistViewholder(View itemView) {
            super(itemView);
            mView=itemView;
            Username=itemView.findViewById(R.id.Layout_Find_Friends_Username);
            Date=itemView.findViewById(R.id.Layout_Find_Friends_Status);
            profilepic=itemView.findViewById(R.id.Layout_Find_Friends);
        }
    }
}
