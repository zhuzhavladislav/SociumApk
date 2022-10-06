package com.zhuzhaproject.socium;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.zhuzhaproject.socium.Utils.Comments;

import java.util.HashMap;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {


    DatabaseReference usersRef;
    DatabaseReference rootRef;
    DatabaseReference userpostsRef;
    DatabaseReference allpostsRef;
    TextView post_description;
    RecyclerView comment_view;
    TextView post_time;
    TextView user_name;
    EditText input_comment;
    FirebaseAuth mAuth;
    String Uid;
    String post_by;
    ImageView post_image, send_comment, user_image;
    FirebaseRecyclerOptions<Comments> options;
    FirebaseRecyclerAdapter<Comments, CommentsViewHolder> adapter;
    View line;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        input_comment = (EditText) findViewById(R.id.input_comment);
        post_time = (TextView) findViewById(R.id.post_time);
        send_comment = (ImageView) findViewById(R.id.send_comment);
        post_description = (TextView) findViewById(R.id.post_description);
        post_image = (ImageView) findViewById(R.id.post_image);
        user_name = (TextView) findViewById(R.id.user_name);
        user_image = (ImageView) findViewById(R.id.user_image);
        line = (View) findViewById(R.id.line);

        comment_view = findViewById(R.id.comment_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        comment_view.setLayoutManager(layoutManager);
        final String post_id = getIntent().getStringExtra("post_id");
        post_by = getIntent().getStringExtra("name");

        userpostsRef = FirebaseDatabase.getInstance().getReference().child("UsersPost");
        allpostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");


        // отключение анимации
        overridePendingTransition(0, 0);

        //changing statusbar color
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        }


        allpostsRef.child(post_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String timestamp = dataSnapshot.child("timestamp").getValue().toString();
                GetTimeAgo gta = new GetTimeAgo();
                final String postTime = gta.getTimeAgo(Long.valueOf(timestamp));
                String postDesc = dataSnapshot.child("postDesc").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                post_time.setText("Опубликовано " + postTime);
                if (dataSnapshot.child("type").getValue().toString().equals("text")) {
                    post_description.setText(dataSnapshot.child("postDesc").getValue().toString());
                    line.setVisibility(View.VISIBLE);
                    post_image.setVisibility(View.GONE);
                } else {
                    post_image.setVisibility(View.VISIBLE);
                    line.setVisibility(View.GONE);
                    post_description.setText(dataSnapshot.child("postDesc").getValue().toString());
                    Picasso.get().load(image).into(post_image);
                }
                if (dataSnapshot.child("postDesc").getValue().toString().equals("")) {
                    post_description.setVisibility(View.GONE);
                } else {
                    post_description.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = rootRef.child("Users");

        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();


        usersRef.child(post_by).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue().toString();
                String userImage = dataSnapshot.child("profileImage").getValue().toString();
                Picasso.get().load(userImage).into(user_image);
                user_name.setText(username);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        loadcomments(post_id);

        input_comment.addTextChangedListener(commentTextWatcher);
        send_comment.setClickable(false);

        send_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment(post_id);
            }
        });


    }

    private TextWatcher commentTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String inputTextSms = input_comment.getText().toString().trim();
            if (inputTextSms.isEmpty()) {
                send_comment.setColorFilter(Color.rgb(154, 156, 164));
                send_comment.setClickable(false);
            } else {
                send_comment.setColorFilter(Color.rgb(67, 205, 232));
                send_comment.setClickable(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void postComment(String post_id) {
        String commentToPost = input_comment.getText().toString().trim();
        input_comment.setText("");

        if (!TextUtils.isEmpty(commentToPost)) {

            Map commentMap = new HashMap<>();
            commentMap.put("timestamp", ServerValue.TIMESTAMP);
            commentMap.put("by", Uid);
            commentMap.put("text", commentToPost);

            final Map commentNotimap = new HashMap();
            commentNotimap.put("comment_by", Uid);
            commentNotimap.put("post_id", post_id);

            rootRef.child("AllPosts").child(post_id).child("comments").push().setValue(commentMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    rootRef.child("CommentNoti").child(post_by).push().setValue(commentNotimap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(CommentsActivity.this, "Комментарий добавлен", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {

        }


    }

    private void loadcomments(final String post_id) {
        DatabaseReference commentsRef = rootRef.child("AllPosts").child(post_id).child("comments");

        commentsRef.keepSynced(true);
        Query commentQuery = commentsRef.orderByChild("timestamp");
        options = new FirebaseRecyclerOptions.Builder<Comments>().setQuery(commentQuery, Comments.class).build();
        adapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {
                final String By = model.getBy();
                long timestamp = model.getTimestamp();
                String text = model.getText();

                GetTimeAgo gta = new GetTimeAgo();
                final String comment_time = gta.getTimeAgo(timestamp);
                holder.setTime(comment_time);
                holder.setText(text);


                if (By.equals(Uid) || post_by.equals(Uid)) {
                    holder.setDeleteView(true);

                    holder.comment_item_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String comment_id = getRef(position).getKey();
                            rootRef.child("AllPosts").child(post_id).child("comments")
                                    .child(comment_id).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(CommentsActivity.this, "Комментарий удалён", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });

                } else {

                    holder.setDeleteView(false);
                }

                holder.profile_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profIntent = new Intent(CommentsActivity.this, ProfileActivity.class);
                        profIntent.putExtra("userKey", By);
                        startActivity(profIntent);
                    }
                });


                usersRef.child(By).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String username;
                        username = dataSnapshot.child("username").getValue().toString();
                        String userImage = dataSnapshot.child("profileImage").getValue().toString();

                        holder.setUsername(username);
                        holder.setProfileImage(userImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_comment, parent, false);
                return new CommentsViewHolder(view);
                //return null;
            }
        };
        adapter.startListening();
        comment_view.setAdapter(adapter);
    }

}
