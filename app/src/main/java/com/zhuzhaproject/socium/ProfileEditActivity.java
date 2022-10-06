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
import android.widget.ImageView;
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
    private static final int REQUEST_CODE1 = 101;
    private static final int REQUEST_CODE2 = 102;
    Toolbar toolbar;
    String cover;
    CircleImageView profileImageView;
    ImageView profileCover;
    EditText inputUsername, inputCountry, inputCity, inputProfession, inputStatus;
    Button btnUpdate;
    public String profileImageUrl;
    DatabaseReference mUserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    StorageReference StorageRefProfileImage, StorageRefCoverImage;
    Uri imageProfileUri, imageCoverUri;
    ProgressDialog mLoadingBar;
    private String Uid;
    Integer state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profileImageView = findViewById(R.id.profile_image);
        profileCover = findViewById(R.id.profile_cover);
        inputUsername = findViewById(R.id.inputUsername);
        inputCountry = findViewById(R.id.inputCountry);
        inputCity = findViewById(R.id.inputCity);
        inputProfession = findViewById(R.id.inputProfession);
        inputStatus = findViewById(R.id.inputStatus);
        btnUpdate = findViewById(R.id.btnUpdate);
        mLoadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        Uid = mUser.getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        StorageRefProfileImage = FirebaseStorage.getInstance().getReference().child("ProfileImage");
        StorageRefCoverImage = FirebaseStorage.getInstance().getReference().child("CoverImage");

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
                    if (snapshot.hasChild("cover")) {
                        cover = snapshot.child("cover").getValue().toString();
                        if (!cover.equals("")) {
                            Picasso.get().load(cover).into(profileCover);
                        }
                    }
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
                state=0;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE1);
            }
        });

        profileCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state=1;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE2);
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
        } else if (profession.isEmpty() || profession.length() < 3) {
            showError(inputProfession, "Профессия введена неверно");
        } else {
            mLoadingBar.setTitle("Настройка профиля");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            HashMap hashMap = new HashMap();
            hashMap.put("username", username);
            hashMap.put("city", city);
            hashMap.put("country", country);
            hashMap.put("profession", profession);
            hashMap.put("status", status);
            hashMap.put("device_token", FirebaseInstanceId.getInstance().getToken());
            if (imageProfileUri != null && imageCoverUri == null) {
                StorageRefProfileImage.child(Uid).putFile(imageProfileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            StorageRefProfileImage.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    hashMap.put("profileImage", uri.toString());
                                    mUserRef.child(mUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                        @Override
                                        public void onSuccess(Object o) {
                                            mLoadingBar.dismiss();
                                            Toast.makeText(ProfileEditActivity.this, "Настройка профиля завершена", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext()
                                                    , MainActivity.class));
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
            } else if (imageProfileUri == null && imageCoverUri != null) {
                StorageRefCoverImage.child(Uid).putFile(imageCoverUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            StorageRefCoverImage.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    hashMap.put("cover", uri.toString());
                                    mUserRef.child(mUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                        @Override
                                        public void onSuccess(Object o) {
                                            mLoadingBar.dismiss();
                                            Toast.makeText(ProfileEditActivity.this, "Настройка профиля завершена", Toast.LENGTH_SHORT).show();
                                            finish();
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
            } else if (imageProfileUri != null && imageCoverUri != null) {
                StorageRefProfileImage.child(Uid).putFile(imageProfileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task1) {
                        if (task1.isSuccessful()) {
                            StorageRefProfileImage.child(Uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri1) {
                                    hashMap.put("profileImage", uri1.toString());
                                    StorageRefCoverImage.child(Uid).putFile(imageCoverUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task2) {
                                            if (task2.isSuccessful()) {
                                                StorageRefCoverImage.child(Uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri2) {
                                                        hashMap.put("cover", uri2.toString());
                                                        mUserRef.child(mUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                                            @Override
                                                            public void onSuccess(Object o) {
                                                                mLoadingBar.dismiss();
                                                                Toast.makeText(ProfileEditActivity.this, "Настройка профиля завершена", Toast.LENGTH_SHORT).show();
                                                                finish();
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
                                }
                            });
                        }
                    }
                });
            } else if (profileImageUrl != null && !profileImageUrl.equals("")) {
                mUserRef.child(Uid).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        mLoadingBar.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Настройка профиля завершена", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mLoadingBar.dismiss();
                        Toast.makeText(ProfileEditActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
//                if (imageProfileUri == null && profileImageUrl == null && profileImageUrl.equals(""))
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
        if (requestCode == REQUEST_CODE1 && resultCode == RESULT_OK && state==0 && data != null) {
            imageProfileUri = data.getData();
            profileImageView.setImageURI(imageProfileUri);
        }
        if (requestCode == REQUEST_CODE2 && resultCode == RESULT_OK && state==1 && data != null) {
            imageCoverUri = data.getData();
            profileCover.setImageURI(imageCoverUri);
        }
    }
}
