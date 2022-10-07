package com.zhuzhaproject.socium;

import android.annotation.SuppressLint;
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
import java.util.Objects;

public class CommentsActivity extends AppCompatActivity {
    TextView user_name, post_description, post_time;
    ImageView post_image, send_comment, user_image;
    String Uid, post_by;
    EditText input_comment;
    View line;
    RecyclerView comment_view;
    FirebaseAuth mAuth;
    DatabaseReference usersRef, rootRef, userPostsRef, allPostsRef;
    FirebaseRecyclerOptions<Comments> options;
    FirebaseRecyclerAdapter<Comments, CommentsViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        input_comment = findViewById(R.id.input_comment);
        post_time = findViewById(R.id.post_time);
        send_comment = findViewById(R.id.send_comment);
        post_description = findViewById(R.id.post_description);
        post_image = findViewById(R.id.post_image);
        user_name = findViewById(R.id.user_name);
        user_image = findViewById(R.id.user_image);
        line = findViewById(R.id.line);
        comment_view = findViewById(R.id.comment_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        comment_view.setLayoutManager(layoutManager);

        final String post_id = getIntent().getStringExtra("post_id");
        post_by = getIntent().getStringExtra("name");

        userPostsRef = FirebaseDatabase.getInstance().getReference().child("UsersPost");
        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");

        // отключение анимации
        overridePendingTransition(0, 0);

        //changing statusBar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.white));
        window.setNavigationBarColor(this.getResources().getColor(R.color.white));


        allPostsRef.child(post_id).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String timestamp = Objects.requireNonNull(dataSnapshot.child("timestamp").getValue()).toString();
                GetTimeAgo gta = new GetTimeAgo();
                final String postTime = gta.getTimeAgo(Long.parseLong(timestamp));
                String postDesc = Objects.requireNonNull(dataSnapshot.child("postDesc").getValue()).toString();
                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                post_time.setText("Опубликовано " + postTime);
                if (Objects.requireNonNull(dataSnapshot.child("type").getValue()).toString().equals("text")) {
                    post_description.setText(postDesc);
                    line.setVisibility(View.VISIBLE);
                    post_image.setVisibility(View.GONE);
                } else {
                    post_image.setVisibility(View.VISIBLE);
                    line.setVisibility(View.GONE);
                    post_description.setText(postDesc);
                    Picasso.get().load(image).into(post_image);
                }
                if (Objects.requireNonNull(dataSnapshot.child("postDesc").getValue()).toString().equals("")) {
                    post_description.setVisibility(View.GONE);
                } else {
                    post_description.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = rootRef.child("Users");

        mAuth = FirebaseAuth.getInstance();
        Uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();


        usersRef.child(post_by).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                String userImage = Objects.requireNonNull(dataSnapshot.child("profileImage").getValue()).toString();
                Picasso.get().load(userImage).into(user_image);
                user_name.setText(username);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        loadComments(post_id);

        input_comment.addTextChangedListener(commentTextWatcher);
        send_comment.setClickable(false);

        send_comment.setOnClickListener(v -> postComment(post_id));


    }

    private final TextWatcher commentTextWatcher = new TextWatcher() {
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
            Map<Object, Object> commentMap = new HashMap<>();
            commentMap.put("timestamp", ServerValue.TIMESTAMP);
            commentMap.put("by", Uid);
            commentMap.put("text", commentToPost);

            final Map<Object, Object> commentNotiMap = new HashMap<>();
            commentNotiMap.put("comment_by", Uid);
            commentNotiMap.put("post_id", post_id);

            rootRef.child("AllPosts").child(post_id).child("comments").push().setValue(commentMap).addOnSuccessListener(aVoid -> rootRef.child("CommentNoti").child(post_by).push().setValue(commentNotiMap).addOnSuccessListener(aVoid1 -> Toast.makeText(CommentsActivity.this, "Комментарий добавлен", Toast.LENGTH_SHORT).show()));
        }
    }

    private void loadComments(final String post_id) {
        DatabaseReference commentsRef = rootRef.child("AllPosts").child(post_id).child("comments");

        commentsRef.keepSynced(true);
        Query commentQuery = commentsRef.orderByChild("timestamp");
        options = new FirebaseRecyclerOptions.Builder<Comments>().setQuery(commentQuery, Comments.class).build();
        adapter = new FirebaseRecyclerAdapter<>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Comments model) {
                final String By = model.getBy();
                long timestamp = model.getTimestamp();
                String text = model.getText();

                GetTimeAgo gta = new GetTimeAgo();
                final String comment_time = gta.getTimeAgo(timestamp);
                holder.setTime(comment_time);
                holder.setText(text);

                if (By.equals(Uid)) {
                    holder.setDeleteView(true);
                    holder.comment_item_delete.setOnClickListener(v -> {
                        String comment_id = getRef(position).getKey();
                        assert comment_id != null;
                        rootRef.child("AllPosts").child(post_id).child("comments")
                                .child(comment_id).setValue(null).addOnSuccessListener(aVoid -> Toast.makeText(CommentsActivity.this, "Комментарий удалён", Toast.LENGTH_SHORT).show());
                    });

                } else {
                    holder.setDeleteView(false);
                }

                holder.profileImage.setOnClickListener(v -> {
                    Intent profIntent = new Intent(CommentsActivity.this, ProfileActivity.class);
                    profIntent.putExtra("userKey", By);
                    startActivity(profIntent);
                });


                usersRef.child(By).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username;
                        username = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                        String userImage = Objects.requireNonNull(dataSnapshot.child("profileImage").getValue()).toString();

                        holder.setUsername(username);
                        holder.setProfileImage(userImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
