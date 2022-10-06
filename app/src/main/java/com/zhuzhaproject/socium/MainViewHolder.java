package com.zhuzhaproject.socium;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainViewHolder extends RecyclerView.ViewHolder{
    ImageView post_like_image, post_comment_image, post_item_delete;
    TextView post_description, post_comments_count, post_likes_count, user_name, post_time;
    Button post_like_button, post_comment_button;
    ImageView post_image;
    CircleImageView user_image;

    public MainViewHolder(View itemView) {
        super(itemView);
        post_like_image = (ImageView) itemView.findViewById(R.id.post_like_image);
        post_comment_image = (ImageView) itemView.findViewById(R.id.post_comment_image);
        post_item_delete = (ImageView) itemView.findViewById(R.id.post_item_delete);
        post_description = (TextView) itemView.findViewById(R.id.post_description);
        post_like_button = (Button) itemView.findViewById(R.id.post_like_button);
        post_comment_button = (Button) itemView.findViewById(R.id.post_comment_button);
        post_image = (ImageView) itemView.findViewById(R.id.post_image);
        user_image = (CircleImageView) itemView.findViewById(R.id.user_image);
        post_comments_count = (TextView) itemView.findViewById(R.id.post_comments_count);
        post_likes_count = (TextView) itemView.findViewById(R.id.post_likes_count);
        user_name = (TextView) itemView.findViewById(R.id.user_name);
        post_time = (TextView) itemView.findViewById(R.id.post_time);

    }

    public void setCommentsCount(int count){
        post_comments_count.setText(count+"");
    }
    public void setPostImage(String image){
        Picasso.get().load(image).into(post_image);
    }
    public void setPostDescription(String text){
        post_description.setText(text);
    }
    public void setLikesCount(final int likes){
        post_likes_count.setText(likes+"");
    }
    public void setUserName(String name){
        user_name.setText(name);
    }
    public void setUserImage(String profileImage){
        Picasso.get().load(profileImage).into(user_image);
    }
    public void setPostTime(String time){
        post_time.setText(time);
    }
}