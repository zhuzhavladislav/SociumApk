package com.zhuzhaproject.socium;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersViewHolder extends RecyclerView.ViewHolder {

    CircleImageView profileImage;
    TextView username, profession;

    public AllUsersViewHolder(@NonNull View itemView) {
        super(itemView);
        profileImage=itemView.findViewById(R.id.profile_image);
        username=itemView.findViewById(R.id.user_name);
        profession=itemView.findViewById(R.id.textUnder);
    }
}
