package com.zhuzhaproject.socium;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsViewHolder extends RecyclerView.ViewHolder {


    View mView;
    ImageView comment_item_delete, profile_image;


    public CommentsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        profile_image = itemView.findViewById(R.id.profile_image);
    }

    public void setDeleteView(boolean delete) {

        comment_item_delete = (ImageView) mView.findViewById(R.id.comment_item_delete);
        if (delete) {
            comment_item_delete.setVisibility(View.VISIBLE);
        } else {
            comment_item_delete.setVisibility(View.GONE);
        }

    }

    public void setName(String name) {
        TextView user_name = (TextView) mView.findViewById(R.id.user_name);
        user_name.setText(name);
    }

    public void setDp(String userImage) {
        CircleImageView profileImage = (CircleImageView) mView.findViewById(R.id.profile_image);
        Picasso.get().load(userImage).into(profileImage);
    }

    public void setText(String text) {
        TextView comment_text = (TextView) mView.findViewById(R.id.comment_text);
        comment_text.setText(text);
    }

    public void setTime(String time) {
        TextView comment_time = (TextView) mView.findViewById(R.id.status);
        comment_time.setText(time);
    }

}