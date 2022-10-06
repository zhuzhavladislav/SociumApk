package com.zhuzhaproject.socium;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllChatsViewHolder extends RecyclerView.ViewHolder {

    CircleImageView profileImage;
    TextView username, lastMessage;

    public AllChatsViewHolder(@NonNull View itemView) {
        super(itemView);
        profileImage=itemView.findViewById(R.id.profile_image);
        username=itemView.findViewById(R.id.user_name);
        lastMessage=itemView.findViewById(R.id.textUnder);
    }
}
