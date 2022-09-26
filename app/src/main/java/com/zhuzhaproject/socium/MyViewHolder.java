package com.zhuzhaproject.socium;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    CircleImageView profileImage;
    ImageView postImage, likeImage, commentsImage;
    Button likeButton;
    TextView username, timeAgo, postDesc, likeCounter, commentCounter;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        profileImage=itemView.findViewById(R.id.profileImagePost);
        postImage=itemView.findViewById(R.id.postImage);
        username=itemView.findViewById(R.id.profileUsernamePost);
        timeAgo=itemView.findViewById(R.id.timeAgo);
        postDesc=itemView.findViewById(R.id.postDesc);
        likeImage=itemView.findViewById(R.id.likeImage);
        commentsImage=itemView.findViewById(R.id.commentsImage);
        likeCounter=itemView.findViewById(R.id.likeCounter);
        commentCounter=itemView.findViewById(R.id.commentCounter);
        likeButton=itemView.findViewById(R.id.likeButton);
    }

    public void countLikes(String postKey, String uid, DatabaseReference likeRef) {
        likeRef.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    int totalLikes= (int) snapshot.getChildrenCount();
                    likeCounter.setText(totalLikes+"");
                }else{
                    likeCounter.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        likeRef.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(uid).exists())
                {
                    likeImage.setImageResource(R.drawable.ic_like_pressed);
                    likeImage.setColorFilter(Color.RED);
                    likeCounter.setTextColor(Color.RED);
                }else{
                    likeImage.setImageResource(R.drawable.ic_like);
                    likeImage.setColorFilter(Color.GRAY);
                    likeCounter.setTextColor(Color.GRAY);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
