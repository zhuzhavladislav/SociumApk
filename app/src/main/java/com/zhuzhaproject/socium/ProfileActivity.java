package com.zhuzhaproject.socium;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.zhuzhaproject.socium.Utils.Friends;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    Toolbar toolbar;
    CircleImageView profileImageView;
    ImageView profile_cover;
    TextView outputUsername, outputLocation, outputProfession, outputStatus, friendsCounter;
    String profileImageUrl, username, city, country, profession, status, cover;
    Button btnEdit, btnMsg, btnPerform, btnDecline;

    String CurrentState = "nothing_happen";
    String Uid;

    FirebaseRecyclerOptions<Friends> options;
    FirebaseRecyclerAdapter<Friends, FriendsViewHolder> adapter;

    DatabaseReference mUserRef, mUserRef2, requestRef, friendRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    RecyclerView recyclerView;
    ShimmerFrameLayout shimmerFrameLayout;

    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_profile);
        Uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        profileImageView = findViewById(R.id.profile_image);
        outputUsername = findViewById(R.id.outputUsername);
        outputLocation = findViewById(R.id.outputLocation);
        outputProfession = findViewById(R.id.outputProfession);
        outputStatus = findViewById(R.id.outputStatus);
        friendsCounter = findViewById(R.id.friendCounter);
        profile_cover = findViewById(R.id.profile_cover);

        userID = getIntent().getStringExtra("userKey");

        btnMsg = findViewById(R.id.btnMsg);
        btnPerform = findViewById(R.id.btnPerform);
        btnDecline = findViewById(R.id.btnDecline);
        btnEdit = findViewById(R.id.btnEdit);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        shimmerFrameLayout = findViewById(R.id.shimmerFrameLayout);
        shimmerFrameLayout.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID); // mDatabase
        mUserRef2 = FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Friendreq"); // mFriendreqDatabase
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends"); // mFriendDatabase

        // отключение анимации
        overridePendingTransition(0, 0);

        CheckUserExistence(userID);
        LoadUser();
        LoadUsers();


        //changing statusbar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.white));
        window.setNavigationBarColor(this.getResources().getColor(R.color.white));

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProfileEditOrSetupActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        btnMsg.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
            intent.putExtra("OtherUserID", userID);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        btnPerform.setOnClickListener(v -> PerformAction(userID));

        btnDecline.setOnClickListener(v -> Unfriend(userID));

        friendRef.child(userID).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalFriends = (int) snapshot.getChildrenCount();
                    friendsCounter.setText(totalFriends + "");
                } else {
                    friendsCounter.setText("0");
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void Unfriend(String userID) {
        if (CurrentState.equals("friend")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Удалить из друзей")
                    .setCancelable(false)
                    .setPositiveButton("Да", (dialog, which) -> {
                        dialog.cancel();
                        friendRef.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                friendRef.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(ProfileActivity.this, "Вы удалили друга", Toast.LENGTH_SHORT).show();
                                        CurrentState = "nothing_happen";
                                        btnPerform.setText("Добавить в друзья");
                                        btnPerform.setVisibility(View.VISIBLE);
                                        btnPerform.setBackgroundResource(R.drawable.btn_blue);
                                        btnPerform.setTextColor(getResources().getColor(R.color.white));
                                        btnDecline.setVisibility(View.GONE);
                                    }
                                });
                            }
                        });
                    })

                    .setNegativeButton("Нет", (dialog, which) -> {

                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
    }

    private void CheckUserExistence(String userID) {
        if (Uid.equals(userID)) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDecline.setVisibility(View.GONE);
            btnMsg.setVisibility(View.GONE);
            btnPerform.setVisibility(View.GONE);
        } else {
            btnEdit.setVisibility(View.GONE);
            btnDecline.setVisibility(View.VISIBLE);
            btnMsg.setVisibility(View.VISIBLE);
            btnPerform.setVisibility(View.VISIBLE);
            friendRef.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        CurrentState = "friend";
                        btnPerform.setVisibility(View.GONE);
                        btnDecline.setText("У вас в друзьях");
                        btnPerform.setBackgroundResource(R.drawable.btn_grey);
                        btnPerform.setTextColor(getResources().getColor(R.color.colorAccent2));
                        btnDecline.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            friendRef.child(userID).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        CurrentState = "friend";
                        btnPerform.setVisibility(View.GONE);
                        btnDecline.setText("У вас в друзьях");
                        btnPerform.setBackgroundResource(R.drawable.btn_grey);
                        btnPerform.setTextColor(getResources().getColor(R.color.colorAccent2));
                        btnDecline.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            requestRef.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("pending")) {
                            CurrentState = "I_sent_pending";
                            btnPerform.setText("Отменить заявку");
                            btnPerform.setBackgroundResource(R.drawable.btn_grey);
                            btnPerform.setTextColor(getResources().getColor(R.color.colorAccent2));
                            btnDecline.setVisibility(View.GONE);
                        }
                        if (Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("decline")) {
                            CurrentState = "I_sent_decline";
                            btnPerform.setText("Отменить заявку");
                            btnPerform.setBackgroundResource(R.drawable.btn_grey);
                            btnPerform.setTextColor(getResources().getColor(R.color.colorAccent2));
                            btnDecline.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            requestRef.child(userID).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("pending")) {
                            CurrentState = "he_sent_pending";
                            btnPerform.setText("Ответить на заявку");
                            btnPerform.setBackgroundResource(R.drawable.btn_grey);
                            btnPerform.setTextColor(getResources().getColor(R.color.colorAccent2));
                            btnDecline.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            if (CurrentState.equals("nothing_happen")) {
                CurrentState = "nothing_happen";
                btnPerform.setText("Добавить в друзья");
                btnPerform.setBackgroundResource(R.drawable.btn_blue);
                btnPerform.setTextColor(getResources().getColor(R.color.white));
                btnDecline.setVisibility(View.GONE);
            }
        }

    }

    private void PerformAction(String userID) {
        if (CurrentState.equals("nothing_happen")) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", "pending");

            requestRef.child(mUser.getUid()).child(userID).updateChildren(hashMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Заявка отправлена", Toast.LENGTH_SHORT).show();
                    CurrentState = "I_sent_pending";
                    btnPerform.setText("Отменить заявку");
                    btnPerform.setBackgroundResource(R.drawable.btn_grey);
                    btnPerform.setTextColor(getResources().getColor(R.color.colorAccent2));
                    btnDecline.setVisibility(View.GONE);
                } else {
                    Toast.makeText(ProfileActivity.this, "" + Objects.requireNonNull(task.getException()), Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (CurrentState.equals("I_sent_pending") || CurrentState.equals("I_sent_decline")) {
            requestRef.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Заявка отменена", Toast.LENGTH_SHORT).show();
                    CurrentState = "nothing_happen";
                    btnPerform.setText("Добавить в друзья");
                    btnPerform.setBackgroundResource(R.drawable.btn_blue);
                    btnPerform.setTextColor(getResources().getColor(R.color.white));
                    btnDecline.setVisibility(View.GONE);
                } else {
                    Toast.makeText(ProfileActivity.this, "" + Objects.requireNonNull(task.getException()), Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (CurrentState.equals("he_sent_pending")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Ответить на заявку")
                    .setCancelable(true)
                    .setPositiveButton("Добавить", (dialog, which) -> {
                        dialog.cancel();

                        HashMap<String, Object> hashMap2 = new HashMap<>();
                        hashMap2.put("status", "friend");
                        hashMap2.put("username", username);
                        hashMap2.put("profileImageUrl", profileImageUrl);
                        hashMap2.put("profession", profession);

                        mUserRef2.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                String username = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                                String profileImageUrl = Objects.requireNonNull(snapshot.child("profileImage").getValue()).toString();
                                String profession = Objects.requireNonNull(snapshot.child("profession").getValue()).toString();

                                requestRef.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("status", "friend");
                                        hashMap.put("username", username);
                                        hashMap.put("profileImageUrl", profileImageUrl);
                                        hashMap.put("profession", profession);

                                        friendRef.child(mUser.getUid()).child(userID).updateChildren(hashMap2).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                friendRef.child(userID).child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(task2 -> {
                                                    Toast.makeText(ProfileActivity.this, "Вы добавили друга", Toast.LENGTH_SHORT).show();
                                                    CurrentState = "friend";
                                                    btnPerform.setVisibility(View.GONE);
                                                    btnDecline.setText("У вас в друзьях");
                                                    btnDecline.setVisibility(View.VISIBLE);
                                                });
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    })
                    .setNegativeButton("Отклонить", (dialog, which) -> requestRef.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Вы отменили запрос дружбы", Toast.LENGTH_SHORT).show();
                            CurrentState = "he_sent_decline";
                            btnPerform.setVisibility(View.GONE);
                            btnDecline.setVisibility(View.GONE);

                        }
                    }));
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void LoadUser() {
        mUserRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profileImageUrl = Objects.requireNonNull(snapshot.child("profileImage").getValue()).toString();
                    username = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                    city = Objects.requireNonNull(snapshot.child("city").getValue()).toString();
                    country = Objects.requireNonNull(snapshot.child("country").getValue()).toString();
                    profession = Objects.requireNonNull(snapshot.child("profession").getValue()).toString();


                    Picasso.get().load(profileImageUrl).into(profileImageView);
                    outputLocation.setText("Место проживания: " + country + ", " + city);
                    outputProfession.setText("Профессия: " + profession);
                    outputUsername.setText(username);

                    if (snapshot.hasChild("status")){
                        status = Objects.requireNonNull(snapshot.child("status").getValue()).toString();
                        if (status.equals("")){
                            outputStatus.setVisibility(View.GONE);
                        } else {
                            outputStatus.setText(status);
                        }
                    }
                    if (snapshot.hasChild("cover")) {
                        cover = Objects.requireNonNull(snapshot.child("cover").getValue()).toString();
                        if (!cover.equals("")){
                            Picasso.get().load(cover).into(profile_cover);
                        } else {
                            Picasso.get().load(profileImageUrl).into(profile_cover);
                            profile_cover.setRenderEffect(RenderEffect.createBlurEffect(60, 60, Shader.TileMode.MIRROR));
                        }
                    } else {
                        Picasso.get().load(profileImageUrl).into(profile_cover);
                        profile_cover.setRenderEffect(RenderEffect.createBlurEffect(60, 60, Shader.TileMode.MIRROR));
                    }



                } else {
                    Toast.makeText(ProfileActivity.this, "Пользователь не существует", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, ProfileEditOrSetupActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void LoadUsers() {
        Query query = friendRef.child(userID).orderByChild("username");
        options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query, Friends.class).build();
        adapter = new FirebaseRecyclerAdapter<>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Friends model) {
                final String friend_user_id = getRef(position).getKey();
                assert friend_user_id != null;
                mUserRef2.child(friend_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String username = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                        String profileImageUrl = Objects.requireNonNull(dataSnapshot.child("profileImage").getValue()).toString();

                        Picasso.get().load(profileImageUrl).into(holder.profileImageUrl);
                        holder.username.setText(username);

                        holder.itemView.setOnClickListener(v -> {
                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                            intent.putExtra("userKey", getRef(position).getKey());
                            startActivity(intent);
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_friend, parent, false);
                return new FriendsViewHolder(view);
                //return null;
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
}