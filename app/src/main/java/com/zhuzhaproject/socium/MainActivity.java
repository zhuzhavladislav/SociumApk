package com.zhuzhaproject.socium;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.zhuzhaproject.socium.Utils.Moment;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private DatabaseReference postRef;
    private DatabaseReference userRef;
    private DatabaseReference allPostsRef;
    private DatabaseReference friendsRef;
    private DatabaseReference usersPostRef;
    private String Uid;

    private ArrayList<String> friends_IdList;
    String profileImageUrlV, usernameV;

    ImageButton addFriends;
    CircleImageView profileImage;
    ImageView logo, editIcon;

    AppCompatButton btnCreatePost;

    private static final int REQUEST_CODE = 101;
    Uri imageUri;
    ProgressDialog mLoadingBar;
    FirebaseRecyclerAdapter<Moment, MyViewHolder> adapter;
    FirebaseRecyclerOptions<Moment> options;

    RecyclerView recyclerView;
    CardView cardView;
    ShimmerFrameLayout shimmerFrameLayout;

    static int y;

    @Nullable
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

        cardView = findViewById(R.id.cardView);
        shimmerFrameLayout = findViewById(R.id.shimmerFrameLayout);
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setVisibility(View.INVISIBLE);

        friends_IdList = new ArrayList<String>();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        Uid = mAuth.getCurrentUser().getUid();

        postRef = FirebaseDatabase.getInstance().getReference().child("PostsToShow");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        usersPostRef = FirebaseDatabase.getInstance().getReference().child("UsersPost").child(Uid);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(Uid);

        allPostsRef.keepSynced(true);
        postRef.child(Uid).keepSynced(true);
        userRef.keepSynced(true);

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    friends_IdList.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        friends_IdList.add(Uid);

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
        overridePendingTransition(0, 0);
        // нижняя навигация
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // выбранный элемент в нижнем меню
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        overridePendingTransition(0, 0);
                        return false;
                    case R.id.nav_chat:
                        startActivity(new Intent(getApplicationContext()
                                , ChatUsersActivity.class));
                        overridePendingTransition(0, 0);
                        return false;
                    case R.id.nav_friends:
                        startActivity(new Intent(getApplicationContext()
                                , FriendsActivity.class));
                        overridePendingTransition(0, 0);
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
        options = new FirebaseRecyclerOptions.Builder<Moment>().setQuery(postRef.child(Uid), Moment.class).build();
        adapter = new FirebaseRecyclerAdapter<Moment, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Moment model) {
                final String By = model.getBy();
                long timestamp = model.getTimestamp();
                final String liked = model.getLiked();

                GetTimeAgo gta = new GetTimeAgo();
                final String momentposttime = gta.getTimeAgo(timestamp);
                holder.setTime(momentposttime);

                final String moment_id = getRef(position).getKey();

                if (By.equals(Uid)) {
                    holder.mom_delete.setVisibility(View.VISIBLE);
                } else {
                    holder.mom_delete.setVisibility(View.GONE);
                }

                if (liked.equals("true"))
                    holder.likes_but.setBackgroundResource(R.drawable.ic_like_pressed);
                else
                    holder.likes_but.setBackgroundResource(R.drawable.ic_like);


                holder.mom_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setTitle("Подтверждение").setMessage("Вы точно хотите удалить пост?").setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                for (String id : friends_IdList) {
                                    postRef.child(id).child(moment_id).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            usersPostRef.child(moment_id).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                }
                                            });
                                        }
                                    });
                                }
                                allPostsRef.child(moment_id).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, "Пост удалён", Toast.LENGTH_SHORT).show();
                                        mLoadingBar.dismiss();
                                    }
                                });

                            }
                        }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();

                    }
                });


                allPostsRef.child(moment_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int llikes;
                        if (dataSnapshot.hasChild("likes"))
                            llikes = Integer.valueOf(dataSnapshot.child("likes").getValue().toString());
                        else
                            llikes = 0;

                        final int likes = llikes;
                        holder.setLikes(likes);

                        holder.likeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (liked.equals("false")) {
                                    postRef.child(Uid).child(moment_id).child("liked").setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            allPostsRef.child(moment_id).child("likes").setValue(likes + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
//                                                    Toast.makeText(MainActivity.this, "Liked", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    postRef.child(Uid).child(moment_id).child("liked").setValue("false").addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            allPostsRef.child(moment_id).child("likes").setValue(likes - 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
//                                                    Toast.makeText(MainActivity.this, "Unliked", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });

                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                allPostsRef.child(moment_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("type").getValue().toString().equals("text")) {
                            holder.setText(dataSnapshot.child("postDesc").getValue().toString());
                            holder.moment_image.setVisibility(View.GONE);
                        } else {
                            holder.moment_image.setVisibility(View.VISIBLE);
                            holder.setText(dataSnapshot.child("postDesc").getValue().toString());
                            holder.setImage(dataSnapshot.child("image").getValue().toString());
                        }
                        if (dataSnapshot.child("postDesc").getValue().toString().equals("")) {
                            holder.mom_text_item.setVisibility(View.GONE);
                        } else {
                            holder.mom_text_item.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


//                holder.commentsButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent commentIntent = new Intent(MainActivity.this,CommentsActivity.class);
//                        commentIntent.putExtra("moment_id",moment_id);
//                        commentIntent.putExtra("name",By);
//                        commentIntent.putExtra("time",momentposttime);
//                        startActivity(commentIntent);
//                    }
//                });

                holder.userDp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (By.equals(Uid)) {
                            Intent profIntent = new Intent(MainActivity.this, ProfileActivity.class);
                            profIntent.putExtra("userKey", By);
                            startActivity(profIntent);
                        } else {
                            Intent profIntent = new Intent(MainActivity.this, ViewOtherProfileActivity.class);
                            profIntent.putExtra("userKey", By);
                            startActivity(profIntent);
                        }

                    }
                });


                allPostsRef.child(moment_id).child("comments").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        holder.setCommentsCount(((int) dataSnapshot.getChildrenCount()));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                userRef.child(By).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String username;
                        username = dataSnapshot.child("username").getValue().toString();
                        String profileImage = dataSnapshot.child("profileImage").getValue().toString();

                        holder.setName(username);
                        holder.setDp(profileImage, MainActivity.this);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_post2, parent, false);
                return new MyViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
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
            userRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
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