package com.zhuzhaproject.socium;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import java.util.Objects;

public class FriendsActivity extends AppCompatActivity {
    Toolbar toolbar;

    FirebaseRecyclerOptions<Friends> options;
    FirebaseRecyclerAdapter<Friends, FriendsViewHolder> adapter;

    DatabaseReference friendRef, mUserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    RecyclerView recyclerView;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        SearchView searchView = findViewById(R.id.searchView2);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        //changing statusbar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.white));
        window.setNavigationBarColor(this.getResources().getColor(R.color.white));

        // отключение анимации
        overridePendingTransition(0, 0);
        // нижняя навигация
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // выбранный элемент в нижнем меню
        bottomNavigationView.setSelectedItemId(R.id.nav_friends);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                    overridePendingTransition(0, 0);
                    return false;
                case R.id.nav_chat:
                    Intent intent2 = new Intent(getApplicationContext(), AllChatsActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent2);
                    overridePendingTransition(0, 0);
                    return false;
                case R.id.nav_friends:
                    overridePendingTransition(0, 0);
                    return false;
            }
            return false;
        });

        LoadUsers("");

        searchView.setIconified(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                LoadUsers(s);
                return false;
            }
        });
    }


    private void LoadUsers(String s) {
        Query query = friendRef.child(mUser.getUid()).orderByChild("username").startAt(s).endAt(s + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query, Friends.class).build();
        adapter = new FirebaseRecyclerAdapter<>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Friends model) {
                final String friend_user_id = getRef(position).getKey();
                assert friend_user_id != null;
                mUserRef.child(friend_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String username = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                        String profileImageUrl = Objects.requireNonNull(dataSnapshot.child("profileImage").getValue()).toString();
                        String status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();

                        Picasso.get().load(profileImageUrl).into(holder.profileImageUrl);
                        holder.username.setText(username);
                        holder.status.setText(status);
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_user, parent, false);
                return new FriendsViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
}