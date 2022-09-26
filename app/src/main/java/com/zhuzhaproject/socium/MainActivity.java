package com.zhuzhaproject.socium;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.zhuzhaproject.socium.Utils.Posts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserRef, postRef, likeRef;
    String profileImageUrlV, usernameV;

    ImageButton addFriends;
    CircleImageView profileImage;
    ImageView logo, editIcon;

    AppCompatButton btnCreatePost;

    private static final int REQUEST_CODE = 101;
    Uri imageUri;
    ProgressDialog mLoadingBar;
    StorageReference postImageRef;
    FirebaseRecyclerAdapter<Posts, MyViewHolder> adapter;
    FirebaseRecyclerOptions<Posts> options;

    RecyclerView recyclerView;
    CardView cardView;
    ShimmerFrameLayout shimmerFrameLayout;

    LinearLayoutManager layoutManager = new LinearLayoutManager(this);

    static int y;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        mLoadingBar = new ProgressDialog(this);

        btnCreatePost = findViewById(R.id.btnCreatePost);
        editIcon = findViewById(R.id.ic_edit);


        recyclerView = findViewById(R.id.recyclerView);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setVisibility(View.INVISIBLE);

        cardView = findViewById(R.id.cardView);
        shimmerFrameLayout = findViewById(R.id.shimmerFrameLayout);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postImageRef = FirebaseStorage.getInstance().getReference().child("PostImages");
        postRef.keepSynced(true);
        likeRef.keepSynced(true);
        mUserRef.keepSynced(true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                y = dy;
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (recyclerView.SCROLL_STATE_SETTLING == newState)
                    if (y > 0) {

                        if (btnCreatePost.getVisibility() != View.GONE) {
                            YoYo.with(Techniques.FadeOutUp)
                                    .duration(300)
                                    .playOn(btnCreatePost);
                            YoYo.with(Techniques.FadeOutUp)
                                    .duration(300)
                                    .playOn(editIcon);
                            YoYo.with(Techniques.FadeOutUp)
                                    .duration(300)
                                    .playOn(cardView);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    btnCreatePost.setVisibility(View.GONE);
                                    editIcon.setVisibility(View.GONE);
                                    cardView.setVisibility(View.GONE);
                                }
                            }, 300); //Time in milisecond
                        }

                    } else if (y < 0) {
                        if (btnCreatePost.getVisibility() != View.VISIBLE) {
                            YoYo.with(Techniques.FadeInDown)
                                    .duration(400)
                                    .playOn(btnCreatePost);
                            YoYo.with(Techniques.FadeInDown)
                                    .duration(400)
                                    .playOn(editIcon);
                            YoYo.with(Techniques.FadeInDown)
                                    .duration(400)
                                    .playOn(cardView);
                            btnCreatePost.setVisibility(View.VISIBLE);
                            editIcon.setVisibility(View.VISIBLE);
                            cardView.setVisibility(View.VISIBLE);
                        }

                    }
            }

        });


        //Toolbar
        addFriends = (ImageButton) findViewById(R.id.toolbar_addFriends);
        profileImage = (CircleImageView) findViewById(R.id.toolbar_profile);
        logo = (ImageView) findViewById(R.id.toolbar_logo);

        //logo on long click listener
        logo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                YoYo.with(Techniques.RotateIn)
                        .duration(400)
                        .playOn(logo);
                return true;
            }
        });

        btnCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext()
                        , CreatePostActivity.class));
            }
        });

        //addfriends on click listener
        addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext()
                        , FindFriendActivity.class));
            }
        });

        //profile image on click listener
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext()
                        , ProfileActivity.class));

            }
        });

        // отключение анимации
        overridePendingTransition(0,0);
        // нижняя навигация
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // выбранный элемент в нижнем меню
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        overridePendingTransition(0,0);
                        return false;
                    case R.id.nav_chat:
                        startActivity(new Intent(getApplicationContext()
                                , ChatUsersActivity.class));
                        overridePendingTransition(0,0);
                        return false;
                    case R.id.nav_friends:
                        startActivity(new Intent(getApplicationContext()
                                , FriendsActivity.class));
                        overridePendingTransition(0,0);
                        return false;
                }
                return false;
            }
        });
        LoadPost();



        //changing statusbar color
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeOut)
                        .duration(100)
                        .playOn(shimmerFrameLayout);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        shimmerFrameLayout.setVisibility(View.GONE);
                    }
                }, 100);
            }
        }, 2000);
    }

    private void LoadPost() {
        options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(postRef.orderByChild("datePost"), Posts.class).build();
        adapter = new FirebaseRecyclerAdapter<Posts, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Posts model) {
                final String postKey = getRef(position).getKey();
                if (model.getPostDesc().equals("")) {
                    holder.postDesc.setVisibility(View.GONE);
                } else {
                    holder.postDesc.setText(model.getPostDesc());
                }
                String timeAgo = calculateTimeAgo(model.getDatePost());
                holder.timeAgo.setText(timeAgo);
                holder.username.setText(model.getUsername());
                Picasso.get().load(model.getUserProfileImageUrl()).into(holder.profileImage);
                if (model.getPostImageUrl().equals("")) {
                    holder.postImage.setVisibility(View.GONE);
                    holder.view3.setVisibility(View.VISIBLE);
                } else {
                    Picasso.get().load(model.getPostImageUrl()).into(holder.postImage);
                }


                holder.countLikes(postKey, mUser.getUid(), likeRef);

                holder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeRef.child(postKey).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    likeRef.child(postKey).child(mUser.getUid()).removeValue();
                                    holder.likeImage.setImageResource(R.drawable.ic_like);
                                    holder.likeImage.setColorFilter(Color.GRAY);
                                    holder.likeCounter.setTextColor(Color.GRAY);

                                    YoYo.with(Techniques.BounceIn)
                                            .duration(300)
                                            .playOn(holder.likeImage);

                                    //notifyDataSetChanged();
                                } else {
                                    likeRef.child(postKey).child(mUser.getUid()).setValue("like");
                                    holder.likeImage.setImageResource(R.drawable.ic_like_pressed);
                                    holder.likeImage.setColorFilter(Color.RED);
                                    holder.likeCounter.setTextColor(Color.RED);

                                    YoYo.with(Techniques.BounceIn)
                                            .duration(300)
                                            .playOn(holder.likeImage);

                                    //notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_post, parent, false);
                return new MyViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private String calculateTimeAgo(String datePost) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-12:00"));
        try {
            long time = sdf.parse(datePost).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            return ago + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mUser == null) {
            SendUserToLoginActivity();
        } else {
            mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        profileImageUrlV = dataSnapshot.child("profileImage").getValue().toString();
                        usernameV = dataSnapshot.child("username").getValue().toString();
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

    private void SendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}