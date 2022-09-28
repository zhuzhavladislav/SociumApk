package com.zhuzhaproject.socium;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyViewHolder extends RecyclerView.ViewHolder{
    View mView;
    ImageView likes_but;
    LinearLayout imageView;
    LinearLayout textView;
    ImageView comments_but;
    ImageView mom_delete;
    LinearLayout mom_delete_view;
    LinearLayout profile_view;

    public MyViewHolder(View itemView) {
        super(itemView);
        likes_but = (ImageView) itemView.findViewById(R.id.mom_likes_image);
        imageView = (LinearLayout) itemView.findViewById(R.id.mom_image_view);
        textView = (LinearLayout) itemView.findViewById(R.id.mom_text_view);
        comments_but = (ImageView) itemView.findViewById(R.id.mom_comment_but);
        mom_delete = (ImageView) itemView.findViewById(R.id.mom_item_delete);
        mom_delete_view = (LinearLayout) itemView.findViewById(R.id.delete_comment_view);
        profile_view = (LinearLayout) itemView.findViewById(R.id.profile_view);

    }

    public void setCommentsCount(int count){
        TextView setcount_tv = (TextView) itemView.findViewById(R.id.mom_comments_text);
        setcount_tv.setText(count+"");
    }
    public void setImage(String image){
        ImageView moment_image = (ImageView) itemView.findViewById(R.id.mom_image_item);
        Picasso.get().load("com.google.android.gms.tasks.zzu@d68bf1b").into(moment_image);
    }
    public void setText(String text){
        TextView text_tv = (TextView) itemView.findViewById(R.id.mom_text_item);
        text_tv.setText(text);
    }

    public void setLikes(final int likes){
        TextView setLikes_tv = (TextView) itemView.findViewById(R.id.mom_likes_text);
        setLikes_tv.setText(likes+"");
    }


    public void setName(String name){
        TextView nameview = (TextView) itemView.findViewById(R.id.mom_name);
        nameview.setText(name);
    }
    public void setDp(String thumb_image, Context ctx){
        CircleImageView userdp = (CircleImageView) itemView.findViewById(R.id.mom_dp);
        Picasso.get().load(thumb_image).placeholder(R.drawable.ic_person_black_24dp).into(userdp);
    }

    public void setTime(String time){
        TextView timeview = (TextView) itemView.findViewById(R.id.mom_time);
        timeview.setText(time);
    }
}