package com.zhuzhaproject.socium;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreatePostActivity extends AppCompatActivity {
    int n=0;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserRef, postRef, likeRef;
    String profileImageUrlV, usernameV;

    CircleImageView profileImage;
    ImageView addImagePost, sendImagePost;
    Bitmap compressedImageBitmap;

    EditText inputPostDesc;
    private static final int REQUEST_CODE = 101;
    Uri imageUri;
    ProgressDialog mLoadingBar;
    StorageReference postImageRef;
    TextView outputUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        addImagePost = findViewById(R.id.addImagePost);
        sendImagePost = findViewById(R.id.send_post_imageView);
        inputPostDesc = findViewById(R.id.inputPostDesc);
        mLoadingBar = new ProgressDialog(this);

        profileImage = findViewById(R.id.profileImage);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postImageRef = FirebaseStorage.getInstance().getReference().child("PostImages");

        outputUsername = findViewById(R.id.outputUsername);

        inputPostDesc.addTextChangedListener(descTextWatcher);
        sendImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPost();
            }
        });
        sendImagePost.setClickable(false);

        addImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
                sendImagePost.setColorFilter(Color.rgb(67, 205, 232));
                sendImagePost.setClickable(true);
                n++;
            }
        });


        // отключение анимации
        overridePendingTransition(0,0);

        //changing statusbar color
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        }
    }

    private TextWatcher descTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String inputTextSms = inputPostDesc.getText().toString().trim();
            if (inputTextSms.isEmpty() && n==0){
                sendImagePost.setColorFilter(Color.rgb(154, 156, 164));
                sendImagePost.setClickable(false);
            }else{
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
            addImagePost.setImageURI(imageUri);
        }
    }

    private void AddPost() {
        String postDesc = inputPostDesc.getText().toString();
        if (imageUri == null && postDesc.isEmpty()){
            inputPostDesc.setError("Необходимо прикрепить изображение или написать описание");
        }else {
            if (imageUri == null) {
                //inputPostDesc.setError("Необходимо прикрепить изображение");
                mLoadingBar.setTitle("Выкладываем публикацию");
                mLoadingBar.setCanceledOnTouchOutside(false);
                mLoadingBar.show();

                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                String strDate = formatter.format(date);
                HashMap hashMap = new HashMap();
                hashMap.put("datePost", strDate);
                hashMap.put("postImageUrl", "");
                hashMap.put("postDesc", postDesc);
                hashMap.put("userProfileImageUrl", profileImageUrlV);
                hashMap.put("username", usernameV);
                postRef.child(mUser.getUid() + strDate).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            mLoadingBar.dismiss();
                            Toast.makeText(CreatePostActivity.this, "Публикация добавлена", Toast.LENGTH_SHORT).show();
                            addImagePost.setImageResource(R.drawable.ic_add_image);
                            inputPostDesc.setText("");
                            startActivity(new Intent(getApplicationContext()
                                    , MainActivity.class));
                        } else {
                            mLoadingBar.dismiss();
                            Toast.makeText(CreatePostActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else {
                mLoadingBar.setTitle("Выкладываем публикацию");
                mLoadingBar.setCanceledOnTouchOutside(false);
                mLoadingBar.show();

                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                String strDate = formatter.format(date);

                postImageRef.child(mUser.getUid() + strDate).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            postImageRef.child(mUser.getUid() + strDate).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {


                                    HashMap hashMap = new HashMap();
                                    hashMap.put("datePost", strDate);
                                    hashMap.put("postImageUrl", uri.toString());
                                    hashMap.put("postDesc", postDesc);
                                    hashMap.put("userProfileImageUrl", profileImageUrlV);
                                    hashMap.put("username", usernameV);
                                    postRef.child(mUser.getUid() + strDate).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if (task.isSuccessful()) {
                                                mLoadingBar.dismiss();
                                                Toast.makeText(CreatePostActivity.this, "Публикация добавлена", Toast.LENGTH_SHORT).show();
                                                addImagePost.setImageResource(R.drawable.ic_add_image);
                                                inputPostDesc.setText("");
                                                startActivity(new Intent(getApplicationContext()
                                                        , MainActivity.class));
                                            } else {
                                                mLoadingBar.dismiss();
                                                Toast.makeText(CreatePostActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        } else {
                            mLoadingBar.dismiss();
                            Toast.makeText(CreatePostActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
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
                        profileImageUrlV = dataSnapshot.child("profileImage").getValue().toString();
                        usernameV = dataSnapshot.child("username").getValue().toString();
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
        Intent intent = new Intent(CreatePostActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}