package com.zhuzhaproject.socium;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewOtherProfileActivity extends AppCompatActivity {
    Toolbar toolbar;
    CircleImageView profileImageView;
    TextView outputUsername, outputLocation, outputProfession, outputStatus, friendsCounter;
    String profileImageUrl, username, city, country, profession, status;
    Button btnMsg, btnPerform, btnDecline;

    String CurrentState = "nothing_happen";
    String Uid;

    FirebaseRecyclerOptions<Friends> options;
    FirebaseRecyclerAdapter<Friends, ViewOtherProfileViewHolder> adapter;

    DatabaseReference mUserRef, mUserRef2, requestRef, friendRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    RecyclerView recyclerView;
    CardView cardView;
    ShimmerFrameLayout shimmerFrameLayout;

    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_view_otherprofile);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profileImageView = findViewById(R.id.profile_image);
        outputUsername = findViewById(R.id.outputUsername);
        outputLocation = findViewById(R.id.outputLocation);
        outputProfession = findViewById(R.id.outputProfession);
        outputStatus = findViewById(R.id.outputStatus);
        friendsCounter = findViewById(R.id.friendCounter2);

        userID = getIntent().getStringExtra("userKey");

        btnMsg = findViewById(R.id.btnMsg);
        btnPerform = findViewById(R.id.btnPerform);
        btnDecline = findViewById(R.id.btnDecline);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        cardView = findViewById(R.id.CardView);
        cardView.setVisibility(View.INVISIBLE);
        shimmerFrameLayout = findViewById(R.id.shimmerFrameLayout);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID); // mDatabase
        mUserRef2 = FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Friendreq"); // mFriendreqDatabase
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends"); // mFriendDatabase

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
                        startActivity(new Intent(getApplicationContext()
                                , MainActivity.class));
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

        LoadUser();
        LoadUsers("");


        //changing statusbar color
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        }

        btnMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(ViewFriendActivity.this, "В разработке", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewOtherProfileActivity.this, ChatActivity.class);
                intent.putExtra("OtherUserID",userID);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });
        btnPerform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PerformAction(userID);
            }
        });
        CheckUserExistance(userID);
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Unfriend(userID);
            }
        });

        friendRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    int totalFriends = (int) snapshot.getChildrenCount();
                    friendsCounter.setText(totalFriends+"");
                }else{
                    friendsCounter.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cardView.setVisibility(View.VISIBLE);
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
        }, 1300);

    }

    private void Unfriend(String userID) {
        if (CurrentState.equals("friend")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Удалить из друзей")
                    .setCancelable(false)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            friendRef.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        friendRef.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ViewOtherProfileActivity.this, "Вы удалили друга", Toast.LENGTH_SHORT).show();
                                                    CurrentState = "nothing_happen";
                                                    btnPerform.setText("Добавить в друзья");
                                                    btnPerform.setVisibility(View.VISIBLE);
                                                    btnPerform.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_blue));
                                                    btnPerform.setTextColor(getResources().getColor(R.color.white));
                                                    btnDecline.setVisibility(View.GONE);
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    })

                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
    }

    private void CheckUserExistance(String userID) {

        friendRef.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CurrentState = "friend";
                    btnPerform.setVisibility(View.GONE);
                    btnDecline.setText("У вас в друзьях");
                    btnPerform.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_grey));
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
                    btnPerform.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_grey));
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
                    if (snapshot.child("status").getValue().toString().equals("pending")) {
                        CurrentState = "I_sent_pending";
                        btnPerform.setText("Отменить заявку");
                        btnPerform.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_grey));
                        btnPerform.setTextColor(getResources().getColor(R.color.colorAccent2));
                        btnDecline.setVisibility(View.GONE);
                    }
                    if (snapshot.child("status").getValue().toString().equals("decline")) {
                        CurrentState = "I_sent_decline";
                        btnPerform.setText("Отменить заявку");
                        btnPerform.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_grey));
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
                    if (snapshot.child("status").getValue().toString().equals("pending")) {
                        CurrentState = "he_sent_pending";
                        btnPerform.setText("Ответить на заявку");
                        btnPerform.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_grey));
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
            btnPerform.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_blue));
            btnPerform.setTextColor(getResources().getColor(R.color.white));
            btnDecline.setVisibility(View.GONE);
        }
    }

    private void PerformAction(String userID) {
        if (CurrentState.equals("nothing_happen")) {
            HashMap hashMap = new HashMap();
            hashMap.put("status", "pending");

            requestRef.child(mUser.getUid()).child(userID).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ViewOtherProfileActivity.this, "Заявка отправлена", Toast.LENGTH_SHORT).show();
                        CurrentState = "I_sent_pending";
                        btnPerform.setText("Отменить заявку");
                        btnPerform.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_grey));
                        btnPerform.setTextColor(getResources().getColor(R.color.colorAccent2));
                        btnDecline.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(ViewOtherProfileActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if (CurrentState.equals("I_sent_pending") || CurrentState.equals("I_sent_decline")) {
            requestRef.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ViewOtherProfileActivity.this, "Заявка отменена", Toast.LENGTH_SHORT).show();
                        CurrentState = "nothing_happen";
                        btnPerform.setText("Добавить в друзья");
                        btnPerform.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_blue));
                        btnPerform.setTextColor(getResources().getColor(R.color.white));
                        btnDecline.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(ViewOtherProfileActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if (CurrentState.equals("he_sent_pending")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Ответить на заявку")
                    .setCancelable(true)
                    .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                            HashMap hashMap2 = new HashMap();
                            hashMap2.put("status", "friend");
                            hashMap2.put("username", username);
                            hashMap2.put("profileImageUrl", profileImageUrl);
                            hashMap2.put("profession", profession);

                            mUserRef2.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    String username = snapshot.child("username").getValue().toString();
                                    String profileImageUrl = snapshot.child("profileImage").getValue().toString();
                                    String profession = snapshot.child("profession").getValue().toString();

                                    requestRef.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap hashMap = new HashMap();
                                                hashMap.put("status", "friend");
                                                hashMap.put("username", username);
                                                hashMap.put("profileImageUrl", profileImageUrl);
                                                hashMap.put("profession", profession);

                                                friendRef.child(mUser.getUid()).child(userID).updateChildren(hashMap2).addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        if (task.isSuccessful()) {
                                                            friendRef.child(userID).child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                                                @Override
                                                                public void onComplete(@NonNull Task task) {
                                                                    Toast.makeText(ViewOtherProfileActivity.this, "Вы добавили друга", Toast.LENGTH_SHORT).show();
                                                                    CurrentState = "friend";
                                                                    btnPerform.setVisibility(View.GONE);
                                                                    btnDecline.setText("У вас в друзьях");
                                                                    btnDecline.setVisibility(View.VISIBLE);
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    })
                    .setNegativeButton("Отклонить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestRef.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ViewOtherProfileActivity.this, "Вы отменили запрос дружбы", Toast.LENGTH_SHORT).show();
                                        CurrentState = "he_sent_decline";
                                        btnPerform.setVisibility(View.GONE);
                                        btnDecline.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        if (CurrentState.equals("friend")) {
            //
        }
    }

    private void LoadUser() {



        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    username = snapshot.child("username").getValue().toString();
                    city = snapshot.child("city").getValue().toString();
                    country = snapshot.child("country").getValue().toString();
                    profession = snapshot.child("profession").getValue().toString();
                    status = snapshot.child("status").getValue().toString();

                    Picasso.get().load(profileImageUrl).into(profileImageView);
                    outputLocation.setText("Место проживания: " + country + ", " + city);
                    outputProfession.setText("Профессия: " + profession);
                    outputUsername.setText(username);
                    outputStatus.setText(status);
                } else {
                    Toast.makeText(ViewOtherProfileActivity.this, "Пользователь не существует", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewOtherProfileActivity.this, "" + error.getMessage().toString(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void LoadUsers(String s) {
        Query query = friendRef.child(userID).orderByChild("username");
        options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query, Friends.class).build();
        adapter = new FirebaseRecyclerAdapter<Friends, ViewOtherProfileViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewOtherProfileViewHolder holder, int position, @NonNull Friends model) {
                Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImageUrl);
                holder.username.setText(model.getUsername());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mUser.getUid().equals(getRef(position).getKey().toString()))
                        {
                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);;
                            startActivity(intent);
                        }else {
                            Intent intent = new Intent(getApplicationContext(), ViewOtherProfileActivity.class);
                            intent.putExtra("userKey", getRef(position).getKey().toString());
                            startActivity(intent);
                        }

                    }
                });
            }


            @NonNull
            @Override
            public ViewOtherProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_friend, parent, false);

                return new ViewOtherProfileViewHolder(view);
                //return null;
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
}