package com.zhuzhaproject.socium;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.zhuzhaproject.socium.Utils.Post;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private DatabaseReference postRef, userRef, allPostsRef, friendsRef, usersPostRef;
    private String Uid;

    private ArrayList<String> friends_IdList;
    String profileImageUrlV, usernameV;

    ImageButton addFriends, createPost;
    CircleImageView profileImage;
    ImageView logo;

    ProgressDialog mLoadingBar;
    FirebaseRecyclerAdapter<Post, MainViewHolder> adapter;
    FirebaseRecyclerOptions<Post> options;

    RecyclerView recyclerView;
    ShimmerFrameLayout shimmerFrameLayout;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setVisibility(View.INVISIBLE);
        shimmerFrameLayout = findViewById(R.id.shimmerFrameLayout);

        mLoadingBar = new ProgressDialog(this);

        friends_IdList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        Uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        postRef = FirebaseDatabase.getInstance().getReference().child("PostsToShow");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        usersPostRef = FirebaseDatabase.getInstance().getReference().child("UsersPost").child(Uid);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(Uid);

        allPostsRef.keepSynced(true);
        postRef.child(Uid).keepSynced(true);
        userRef.keepSynced(true);

        // отключение анимации
        overridePendingTransition(0, 0);
        // нижняя навигация
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // выбранный элемент в нижнем меню
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        //changing statusBar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.white));
        window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        //Toolbar
        addFriends = findViewById(R.id.toolbar_addFriends);
        createPost = findViewById(R.id.toolbar_creatPost);
        profileImage = findViewById(R.id.toolbar_profile);
        logo = findViewById(R.id.toolbar_logo);
        //toolbar createPost on click listener
        createPost.setOnClickListener(v -> startActivity(new Intent(getApplicationContext()
                , CreatePostActivity.class)));
        //toolbar addFriends on click listener
        addFriends.setOnClickListener(view -> startActivity(new Intent(getApplicationContext()
                , AllUsersActivity.class)));
        //profile image on click listener
        profileImage.setOnClickListener(view -> {
            Intent profIntent = new Intent(MainActivity.this, ProfileActivity.class);
            profIntent.putExtra("userKey", Uid);
            startActivity(profIntent);
        });
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    friends_IdList.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        friends_IdList.add(Uid);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    overridePendingTransition(0, 0);
                    return false;
                case R.id.nav_chat:
                    Intent intent1 = new Intent(getApplicationContext(), AllChatsActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                    overridePendingTransition(0, 0);
                    return false;
                case R.id.nav_friends:
                    Intent intent2 = new Intent(getApplicationContext(), FriendsActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent2);
                    overridePendingTransition(0, 0);
                    return false;
            }
            return false;
        });
        LoadPost();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUser == null) {
            SendUserToLoginActivity();
        } else {
            userRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        profileImageUrlV = Objects.requireNonNull(dataSnapshot.child("profileImage").getValue()).toString();
                        usernameV = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                        Picasso.get().load(profileImageUrlV).into(profileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Извините! Что-то пошло не так...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void LoadPost() {
        options = new FirebaseRecyclerOptions.Builder<Post>().setQuery(postRef.child(Uid), Post.class).build();
        adapter = new FirebaseRecyclerAdapter<>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MainViewHolder holder, int position, @NonNull Post model) {

                new Handler().postDelayed(() -> {
                    recyclerView.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeOut)
                            .duration(100)
                            .playOn(shimmerFrameLayout);
                    new Handler().postDelayed(() -> shimmerFrameLayout.setVisibility(View.GONE), 100);
                }, 1000);

                final String By = model.getBy();
                long timestamp = model.getTimestamp();
                final String liked = model.getLiked();

                GetTimeAgo gta = new GetTimeAgo();
                final String postTime = gta.getTimeAgo(timestamp);
                holder.setPostTime(postTime);

                final String post_id = getRef(position).getKey();
                assert post_id != null;

                if (By.equals(Uid)) {
                    holder.post_item_delete.setVisibility(View.VISIBLE);
                } else {
                    holder.post_item_delete.setVisibility(View.GONE);
                }

                if (liked.equals("true"))
                    holder.post_like_image.setBackgroundResource(R.drawable.ic_like_pressed);
                else
                    holder.post_like_image.setBackgroundResource(R.drawable.ic_like);


                holder.post_item_delete.setOnClickListener(v -> {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle("Подтверждение").setMessage("Вы точно хотите удалить пост?").setPositiveButton("Да", (dialog, which) -> {

                        for (String id : friends_IdList) {
                            postRef.child(id).child(post_id).setValue(null).addOnSuccessListener(aVoid -> usersPostRef.child(post_id).setValue(null).addOnSuccessListener(aVoid1 -> {

                            }));
                        }

                        allPostsRef.child(post_id).setValue(null).addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Пост удалён", Toast.LENGTH_SHORT).show();
                            mLoadingBar.dismiss();
                        });

                    }).setNegativeButton("Нет", (dialog, which) -> {
                    }).show();
                });

                allPostsRef.child(post_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        int llikes;
                        if (dataSnapshot.hasChild("likes"))
                            llikes = Integer.parseInt(Objects.requireNonNull(dataSnapshot.child("likes").getValue()).toString());
                        else
                            llikes = 0;

                        final int likes = llikes;
                        holder.setLikesCount(likes);
                        holder.post_like_button.setOnClickListener(v -> {
                            if (liked.equals("false")) {
                                postRef.child(Uid).child(post_id).child("liked").setValue("true").addOnSuccessListener(aVoid -> allPostsRef.child(post_id).child("likes").setValue(likes + 1).addOnSuccessListener(aVoid2 -> {
//                                                    Toast.makeText(MainActivity.this, "Liked", Toast.LENGTH_SHORT).show();
                                }));
                            } else {
                                postRef.child(Uid).child(post_id).child("liked").setValue("false").addOnSuccessListener(aVoid -> allPostsRef.child(post_id).child("likes").setValue(likes - 1).addOnSuccessListener(aVoid2 -> {
//                                                    Toast.makeText(MainActivity.this, "Unliked", Toast.LENGTH_SHORT).show();
                                }));
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                allPostsRef.child(post_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        if (Objects.requireNonNull(dataSnapshot.child("type").getValue()).toString().equals("text")) {
                            holder.setPostDescription(Objects.requireNonNull(dataSnapshot.child("postDesc").getValue()).toString());
                            holder.post_image.setVisibility(View.GONE);
                        } else {
                            holder.post_image.setVisibility(View.VISIBLE);
                            holder.setPostDescription(Objects.requireNonNull(dataSnapshot.child("postDesc").getValue()).toString());
                            holder.setPostImage(Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString());
                        }
                        if (Objects.requireNonNull(dataSnapshot.child("postDesc").getValue()).toString().equals("")) {
                            holder.post_description.setVisibility(View.GONE);
                        } else {
                            holder.post_description.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                holder.post_comment_button.setOnClickListener(v -> {
                    Intent commentIntent = new Intent(MainActivity.this, CommentsActivity.class);
                    commentIntent.putExtra("post_id", post_id);
                    commentIntent.putExtra("name", By);
                    commentIntent.putExtra("time", postTime);
                    startActivity(commentIntent);
                });

                holder.user_image.setOnClickListener(v -> {
                    Intent profIntent = new Intent(MainActivity.this, ProfileActivity.class);
                    profIntent.putExtra("userKey", By);
                    startActivity(profIntent);
                });

                allPostsRef.child(post_id).child("comments").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        holder.setCommentsCount(((int) dataSnapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                userRef.child(By).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username;
                        username = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                        String profileImage = Objects.requireNonNull(dataSnapshot.child("profileImage").getValue()).toString();
                        holder.setUserName(username);
                        holder.setUserImage(profileImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @NonNull
            @Override
            public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_post, parent, false);
                return new MainViewHolder(view);
            }

        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }



    private void SendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, AuthLoginActivity.class);
        startActivity(intent);
        finish();
    }


}