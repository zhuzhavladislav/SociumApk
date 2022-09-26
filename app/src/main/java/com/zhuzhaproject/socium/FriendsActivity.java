package com.zhuzhaproject.socium;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SearchView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import com.zhuzhaproject.socium.Utils.Friends;

public class FriendsActivity extends AppCompatActivity {
    Toolbar toolbar;

    FirebaseRecyclerOptions<Friends>options;
    FirebaseRecyclerAdapter<Friends, ViewFriendViewHolder>adapter;

    DatabaseReference friendRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        SearchView searchView = (SearchView) findViewById(R.id.searchView2);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        //changing statusbar color
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        }

        // отключение анимации
        overridePendingTransition(0,0);
        // нижняя навигация
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // выбранный элемент в нижнем меню
        bottomNavigationView.setSelectedItemId(R.id.nav_friends);

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
                        overridePendingTransition(0,0);
                        return false;
                }
                return false;
            }
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
        Query query = friendRef.child(mUser.getUid()).orderByChild("username").startAt(s).endAt(s+"\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query, Friends.class).build();
        adapter = new FirebaseRecyclerAdapter<Friends, ViewFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewFriendViewHolder holder, int position, @NonNull Friends model) {
                Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImageUrl);
                holder.username.setText(model.getUsername());
                holder.profession.setText(model.getProfession());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mUser.getUid().equals(getRef(position).getKey().toString()))
                        {
                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);;
                            startActivity(intent);
                        }else {
                            Intent intent = new Intent(getApplicationContext(), ViewFriendActivity.class);
                            intent.putExtra("userKey", getRef(position).getKey().toString());
                            startActivity(intent);
                        }

                    }
                });

            }

            @NonNull
            @Override
            public ViewFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_friend, parent, false);

                return new ViewFriendViewHolder(view);
                //return null;
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
}