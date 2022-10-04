package com.zhuzhaproject.socium;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileViewHolder extends RecyclerView.ViewHolder {

    CircleImageView profileImageUrl;
    TextView username, profession;

    public ProfileViewHolder(@NonNull View itemView) {
        super(itemView);
        profileImageUrl = itemView.findViewById(R.id.profile_image);
        username = itemView.findViewById(R.id.user_name);
        profession = itemView.findViewById(R.id.comment_time);
    }
}