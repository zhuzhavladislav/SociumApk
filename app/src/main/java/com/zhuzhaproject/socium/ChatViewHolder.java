package com.zhuzhaproject.socium;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatViewHolder extends RecyclerView.ViewHolder {

    TextView firstUserText, secondUserText;

    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);

        firstUserText=itemView.findViewById(R.id.firstUserText);
        secondUserText=itemView.findViewById(R.id.secondUserText);
    }
}
