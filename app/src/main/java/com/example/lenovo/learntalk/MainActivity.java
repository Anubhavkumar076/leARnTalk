package com.example.lenovo.learntalk;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

//    DatabaseReference reference;
//    RecyclerView recyclerView;
//    List<Post> list;
//    MyAdapter myAdapter;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//
//        recyclerView=findViewById(R.id.recycler);
//        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(linearLayoutManager);
//        list=new ArrayList<Post>();
//
//        reference= FirebaseDatabase.getInstance().getReference().child("Posts");
//
//
//
//
//
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
//                {
//
//                    ListItem listData=dataSnapshot1.getValue(ListItem.class);
//                    //  Post post=dataSnapshot1.getValue(Post.class);
//                   // ListData listData=new ListData();
//                    Post post=new Post();//---------------------
//                    //String name=listData.getFullname();
//                   // String time=listData.getTime();
//                    String date=listData.getDate();
//                    String description=listData.getDescription();
//                    String profile=listData.getProfileimage();
//
//                    post.setFullname("abc");
//                    post.setTime("efg");
//                    post.setDate1(date);
//                    post.setDescription(description);
//                    post.setProfileimage(profile);
//                    list.add(post);
//
////                    Post post1=new Post(post);
////                    list.add(post1);
//                    Log.d("aayatohai"," "+post);
//
//
//                }
//
//
//                myAdapter=new MyAdapter(MainActivity.this,list);
//                myAdapter.notifyDataSetChanged();
//                recyclerView.setAdapter(myAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//    }
}
