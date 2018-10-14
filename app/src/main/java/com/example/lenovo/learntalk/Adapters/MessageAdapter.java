package com.example.lenovo.learntalk.Adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lenovo.learntalk.R;
import com.example.lenovo.learntalk.Singleton.messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.messageViewHolder>{

    private List<messages> userMessageList;
    FirebaseAuth mAuth;
    DatabaseReference userDatabaseRef;

    public MessageAdapter (List<messages> userMessageList)
    {
        this.userMessageList=userMessageList;
    }

    @NonNull
    @Override
    public MessageAdapter.messageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout_users,parent,false);
        mAuth=FirebaseAuth.getInstance();
        return new messageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.messageViewHolder holder, int position) {
        String messageUserID=mAuth.getCurrentUser().getUid();
        messages message=userMessageList.get(position);
        String fromUserID=message.getFrom();
        String fromMessageType=message.getType();

        userDatabaseRef=FirebaseDatabase.getInstance().getReference().child("users").child(fromUserID);
        userDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username=dataSnapshot.child("fullname").getValue().toString();
                String userImage=dataSnapshot.child("profileimage").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


//        if(fromMessageType.equals("text"))
//        {
            holder.messageImage.setVisibility(View.INVISIBLE);
            if(fromUserID.equals(messageUserID))
            {
                holder.messageDisplay.setBackgroundResource(R.drawable.senders_msg_background);
                holder.messageDisplay.setTextColor(Color.WHITE);
                holder.messageDisplay.setGravity(Gravity.RIGHT);
            }
            else
            {
                holder.messageDisplay.setBackgroundResource(R.drawable.recievers_msg_background);
                holder.messageDisplay.setTextColor(Color.BLACK);
                holder.messageDisplay.setGravity(Gravity.LEFT);
            }
            holder.messageDisplay.setText(message.getMessage());
//        }
//        else
//        {
//            holder.messageDisplay.setVisibility(View.INVISIBLE);
//            holder.messageDisplay.setPadding(0,0,0,0);
//
//            Picasso.get().load(message.getMessage()).into(holder.messageImage);
//        }




    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class messageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageDisplay;
        public CircleImageView userProfileDisplay;
        public ImageView messageImage;

        public messageViewHolder(View itemView) {
            super(itemView);

           // recieverMessageDisplay=itemView.findViewById(R.id.reciever_message_display);
            messageDisplay=itemView.findViewById(R.id.message_display);
            messageImage=itemView.findViewById(R.id.messageImageview);
           userProfileDisplay=itemView.findViewById(R.id.message_profile);
        }
    }
}
