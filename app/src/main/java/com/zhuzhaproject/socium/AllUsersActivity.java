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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import com.zhuzhaproject.socium.Utils.Users;

import java.util.Objects;

public class AllUsersActivity extends AppCompatActivity {
    Toolbar toolbar;

    FirebaseRecyclerOptions<Users>options;
    FirebaseRecyclerAdapter<Users, AllUsersViewHolder>adapter;

    DatabaseReference mUserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    RecyclerView recyclerView;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        SearchView searchView = findViewById(R.id.searchView2);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
        overridePendingTransition(0,0);
        // нижняя навигация
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // выбранный элемент в нижнем меню
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    startActivity(new Intent(getApplicationContext()
                            , MainActivity.class));
                    overridePendingTransition(0,0);
                    return false;
                case R.id.nav_chat:
                    startActivity(new Intent(getApplicationContext()
                            , AllChatsActivity.class));
                    overridePendingTransition(0,0);
                    return false;
                case R.id.nav_friends:
                    startActivity(new Intent(getApplicationContext()
                            , FriendsActivity.class));
                    overridePendingTransition(0,0);
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
        Query query= mUserRef.orderByChild("username").startAt(s).endAt(s+"\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(query, Users.class).build();
        adapter = new FirebaseRecyclerAdapter<>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AllUsersViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Users model) {
                if (mUser.getUid().equals(getRef(position).getKey())) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));

                } else {
                    Picasso.get().load(model.getProfileImage()).into(holder.profileImage);
                    holder.username.setText(model.getUsername());
                    holder.profession.setText(model.getProfession());
                }
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    intent.putExtra("userKey", getRef(position).getKey());
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public AllUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_user, parent, false);

                return new AllUsersViewHolder(view);
                //return null;
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }



}