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
import com.zhuzhaproject.socium.Utils.Chats;

import java.util.Objects;

public class AllChatsActivity extends AppCompatActivity {
    Toolbar toolbar;
    private String Uid;
    FirebaseRecyclerOptions<Chats> options;
    FirebaseRecyclerAdapter<Chats, AllChatsViewHolder> adapter;
    DatabaseReference mUserRef, messageRef, chatRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    RecyclerView recyclerView;
    String userID;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_chats);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        SearchView searchView = findViewById(R.id.searchView2);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        messageRef = FirebaseDatabase.getInstance().getReference().child("Message");
        chatRef = FirebaseDatabase.getInstance().getReference().child("Chat");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        userID = getIntent().getStringExtra("userKey");

        //changing statusBar color
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
        bottomNavigationView.setSelectedItemId(R.id.nav_chat);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                    overridePendingTransition(0, 0);
                    return false;
                case R.id.nav_chat:
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

        LoadUsers();

        searchView.setIconified(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                LoadUsers();
                return false;
            }
        });
    }


    private void LoadUsers() {
        Query query = chatRef.child(Uid).orderByChild("timestamp");
        options = new FirebaseRecyclerOptions.Builder<Chats>().setQuery(query, Chats.class).build();
        adapter = new FirebaseRecyclerAdapter<>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AllChatsViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Chats model) {
                final String chat_user_id = getRef(position).getKey();

                //Last message display
                Query lastQuery;
                assert chat_user_id != null;
                lastQuery = messageRef.child(Uid).child(chat_user_id).orderByKey().limitToLast(1);
                lastQuery.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String lastMessage;
                        String messageBy;
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            lastMessage = Objects.requireNonNull(child.child("message").getValue()).toString();
                            messageBy = Objects.requireNonNull(child.child("userID").getValue()).toString();
                            if (messageBy.equals(Uid)) {
                                holder.lastMessage.setText("Вы: " + lastMessage);
                            } else {
                                holder.lastMessage.setText(lastMessage);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                mUserRef.child(chat_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String username = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                        String profileImageUrl = Objects.requireNonNull(dataSnapshot.child("profileImage").getValue()).toString();
                        Picasso.get().load(profileImageUrl).into(holder.profileImage);
                        holder.username.setText(username);
                        holder.itemView.setOnClickListener(v -> {
                            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                            intent.putExtra("OtherUserID", getRef(position).getKey());
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
            public AllChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_user, parent, false);
                return new AllChatsViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
}