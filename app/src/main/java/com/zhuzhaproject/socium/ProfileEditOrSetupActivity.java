package com.zhuzhaproject.socium;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEditOrSetupActivity extends AppCompatActivity {
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
    Uri imageProfileUri, imageCoverUri, imageProfileUriResultCrop, imageCoverUriResultCrop;
    ProgressDialog mLoadingBar;
    private String Uid;
    Integer state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

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
        assert mUser != null;
        Uid = mUser.getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        StorageRefProfileImage = FirebaseStorage.getInstance().getReference().child("ProfileImage");
        StorageRefCoverImage = FirebaseStorage.getInstance().getReference().child("CoverImage");

        //changing statusbar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.white));

        mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("cover")) {
                        cover = Objects.requireNonNull(snapshot.child("cover").getValue()).toString();
                        if (!cover.equals("")) {
                            Picasso.get().load(cover).into(profileCover);
                        }
                    }
                    profileImageUrl = Objects.requireNonNull(snapshot.child("profileImage").getValue()).toString();
                    String city = Objects.requireNonNull(snapshot.child("city").getValue()).toString();
                    String country = Objects.requireNonNull(snapshot.child("country").getValue()).toString();
                    String profession = Objects.requireNonNull(snapshot.child("profession").getValue()).toString();
                    String username = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                    String status = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

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
                Toast.makeText(ProfileEditOrSetupActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        profileImageView.setOnClickListener(v -> {
            state = 0;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE1);
        });

        profileCover.setOnClickListener(view -> {
            state = 1;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE2);
        });

        btnUpdate.setOnClickListener(v -> UpdateData());
    }

    private void startCropProfileImage(@NonNull Uri uri) {
        String destinationFileName = "profileCroppedImage";
        destinationFileName += ".jpg";

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(500, 500);
        uCrop.withOptions(getCropOptions());
        uCrop.start(ProfileEditOrSetupActivity.this);
    }
    private void startCropProfileCover(@NonNull Uri uri) {
        String destinationFileName = "profileCroppedCover";
        destinationFileName += ".jpg";

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(3, 1);
        uCrop.withMaxResultSize(1344, 448);
        uCrop.withOptions(getCropOptions());
        uCrop.start(ProfileEditOrSetupActivity.this);
    }

    private UCrop.Options getCropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(60);
        //CompressType
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);

        //UI
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(false);

        //Colors
        options.setStatusBarColor(getResources().getColor(R.color.white));
        options.setToolbarColor(getResources().getColor(R.color.white));

        options.setToolbarTitle("Обрезка изображения");

        return options;
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
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("username", username);
            hashMap.put("city", city);
            hashMap.put("country", country);
            hashMap.put("profession", profession);
            hashMap.put("status", status);
            hashMap.put("device_token", FirebaseInstanceId.getInstance().getToken());
            if (imageProfileUriResultCrop != null && imageCoverUriResultCrop == null) {
                StorageRefProfileImage.child(Uid).putFile(imageProfileUriResultCrop).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StorageRefProfileImage.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(uri -> {
                            hashMap.put("profileImage", uri.toString());
                            mUserRef.child(mUser.getUid()).updateChildren(hashMap).addOnSuccessListener(o -> {
                                mLoadingBar.dismiss();
                                Toast.makeText(ProfileEditOrSetupActivity.this, "Настройка профиля завершена", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext()
                                        , MainActivity.class));
                            }).addOnFailureListener(e -> {
                                mLoadingBar.dismiss();
                                Toast.makeText(ProfileEditOrSetupActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            });
                        });
                    }
                });
            } else if (imageProfileUriResultCrop == null && imageCoverUriResultCrop != null && profileImageUrl != null) {
                StorageRefCoverImage.child(Uid).putFile(imageCoverUriResultCrop).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StorageRefCoverImage.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(uri -> {
                            hashMap.put("cover", uri.toString());
                            mUserRef.child(mUser.getUid()).updateChildren(hashMap).addOnSuccessListener(o -> {
                                mLoadingBar.dismiss();
                                Toast.makeText(ProfileEditOrSetupActivity.this, "Настройка профиля завершена", Toast.LENGTH_SHORT).show();
                                finish();
                            }).addOnFailureListener(e -> {
                                mLoadingBar.dismiss();
                                Toast.makeText(ProfileEditOrSetupActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            });
                        });
                    }
                });
            } else if (imageProfileUriResultCrop != null && imageCoverUriResultCrop != null) {
                StorageRefProfileImage.child(Uid).putFile(imageProfileUriResultCrop).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        StorageRefProfileImage.child(Uid).getDownloadUrl().addOnSuccessListener(uri1 -> {
                            hashMap.put("profileImage", uri1.toString());
                            StorageRefCoverImage.child(Uid).putFile(imageCoverUriResultCrop).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    StorageRefCoverImage.child(Uid).getDownloadUrl().addOnSuccessListener(uri2 -> {
                                        hashMap.put("cover", uri2.toString());
                                        mUserRef.child(mUser.getUid()).updateChildren(hashMap).addOnSuccessListener(o -> {
                                            mLoadingBar.dismiss();
                                            Toast.makeText(ProfileEditOrSetupActivity.this, "Настройка профиля завершена", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }).addOnFailureListener(e -> {
                                            mLoadingBar.dismiss();
                                            Toast.makeText(ProfileEditOrSetupActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                        });
                                    });
                                }
                            });
                        });
                    }
                });
            } else if (imageProfileUriResultCrop == null && profileImageUrl != null) {
                mUserRef.child(Uid).updateChildren(hashMap).addOnSuccessListener(o -> {
                    mLoadingBar.dismiss();
                    Toast.makeText(ProfileEditOrSetupActivity.this, "Настройка профиля завершена", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> {
                    mLoadingBar.dismiss();
                    Toast.makeText(ProfileEditOrSetupActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                });
            } else {
                mLoadingBar.dismiss();
                Toast.makeText(ProfileEditOrSetupActivity.this, "При создании профиля необходимо добавить изображение", Toast.LENGTH_SHORT).show();
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
        if (requestCode == REQUEST_CODE1 && resultCode == RESULT_OK && state == 0 && data != null) {
            imageProfileUri = data.getData();
            if (imageProfileUri != null) {
                startCropProfileImage(imageProfileUri);
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && state == 0  && data != null) {
            imageProfileUriResultCrop = UCrop.getOutput(data);
            if (imageProfileUriResultCrop != null) {
                profileImageView.setImageURI(imageProfileUriResultCrop);
            }
        }
        if (requestCode == REQUEST_CODE2 && resultCode == RESULT_OK && state == 1  && data != null) {
            imageCoverUri = data.getData();
            if (imageCoverUri != null) {
                startCropProfileCover(imageCoverUri);
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK  && state == 1  && data != null) {
            imageCoverUriResultCrop = UCrop.getOutput(data);
            if (imageCoverUriResultCrop != null) {
                profileCover.setImageURI(imageCoverUriResultCrop);
            }
        }
    }
}
