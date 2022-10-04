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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEditActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 101;
    Toolbar toolbar;
    CircleImageView profileImageView;
    EditText inputUsername, inputCountry, inputCity, inputProfession, inputStatus;
    Button btnUpdate;
    public String profileImageUrl;
    DatabaseReference mUserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    StorageReference StorageRef;
    Uri imageUri;
    ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profileImageView = findViewById(R.id.profile_image);
        inputUsername = findViewById(R.id.inputUsername);
        inputCountry = findViewById(R.id.inputCountry);
        inputCity = findViewById(R.id.inputCity);
        inputProfession = findViewById(R.id.inputProfession);
        inputStatus = findViewById(R.id.inputStatus);
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
                    profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    String city = snapshot.child("city").getValue().toString();
                    String country = snapshot.child("country").getValue().toString();
                    String profession = snapshot.child("profession").getValue().toString();
                    String username = snapshot.child("username").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();

                    Picasso.get().load(profileImageUrl).into(profileImageView);
                    inputCity.setText(city);
                    inputCountry.setText(country);
                    inputProfession.setText(profession);
                    inputUsername.setText(username);
                    inputStatus.setText(status);
                } else {
                    btnUpdate.setText("Сохранить");
                    //Toast.makeText(ProfileEditActivity.this, "Профиль не существует", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileEditActivity.this, "" + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateData();
            }
        });
    }

    private void UpdateData() {

        String username = inputUsername.getText().toString();
        String city = inputCity.getText().toString();
        String country = inputCountry.getText().toString();
        String profession = inputProfession.getText().toString();
        String status = inputStatus.getText().toString();

        if (username.isEmpty() || username.length() < 3) {
            showError(inputUsername, "Имя и фамилия введены неверно");
        } else if (city.isEmpty() || city.length() < 3) {
            showError(inputCity, "Город введен неверно");
        } else if (country.isEmpty() || country.length() < 3) {
            showError(inputCountry, "Страна введена неверно");
        } else if(profession.isEmpty() || profession.length()<3) {
            showError(inputProfession, "Профессия введена неверно");
        }else{
            mLoadingBar.setTitle("Настройка профиля");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            if(imageUri!=null){
                StorageRef.child(mUser.getUid()).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            StorageRef.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    HashMap hashMap=new HashMap();
                                    hashMap.put("username", username);
                                    hashMap.put("city", city);
                                    hashMap.put("country", country);
                                    hashMap.put("profession", profession);
                                    hashMap.put("status", status);
                                    hashMap.put("profileImage", uri.toString());
                                    hashMap.put("device_token", FirebaseInstanceId.getInstance().getToken());


                                    mUserRef.child(mUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                        @Override
                                        public void onSuccess(Object o) {
                                            mLoadingBar.dismiss();
                                            Toast.makeText(ProfileEditActivity.this, "Настройка профиля завершена", Toast.LENGTH_SHORT).show();
//                                            Intent intent=new Intent(ProfileEditActivity.this, MainActivity.class);
//                                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                                            startActivity(intent);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mLoadingBar.dismiss();
                                            Toast.makeText(ProfileEditActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            } else if (imageUri==null && profileImageUrl!=null && profileImageUrl!="") {
                HashMap hashMap=new HashMap();
                hashMap.put("username", username);
                hashMap.put("city", city);
                hashMap.put("country", country);
                hashMap.put("profession", profession);
                hashMap.put("status", status);
                mUserRef.child(mUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        mLoadingBar.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Настройка профиля завершена", Toast.LENGTH_SHORT).show();
//                        Intent intent=new Intent(ProfileEditActivity.this, MainActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mLoadingBar.dismiss();
                        Toast.makeText(ProfileEditActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                mLoadingBar.dismiss();
                Toast.makeText(ProfileEditActivity.this, "При создании профиля необходимо добавить изображение", Toast.LENGTH_SHORT).show();
            }

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
