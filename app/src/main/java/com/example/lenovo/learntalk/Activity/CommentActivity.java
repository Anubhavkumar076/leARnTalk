package com.example.lenovo.learntalk.Activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.learntalk.R;
import com.example.lenovo.learntalk.Singleton.Comments;
import com.example.lenovo.learntalk.Singleton.UserData;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentActivity extends AppCompatActivity {

    private EditText CommentEditText;
    private ImageButton CommentPost;
    private RecyclerView Comments;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef,postRef;
    private String Post_Key,current_user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        CommentEditText=findViewById(R.id.comment_editText);
        CommentPost=findViewById(R.id.comment_post);
        Comments=findViewById(R.id.comment_list);


        mAuth=FirebaseAuth.getInstance();
        current_user_id=mAuth.getCurrentUser().getUid();


        Post_Key=getIntent().getExtras().get("postkey").toString();
        postRef= FirebaseDatabase.getInstance().getReference().child("posts").child(Post_Key).child("comments");
        userRef= FirebaseDatabase.getInstance().getReference().child("users");

        Comments.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        Comments.setLayoutManager(linearLayoutManager);

        CommentPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists())
                        {
                            String username=dataSnapshot.child("username").getValue().toString();
                            validateComment(username);

                            CommentEditText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });



    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Comments> options=                                         //FIREBASE RECYCLERVIEW
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(postRef,Comments.class)
                        .build();


        FirebaseRecyclerAdapter<Comments,CommentActivity.CommentsViewHolder> FirebaseRecyclerAdapter=new
                FirebaseRecyclerAdapter<com.example.lenovo.learntalk.Singleton.Comments, CommentsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {
                        holder.CommentUsername.setText(model.getUsername());
                        holder.CommentDate.setText(model.getDate());
                        holder.CommentText.setText(model.getComment());
                        holder.CommentTime.setText(model.getTime());

                    }

                    @NonNull
                    @Override
                    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comment_layout,parent,false);
                        CommentActivity.CommentsViewHolder commentsViewHolder=new CommentsViewHolder(view);
                        return commentsViewHolder;
                    }
                };

        Comments.setAdapter(FirebaseRecyclerAdapter);
        FirebaseRecyclerAdapter.startListening();



    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView CommentUsername,CommentDate,CommentTime,CommentText;
        public CommentsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            CommentUsername=itemView.findViewById(R.id.comment_name);
            CommentDate=itemView.findViewById(R.id.comment_date);
            CommentTime=itemView.findViewById(R.id.comment_time);
            CommentText=itemView.findViewById(R.id.comment_display);
        }
    }

    private void validateComment(String username) {

        String textComment=CommentEditText.getText().toString().trim();

        if(TextUtils.isEmpty(textComment))
        {
            Toast.makeText(getApplicationContext(),"comment Section Empty...",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calendarForDate=Calendar.getInstance();
            SimpleDateFormat calendarDate=new SimpleDateFormat("dd-MMMM-yyyy");
            final String SaveCurrentdate=calendarDate.format(calendarForDate.getTime());

            Calendar calendarForTime=Calendar.getInstance();
            SimpleDateFormat calendarTime=new SimpleDateFormat("HH:mm");
            final String SaveCurrentTime=calendarTime.format(calendarForTime.getTime());

            final String RandomName=current_user_id+SaveCurrentdate+SaveCurrentTime;

            HashMap commentMap=new HashMap();
            commentMap.put("uid",current_user_id);
            commentMap.put("comment",textComment);
            commentMap.put("date",SaveCurrentdate);
            commentMap.put("time",SaveCurrentTime);
            commentMap.put("username",username);

            postRef.child(RandomName).updateChildren(commentMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if(task.isSuccessful())
                            {
                                Toast.makeText(getApplicationContext(),"commented successfully",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String Message=task.getException().toString();
                                Toast.makeText(getApplicationContext(),"Error..... Try Again"+Message,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
