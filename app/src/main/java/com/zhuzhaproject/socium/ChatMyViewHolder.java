package com.zhuzhaproject.socium;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatMyViewHolder extends RecyclerView.ViewHolder {

    TextView firstUserText, secondUserText;

    public ChatMyViewHolder(@NonNull View itemView) {
        super(itemView);

        firstUserText=itemView.findViewById(R.id.firstUserText);
        secondUserText=itemView.findViewById(R.id.secondUserText);
    }
}
