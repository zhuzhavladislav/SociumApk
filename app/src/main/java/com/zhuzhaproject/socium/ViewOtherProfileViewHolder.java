package com.zhuzhaproject.socium;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewOtherProfileViewHolder extends RecyclerView.ViewHolder {

    CircleImageView profileImageUrl;
    TextView username, profession;

    public ViewOtherProfileViewHolder(@NonNull View itemView) {
        super(itemView);
        profileImageUrl = itemView.findViewById(R.id.profileImage);
        username = itemView.findViewById(R.id.user_name);
        profession = itemView.findViewById(R.id.profession);
    }
}
