package com.zhuzhaproject.socium;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity2 extends AppCompatActivity {
    private static final int REQUEST_CODE = 101;
    Toolbar toolbar;
    CircleImageView profileImageView;
    EditText inputUsername, inputCountry, inputCity, inputProfession;
    Button btnUpdate;

    DatabaseReference mUserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    StorageReference StorageRef;
    Uri imageUri;
    ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile2);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profileImageView = findViewById(R.id.profile_image);
        inputUsername = findViewById(R.id.inputUsername);
        inputCountry = findViewById(R.id.inputCountry);
        inputCity = findViewById(R.id.inputCity);
        inputProfession = findViewById(R.id.inputProfession);
        btnUpdate = findViewById(R.id.btnUpdate);
        mLoadingBar=new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        StorageRef = FirebaseStorage.getInstance().getReference().child("ProfileImage");

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
                if (snapshot.exists()) {
                    String profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    String city = snapshot.child("city").getValue().toString();
                    String country = snapshot.child("country").getValue().toString();
                    String profession = snapshot.child("profession").getValue().toString();
                    String username = snapshot.child("username").getValue().toString();

                    Picasso.get().load(profileImageUrl).into(profileImageView);
                    inputCity.setText(city);
                    inputCountry.setText(country);
                    inputProfession.setText(profession);
                    inputUsername.setText(username);
                } else {
                    Toast.makeText(ProfileActivity2.this, "Профиль не существует", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity2.this, "" + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });


        //Фотку менять пока что нельзя, меняется url, соответственно в постах пропадает старая фотка
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity2.this, "На данный момент фотографию сменить нельзя", Toast.LENGTH_SHORT).show();
        //        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //        intent.setType("image/*");
        //        startActivityForResult(intent, REQUEST_CODE);
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateData();
                startActivity(new Intent(getApplicationContext()
                        ,ProfileActivity.class));
            }
        });
    }

    private void UpdateData() {

        String username = inputUsername.getText().toString();
        String city = inputCity.getText().toString();
        String country = inputCountry.getText().toString();
        String profession = inputProfession.getText().toString();

        if (username.isEmpty() || username.length() < 3) {
            showError(inputUsername, "Имя и фамилия введены неверно");
        } else if (city.isEmpty() || city.length() < 3) {
            showError(inputCity, "Город введен неверно");
        } else if (country.isEmpty() || country.length() < 3) {
            showError(inputCountry, "Страна введена неверно");
        } else if(profession.isEmpty() || profession.length()<3) {
            showError(inputProfession, "Профессия введена неверно");
        }else{

            mUserRef.child(mUser.getUid()).child("username").setValue(username);
            mUserRef.child(mUser.getUid()).child("city").setValue(city);
            mUserRef.child(mUser.getUid()).child("country").setValue(country);
            mUserRef.child(mUser.getUid()).child("profession").setValue(profession);

            Toast.makeText(ProfileActivity2.this, "Настройка профиля завершена", Toast.LENGTH_SHORT).show();
        }

    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }
}
