package com.zhuzhaproject.socium;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.Toast;

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
import com.zhuzhaproject.socium.Utils.Chat;
import com.zhuzhaproject.socium.Utils.Chats;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ChatUsersActivity extends AppCompatActivity {
    Toolbar toolbar;

    private String Uid;
    FirebaseRecyclerOptions<Chats> options;
    FirebaseRecyclerAdapter<Chats, AllChatsViewHolder> adapter;

    DatabaseReference mUserRef, messageRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    RecyclerView recyclerView;
    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users);

        SearchView searchView = (SearchView) findViewById(R.id.searchView2);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        messageRef = FirebaseDatabase.getInstance().getReference().child("Message");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        userID = getIntent().getStringExtra("userKey");

        //changing statusbar color
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        }

        // отключение анимации
        overridePendingTransition(0, 0);
        // нижняя навигация
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // выбранный элемент в нижнем меню
        bottomNavigationView.setSelectedItemId(R.id.nav_chat);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(getApplicationContext()
                                , MainActivity.class));
                        overridePendingTransition(0, 0);
                        return false;
                    case R.id.nav_chat:
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
        Query query = messageRef.child(Uid);
        options = new FirebaseRecyclerOptions.Builder<Chats>().setQuery(query, Chats.class).build();
        adapter = new FirebaseRecyclerAdapter<Chats, AllChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AllChatsViewHolder holder, int position, @NonNull Chats model) {
                final String chat_user_id = getRef(position).getKey();

                //Last message display
                Query lastquery;
                lastquery = messageRef.child(Uid).child(chat_user_id).orderByKey().limitToLast(1);
                lastquery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String lastmessage;
                        String messageBy;
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            lastmessage = child.child("sms").getValue().toString();
                            messageBy = child.child("userID").getValue().toString();
                            if (messageBy.equals(Uid)) {
                                holder.lastMessage.setText("Вы: " + lastmessage);
                            } else {
                                holder.lastMessage.setText(lastmessage);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                mUserRef.child(chat_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("username").getValue().toString();
                        String profileImageUrl = dataSnapshot.child("profileImage").getValue().toString();
                        Picasso.get().load(profileImageUrl).into(holder.profileImage);
                        holder.username.setText(username);
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                intent.putExtra("OtherUserID", getRef(position).getKey().toString());
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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