package com.example.lenovo.learntalk.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.learntalk.R;
import com.example.lenovo.learntalk.Singleton.FindFriends;
import com.example.lenovo.learntalk.Singleton.UserData;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendActivity extends AppCompatActivity {
Toolbar mToolbar;
EditText FindSearch;
ImageButton FindSearchButton;
RecyclerView mRecyclerView;
DatabaseReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);
        mToolbar=findViewById(R.id.Find_Friends_Toolbar);
        FindSearch=findViewById(R.id.Find_Friends_Search);
        FindSearchButton=findViewById(R.id.Find_Friends_Search_Button);
        mRecyclerView=findViewById(R.id.Find_Friends_Recycler);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d("Friendska","oncreate");

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        userRef= FirebaseDatabase.getInstance().getReference().child("users");


        //************************When we click on search button****************************************************

        FindSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchInput=FindSearch.getText().toString().trim();
                searchFriends(searchInput);
            }
        });

    }

    private void searchFriends(String searchInput) {

        Toast.makeText(getApplicationContext(),"Searching",Toast.LENGTH_SHORT).show();

        Query searchFriend=userRef.orderByChild("fullname")
                .startAt(searchInput).endAt(searchInput+"\uf8ff");

        FirebaseRecyclerOptions<FindFriends> options=                                         //FIREBASE RECYCLERVIEW
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(searchFriend,FindFriends.class)
                        .build();

        FirebaseRecyclerAdapter<FindFriends,FindFriendActivity.FindFriendViewHolder> FirebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<FindFriends, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendActivity.FindFriendViewHolder holder, final int position, @NonNull FindFriends model) {  //BIND DATA WITH LAYOUT
                       // final String postKey=getRef(position).getKey();

                        holder.Username.setText(model.getFullname());
                        holder.Status.setText(model.getStatus());
                        Picasso.get().load(model.getProfileimage()).into(holder.profilepic);


                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String visit_user_id=getRef(position).getKey();

                                Intent i=new Intent(FindFriendActivity.this,PersonProfileActivity.class);
                                i.putExtra("visit_user_id",visit_user_id);
                                startActivity(i);

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public FindFriendActivity.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {                             //CREATE VIEWHOLDER TO ATTACH LAYOUT
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends,parent,false);
                        FindFriendActivity.FindFriendViewHolder findFriendViewHolder=new FindFriendActivity.FindFriendViewHolder(view);
                        return findFriendViewHolder;
                    }
                };

        mRecyclerView.setAdapter(FirebaseRecyclerAdapter);
        FirebaseRecyclerAdapter.startListening();

    }
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder                                         //VIEWHOLDER TO DEAL WITH THE STRUCTURE OF LAYOUT
    {
        View mView;
        TextView Username,Status;
        CircleImageView profilepic;


        public FindFriendViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            Username=itemView.findViewById(R.id.Layout_Find_Friends_Username);
            Status=itemView.findViewById(R.id.Layout_Find_Friends_Status);
            profilepic=itemView.findViewById(R.id.Layout_Find_Friends);
        }
    }
}
