package com.zhuzhaproject.socium;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.zhuzhaproject.socium.Utils.Chat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    public EditText inputSms;
    ImageView btnSend;
    CircleImageView toolbar_profile;
    TextView usernameAppbar, status;
    String OtherUserID, OtherUsername, OtherUserProfileImageLink, OtherUserStatus;

    DatabaseReference mUserRef, smsRef, mRootRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    FirebaseRecyclerOptions<Chat>options;
    FirebaseRecyclerAdapter<Chat, ChatViewHolder>adapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        smsRef= FirebaseDatabase.getInstance().getReference().child("Message");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();

        OtherUserID=getIntent().getStringExtra("OtherUserID");

        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);

        inputSms=findViewById(R.id.inputSms);
        btnSend=findViewById(R.id.btnSend);
        toolbar_profile=findViewById(R.id.toolbar_profile);
        usernameAppbar=findViewById(R.id.usernameAppbar);
        status=findViewById(R.id.textUnder);

        // отключение анимации
        overridePendingTransition(0,0);

        //changing statusbar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.white));
        window.setNavigationBarColor(this.getResources().getColor(R.color.white));

        LoadOtherUser();

        inputSms.addTextChangedListener(smsTextWatcher);
        btnSend.setOnClickListener(v -> SendSMS());
        btnSend.setClickable(false);
        LoadSMS();



    }

    private void LoadSMS() {
        options=new FirebaseRecyclerOptions.Builder<Chat>().setQuery(smsRef.child(mUser.getUid()).child(OtherUserID), Chat.class).build();
        adapter= new FirebaseRecyclerAdapter<>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Chat model) {
                if (model.getUserID().equals(mUser.getUid())) {
                    holder.firstUserText.setVisibility(View.GONE);
                    holder.secondUserText.setVisibility(View.VISIBLE);
                    holder.secondUserText.setText(model.getMessage());
                } else {
                    holder.firstUserText.setVisibility(View.VISIBLE);
                    holder.secondUserText.setVisibility(View.GONE);
                    holder.firstUserText.setText(model.getMessage());
                }
            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.singleview_sms, parent, false);

                return new ChatViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void SendSMS() {
        String sms = inputSms.getText().toString();
        if (sms.isEmpty()){
            Toast.makeText(this, "Напишите Ваше сообщение", Toast.LENGTH_SHORT).show();
        }else{

            Map<String, Object> chatUserMap = new HashMap<>();
            chatUserMap.put("Chat/"+OtherUserID+"/"
                    +mUser.getUid()+"/timestamp",ServerValue.TIMESTAMP);
            chatUserMap.put("Chat/"+mUser.getUid()+"/"+OtherUserID+"/timestamp",ServerValue.TIMESTAMP);

            mRootRef.updateChildren(chatUserMap, (databaseError, databaseReference) -> {
                if(databaseError!=null){
                    Log.d("CHAT_LOG", databaseError.getMessage());
                }
            });
            mRootRef.child("Chat").child(OtherUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(mUser.getUid())){
                        Map<String, Object> chataddmap = new HashMap<>();
                        chataddmap.put("seen","false");
                        chataddmap.put("timestamp",ServerValue.TIMESTAMP);

                        Map<String, Object> chatusermap = new HashMap<>();
                        chatusermap.put("Chat/"+OtherUserID+"/"
                                +mUser.getUid(),chataddmap);

                        mRootRef.updateChildren(chatusermap, (databaseError, databaseReference) -> {
                            if(databaseError!=null){
                                Log.d("CHAT_LOG", databaseError.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            HashMap<String, Object> hashMap= new HashMap<>();
            hashMap.put("message", sms);
            hashMap.put("seen", false);
            hashMap.put("type", "text");
            hashMap.put("timestamp", ServerValue.TIMESTAMP);
            hashMap.put("userID",mUser.getUid());

            //Тут надо глянуть, потому что крашит если есть таймштамп рядом с другим дочерним объектом

            smsRef.child(OtherUserID).child(mUser.getUid()).push().updateChildren(hashMap).addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
                    smsRef.child(mUser.getUid()).child(OtherUserID).push().updateChildren(hashMap).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful())
                        {
                            inputSms.setText(null);
                            //Toast.makeText(ChatActivity.this, "Смс отправлено", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }


    private final TextWatcher smsTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String inputTextSms = inputSms.getText().toString().trim();
            if (inputTextSms.isEmpty()){
                btnSend.setColorFilter(Color.rgb(154, 156, 164));
                btnSend.setClickable(false);
            }else{
                btnSend.setColorFilter(Color.rgb(67, 205, 232));
                btnSend.setClickable(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void LoadOtherUser() {
        mUserRef.child(OtherUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    OtherUsername= Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                    OtherUserProfileImageLink= Objects.requireNonNull(snapshot.child("profileImage").getValue()).toString();
                    OtherUserStatus= Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                    Picasso.get().load(OtherUserProfileImageLink).into(toolbar_profile);
                    usernameAppbar.setText(OtherUsername);
                    status.setText(OtherUserStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}