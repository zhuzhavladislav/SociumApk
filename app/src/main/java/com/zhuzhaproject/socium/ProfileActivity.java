package com.zhuzhaproject.socium;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    Toolbar toolbar;
    CircleImageView profileImageView;
    TextView outputUsername, outputCountry, outputCity, outputProfession, outputStatus;
    Button btnEdit;

    DatabaseReference mUserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar=findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profileImageView=findViewById(R.id.profile_image);
        outputUsername=findViewById(R.id.outputUsername);
        outputCountry=findViewById(R.id.outputCountry);
        outputCity=findViewById(R.id.outputCity);
        outputProfession=findViewById(R.id.outputProfession);
        outputStatus=findViewById(R.id.outputStatus);
        btnEdit=findViewById(R.id.btnEdit);

        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users");

        //changing statusbar color
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }

        mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    String profileImageUrl=snapshot.child("profileImage").getValue().toString();
                    String city=snapshot.child("city").getValue().toString();
                    String country=snapshot.child("country").getValue().toString();
                    String profession=snapshot.child("profession").getValue().toString();
                    String username=snapshot.child("username").getValue().toString();
                    String status=snapshot.child("status").getValue().toString();

                    Picasso.get().load(profileImageUrl).into(profileImageView);
                    outputCity.setText("Город: "+city);
                    outputCountry.setText("Страна: "+country);
                    outputProfession.setText("Профессия: "+profession);
                    outputUsername.setText(username);
                    outputStatus.setText(status);
                }else{
                    Toast.makeText(ProfileActivity.this, "Профиль не существует", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, ""+error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext()
                        ,ProfileActivity2.class));
            }
        });
    }
}