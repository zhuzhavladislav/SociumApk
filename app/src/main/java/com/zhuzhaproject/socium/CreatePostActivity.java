package com.zhuzhaproject.socium;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreatePostActivity extends AppCompatActivity {
    int n = 0;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String profileImageUrlV, usernameV;

    private String Uid;
    private DatabaseReference mUserRef, allPostsRef, postsToShowRef, usersPostRef, friendsRef;
    private StorageReference mStorageRef;

    private ArrayList<String> friends_IdList;
    CircleImageView profileImage;
    ImageView addImagePost, sendImagePost;
    EditText inputPostDesc;
    private static final int REQUEST_CODE = 101;
    Uri imageUri, imageUriResultCrop;
    ProgressDialog mLoadingBar;
    TextView outputUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        addImagePost = findViewById(R.id.addImagePost);
        sendImagePost = findViewById(R.id.send_comment);
        inputPostDesc = findViewById(R.id.input_comment);
        mLoadingBar = new ProgressDialog(this);

        profileImage = findViewById(R.id.profile_image);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        Uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        friends_IdList = new ArrayList<>();

        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        postsToShowRef = FirebaseDatabase.getInstance().getReference().child("PostsToShow");
        usersPostRef = FirebaseDatabase.getInstance().getReference().child("UsersPost").child(Uid);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(Uid);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        outputUsername = findViewById(R.id.outputUsername);

        inputPostDesc.addTextChangedListener(descTextWatcher);

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    friends_IdList.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        friends_IdList.add(Uid);

        sendImagePost.setOnClickListener(v -> AddPost());
        sendImagePost.setClickable(false);

        addImagePost.setOnClickListener(v -> {
            if (imageUriResultCrop != null) {
                imageUriResultCrop = null;
                imageUri = null;
                addImagePost.setImageResource(R.drawable.ic_add_image);
                sendImagePost.setColorFilter(Color.rgb(154, 156, 164));
                sendImagePost.setClickable(false);
                n = 0;
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });


        // отключение анимации
        overridePendingTransition(0, 0);

        //changing statusbar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.white));
        window.setNavigationBarColor(this.getResources().getColor(R.color.white));
    }

    private final TextWatcher descTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String inputTextSms = inputPostDesc.getText().toString().trim();
            if (inputTextSms.isEmpty() && n == 0) {
                sendImagePost.setColorFilter(Color.rgb(154, 156, 164));
                sendImagePost.setClickable(false);
            } else {
                sendImagePost.setColorFilter(Color.rgb(67, 205, 232));
                sendImagePost.setClickable(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                startCrop(imageUri);
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
            imageUriResultCrop = UCrop.getOutput(data);
            if (imageUriResultCrop != null) {
                addImagePost.setImageURI(imageUriResultCrop);
                sendImagePost.setColorFilter(Color.rgb(67, 205, 232));
                sendImagePost.setClickable(true);
                n = 1;
            }
        }
    }

    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = "postCropImg";
        destinationFileName += ".jpg";

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(1000, 1000);
        uCrop.withOptions(getCropOptions());
        uCrop.start(CreatePostActivity.this);
    }

    private UCrop.Options getCropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(60);

        //CompressType
        //options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        //options.setCompressionFormat(Bitmap.CompressFormat.JPEG);

        //UI
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true);

        //Colors
        options.setStatusBarColor(getResources().getColor(R.color.white));
        options.setToolbarColor(getResources().getColor(R.color.white));

        options.setToolbarTitle("Обрезка изображения");

        return options;
    }

    private void AddPost() {
        mLoadingBar.setMessage("Публикация...");
        mLoadingBar.setCanceledOnTouchOutside(false);
        mLoadingBar.show();

        DatabaseReference userPost_push = allPostsRef.push();
        final String push_id = userPost_push.getKey();

        String postDesc = inputPostDesc.getText().toString();
        if (imageUriResultCrop == null && postDesc.isEmpty()) {
            inputPostDesc.setError("Необходимо прикрепить изображение или написать описание");
            mLoadingBar.dismiss();
        } else {
            if (imageUriResultCrop == null) {
                final Map<Object, Object> textMap = new HashMap<>();
                textMap.put("type", "text");
                textMap.put("timestamp", ServerValue.TIMESTAMP);
                textMap.put("by", Uid);
                textMap.put("postDesc", postDesc);
                textMap.put("image", "");
                textMap.put("likes", 0);

                final Map<Object, Object> timeBy2 = new HashMap<>();
                timeBy2.put("timestamp", ServerValue.TIMESTAMP);
                timeBy2.put("by", Uid);
                timeBy2.put("liked", "false");

                assert push_id != null;
                allPostsRef.child(push_id).setValue(textMap).addOnSuccessListener(aVoid -> {
                    for (String id : friends_IdList) {
                        postsToShowRef.child(id).child(push_id).setValue(timeBy2).addOnSuccessListener(aVoid1 -> usersPostRef.child(push_id).setValue(timeBy2).addOnSuccessListener(aVoid11 -> {
                            Toast.makeText(CreatePostActivity.this, "Успешно опубликовано", Toast.LENGTH_SHORT).show();
                            mLoadingBar.dismiss();
                            finish();
                        }));
                    }
                });

            } else {
                assert push_id != null;
                mStorageRef.child("Posts").child(Uid).child(push_id).putFile(imageUriResultCrop).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mStorageRef.child("Posts").child(Uid).child(push_id).getDownloadUrl().addOnSuccessListener(uri -> {
                            final Map<Object, Object> imageMap = new HashMap<>();
                            imageMap.put("type", "image");
                            imageMap.put("timestamp", ServerValue.TIMESTAMP);
                            imageMap.put("by", Uid);
                            imageMap.put("postDesc", postDesc);
                            imageMap.put("image", uri.toString());
                            imageMap.put("likes", 0);

                            final Map<Object, Object> timeBy = new HashMap<>();
                            timeBy.put("timestamp", ServerValue.TIMESTAMP);
                            timeBy.put("by", Uid);
                            timeBy.put("liked", "false");

                            allPostsRef.child(push_id).setValue(imageMap).addOnSuccessListener(aVoid -> {
                                for (String id : friends_IdList) {
                                    postsToShowRef.child(id).child(push_id).setValue(timeBy).addOnSuccessListener(aVoid2 -> usersPostRef.child(push_id).setValue(timeBy).addOnSuccessListener(aVoid3 -> {
                                        Toast.makeText(CreatePostActivity.this, "Упешно опубликовано", Toast.LENGTH_SHORT).show();
                                        mLoadingBar.dismiss();
                                        finish();

                                    }));

                                }

                            });

                        });
                    } else {
                        mLoadingBar.dismiss();
                        Toast.makeText(CreatePostActivity.this, "" + Objects.requireNonNull(task.getException()), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUser == null) {
            SendUserToLoginActivity();
        } else {
            mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        profileImageUrlV = Objects.requireNonNull(dataSnapshot.child("profileImage").getValue()).toString();
                        usernameV = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                        Picasso.get().load(profileImageUrlV).into(profileImage);
                        outputUsername.setText(usernameV);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CreatePostActivity.this, "Извините! Что-то пошло не так...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void SendUserToLoginActivity() {
        Intent intent = new Intent(CreatePostActivity.this, AuthLoginActivity.class);
        startActivity(intent);
        finish();
    }
}