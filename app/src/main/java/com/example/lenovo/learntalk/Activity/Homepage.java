package com.example.lenovo.learntalk.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.learntalk.R;
import com.example.lenovo.learntalk.Singleton.ListData;
import com.example.lenovo.learntalk.Singleton.UserData;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Homepage extends AppCompatActivity {

private DrawerLayout drawerLayout;    // contains name and  humburger
    private ImageButton addNewPost;
private NavigationView navigationView;  //contains all the options
private RecyclerView recyclerView;    //post appears on homepage
private android.support.v7.widget.Toolbar mToolbar;   //Developer-made Toolbar
private ActionBarDrawerToggle actionBarDrawerToggle;  //humburger
    private CircleImageView navProfilePhoto;
    private TextView navProfileName;
    private DatabaseReference firebaseDatabase,postRef,likesRef;
    private FirebaseAuth mFirebaseAuth;
    private String currentUserId;
    ArrayList<ListData> list;                  //to fetch data from database as mentioned in list
    Boolean likeChecker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

         firebaseDatabase= FirebaseDatabase.getInstance().getReference().child("users"); //retrieve the data to show in navigation bar
        postRef= FirebaseDatabase.getInstance().getReference().child("posts"); //retrieve post to show on homepage
        likesRef=FirebaseDatabase.getInstance().getReference().child("likes");

        drawerLayout=findViewById(R.id.drawable_layout);
        navigationView=findViewById(R.id.navigation_layout);
        recyclerView=findViewById(R.id.user_post);
        addNewPost=findViewById(R.id.add_post);
        mToolbar=findViewById(R.id.homepage_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        recyclerView.setHasFixedSize(true);   //doesn't depend on adapter content
        list=new ArrayList<ListData>();
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        mFirebaseAuth=FirebaseAuth.getInstance();

//        if(!mFirebaseAuth.getCurrentUser().getUid().equals(null))
//        {
            currentUserId=mFirebaseAuth.getCurrentUser().getUid();
//        }
//        else
//        {
//
//            Intent intent=new Intent(Homepage.this,LoginActivity.class);
//            startActivity(intent);
//        }

        //************************************PROFILE PIC OF USER*************HRADER*****************
        View navView=navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfilePhoto=navView.findViewById(R.id.profile_photo);
        navProfileName=navView.findViewById(R.id.profile_name);
        firebaseDatabase.child(currentUserId).addValueEventListener(new ValueEventListener() {  //GET DATA AND SET IT ON NAVIGATION BAR
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("username"))
                    {
                        final String name=dataSnapshot.child("username").getValue().toString();
                        navProfileName.setText(""+name);                                        //SET NAME ON NAVIGATION BAR
                    }
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        final String image=dataSnapshot.child("profileimage").getValue().toString();


                        Picasso.get().load(image).placeholder(R.mipmap.eclipse).into(navProfilePhoto); //SET PROFILE PIC ON NAVIGATION BAR
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Profile does not exist",Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //*******************SET TOGGLE BUTTON ON DRAWERLAYOUT*********************************************************
        actionBarDrawerToggle= new ActionBarDrawerToggle(Homepage.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //******************OPTION OF NAVIGATION VIEW SELECTED***************************************************************
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                userSelectedOption(item); //FUNCTION CALLED
                return false;
            }
        });

        //***********************************ADD NEW POST BUTTON****************************************
        addNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToPostActivity();
            }
        });


        //*******************************FUNCTION TO DISPLAY ALL THE PHOTOS*****************************************

        displayAllUsersPost();
    }
//*****************************DISPLAY ALL PHOTOS**********************************************************
    private void displayAllUsersPost()
    {
        Query sortPost=postRef.orderByChild("counter");

        FirebaseRecyclerOptions<UserData> options=                                         //FIREBASE RECYCLERVIEW
                new FirebaseRecyclerOptions.Builder<UserData>()
                .setQuery(sortPost,UserData.class)
                .build();

        FirebaseRecyclerAdapter<UserData,PostViewHolder> FirebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<UserData, PostViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final PostViewHolder holder, int position, @NonNull UserData model) {  //BIND DATA WITH LAYOUT
                        final String postKey=getRef(position).getKey();

                        holder.username.setText(model.getfullname());
                        holder.date.setText(model.getDate());
                        holder.time.setText(model.getTime());
                        holder.description.setText(model.getDescription());
                        Picasso.get().load(model.getImages()).into(holder.postImage);
                        Picasso.get().load(model.getProfileimage()).into(holder.circleImageView);
//*************************COMMAND TO GO TO THE DETAILS OF POST************************************************************
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent(Homepage.this,ClickpostActivity.class);
                                intent.putExtra("postkey",postKey);
                                startActivity(intent);
                            }
                        });
//******************************COMMAND TO SET NO OF LIKES ON A PHOTO*******************************************************************
                        holder.setLikeButtonStatus(postKey);    // WHEN WE REFRESH THE  DATA ON HOMEPAGE


// ******************************************************************SEND TO COMMENT ACTIVITY*******************************************8

                        holder.comment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                    Intent i=new Intent(Homepage.this,CommentActivity.class);
                                    i.putExtra("postkey",postKey);
                                    startActivity(i);

                            }
                        });


//********************************COMMAND TO UPDATE LIKE OR UNLIKE OF THE PHOTO TO FIREBASE*****************************************
                        holder.unlike.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                likeChecker=true;

                                likesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(likeChecker)
                                        {
                                            if(dataSnapshot.child(postKey).hasChild(currentUserId))          //IF THE LIKE ALREADY EXIST THEN IT DISLIKE THE DATA
                                            {
                                                likesRef.child(postKey).child(currentUserId).removeValue();
                                                likeChecker=false;
                                            }
                                            else
                                            {
                                                likesRef.child(postKey).child(currentUserId).setValue(true);  //IF NEW POST THEN LIKE THE POST
                                                likeChecker=false;
                                            }
                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {                             //CREATE VIEWHOLDER TO ATTACH LAYOUT
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
                        PostViewHolder postViewHolder=new PostViewHolder(view);
                        return postViewHolder;
                    }
                };

        recyclerView.setAdapter(FirebaseRecyclerAdapter);
        FirebaseRecyclerAdapter.startListening();
    }






    public static class PostViewHolder extends RecyclerView.ViewHolder                                         //VIEWHOLDER TO DEAL WITH THE STRUCTURE OF LAYOUT
    {
        View mView;
        CircleImageView circleImageView;
        TextView username,date,time,description,likeText;
        ImageView postImage;

        ImageButton unlike,comment;
        int countLikes;
        String currentUserID;
        DatabaseReference likesRef;

        public PostViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            circleImageView=itemView.findViewById(R.id.profile_photo);
            username=itemView.findViewById(R.id.username);
            date=itemView.findViewById(R.id.Date);
            time=itemView.findViewById(R.id.Time);
            description=itemView.findViewById(R.id.post_description);
            postImage=itemView.findViewById(R.id.user_post);
            unlike=itemView.findViewById(R.id.Unlike_button);
            comment=itemView.findViewById(R.id.comment);
            likeText=itemView.findViewById(R.id.like_Text);

            likesRef=FirebaseDatabase.getInstance().getReference().child("likes");
            currentUserID=FirebaseAuth.getInstance().getCurrentUser().getUid();




        }
//**************************COMMAND TO CHANGE THE APPEARANCE WHEN SOMEONELIKES OR DISLIKES****************************************************************
        public void setLikeButtonStatus(final String postKey)
        {
            Log.d("yhadkh","click k thoda phle bad");
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Log.d("yhadkh","click bht k bad");
                    if(dataSnapshot.child(postKey).hasChild(currentUserID))   //**********IF YOU LIKE THEN TURNS INTO RED
                    {
                        countLikes=(int) dataSnapshot.child(postKey).getChildrenCount();
                        unlike.setImageResource(R.drawable.like);
                        likeText.setText((Integer.toString(countLikes))+" Likes");
                    }
                    else
                    {
                        countLikes=(int) dataSnapshot.child(postKey).getChildrenCount(); //***************IF DISLIKE THEN TURNS INTO NORMAL
                        unlike.setImageResource(R.drawable.unlike);
                        likeText.setText((Integer.toString(countLikes))+" Likes");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }





    //***************************FUNCTION TO SEND IN POSTACTIVITY***********************************************************
    private void sendToPostActivity() {
        Intent i=new Intent(Homepage.this,PostActivity.class);
        startActivity(i);
    }


    //*******************************CLICK ON TOGGLE BUTTON**********************************************
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

   //**********************CHECK WHETHER ID EXIST OR NOT***********************************************************

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

        if(currentUser==null)
        {
            sendToLoginActivity();
        }

        else
        {
            checkUserExistance();
        }
    }




    //**************************************CHECK WHETHER USER INFORMATION EXIST********************************************************************
    private void checkUserExistance() {
        final String currentUserId=mFirebaseAuth.getCurrentUser().getUid();
        firebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(currentUserId))
                {
                    sendToSetupActivity();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendToSetupActivity() {
        Intent i=new Intent(Homepage.this,Setup.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    //*************************************ADD REQUIRED INFORMATION IN SETUP ACTIVITY**********************************************************
    private void sendToSettingActivity() {
        Intent i=new Intent(Homepage.this,SettingActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void sendToLoginActivity() {
        Intent i=new Intent(Homepage.this,LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
//**************************************************************************************************************
    private void userSelectedOption(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.Add_post:
                sendToPostActivity();
                break;

            case R.id.Profile:
                sendToProfileActivity();
                Toast.makeText(getApplicationContext(),"Profile",Toast.LENGTH_SHORT).show();
                break;

            case R.id.home:
                Toast.makeText(getApplicationContext(),"Homepage",Toast.LENGTH_SHORT).show();
                break;

            case R.id.Friends:
                sendToFriendlistActivity();
                Toast.makeText(getApplicationContext(),"Friends",Toast.LENGTH_SHORT).show();
                break;

            case R.id.Find_Friends:
                sendToFindFriendActivity();
                Toast.makeText(getApplicationContext(),"Find Friends",Toast.LENGTH_SHORT).show();
                break;

            case R.id.Message:
                sendToFriendlistActivity();
                Toast.makeText(getApplicationContext(),"Messages",Toast.LENGTH_SHORT).show();
                break;

            case R.id.Setting:
                sendToSettingActivity();
                break;

            case R.id.logout:
               mFirebaseAuth.signOut();
               sendToLoginActivity();
                break;
        }
    }

    private void sendToFriendlistActivity() {

        Intent i=new Intent(Homepage.this,Friends.class);
        startActivity(i);
    }

    private void sendToProfileActivity() {
        Intent i=new Intent(Homepage.this,ProfileActivity.class);
        startActivity(i);
    }

    private void sendToFindFriendActivity() {
        Intent i=new Intent(Homepage.this,FindFriendActivity.class);
        startActivity(i);
    }
}
