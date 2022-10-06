package com.zhuzhaproject.socium;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsViewHolder extends RecyclerView.ViewHolder {

    ImageView comment_item_delete, profile_image;
    TextView user_name, comment_text, comment_time;
    CircleImageView profileImage;


    public CommentsViewHolder(View itemView) {
        super(itemView);
        profile_image = itemView.findViewById(R.id.profile_image);
        profileImage = itemView.findViewById(R.id.profile_image);
        user_name = itemView.findViewById(R.id.user_name);
        comment_text = itemView.findViewById(R.id.comment_text);
        comment_time = itemView.findViewById(R.id.textUnder);
    }

    public void setDeleteView(boolean delete) {

        comment_item_delete = itemView.findViewById(R.id.comment_item_delete);
        if (delete) {
            comment_item_delete.setVisibility(View.VISIBLE);
        } else {
            comment_item_delete.setVisibility(View.GONE);
        }

    }

    public void setUsername(String name) {
        user_name.setText(name);
    }

    public void setProfileImage(String userImage) {
        Picasso.get().load(userImage).into(profileImage);
    }

    public void setText(String text) {
        comment_text.setText(text);
    }

    public void setTime(String time) {
        comment_time.setText(time);
    }

}