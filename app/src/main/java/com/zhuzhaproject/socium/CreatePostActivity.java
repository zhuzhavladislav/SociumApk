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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class CreatePostActivity extends AppCompatActivity {
    int n = 0;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String profileImageUrlV, usernameV;

    private byte[] image_data;

    private String Uid;
    private DatabaseReference mUserRef;
    private DatabaseReference allPostsRef;
    private DatabaseReference postsToShowRef;
    private DatabaseReference usersPostRef;
    private DatabaseReference friendsRef;
    private StorageReference mStorageref;

    private ArrayList<String> friends_IdList;

    CircleImageView profileImage;
    ImageView addImagePost, sendImagePost;
    Bitmap compressedImageBitmap;

    EditText inputPostDesc;
    private static final int REQUEST_CODE = 101;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    Uri imageUri, imageUriResultCrop;
    ProgressDialog mLoadingBar;
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
        Uid = mAuth.getCurrentUser().getUid();

        friends_IdList = new ArrayList<String>();

        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        postsToShowRef = FirebaseDatabase.getInstance().getReference().child("PostsToShow");
        usersPostRef = FirebaseDatabase.getInstance().getReference().child("UsersPost").child(Uid);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(Uid);

        mStorageref = FirebaseStorage.getInstance().getReference();

        outputUsername = findViewById(R.id.outputUsername);

        inputPostDesc.addTextChangedListener(descTextWatcher);

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    friends_IdList.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        friends_IdList.add(Uid);

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
            }
        });


        // отключение анимации
        overridePendingTransition(0, 0);

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
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            System.out.println("|------------------------------| "+ imageUri);
            if (imageUri != null) {
                startCrop(imageUri);
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            imageUriResultCrop = UCrop.getOutput(data);
            System.out.println("|------------------------------| "+ imageUriResultCrop);
            if (imageUriResultCrop != null) {
                File image_file = new File(imageUriResultCrop.getPath());
                try {
                    compressedImageBitmap = new Compressor(this)
                            .setMaxHeight(800)
                            .setMaxWidth(800)
                            .setQuality(2)
                            .compressToBitmap(image_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                addImagePost.setImageURI(imageUriResultCrop);
                sendImagePost.setColorFilter(Color.rgb(67, 205, 232));
                sendImagePost.setClickable(true);
                n = 1;
            }
        }
    }

    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = SAMPLE_CROPPED_IMG_NAME;
        destinationFileName += ".jpg";

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(450, 450);
        uCrop.withOptions(getCropOptions());
        uCrop.start(CreatePostActivity.this);
    }

    private UCrop.Options getCropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(70);

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

        DatabaseReference userpost_push = allPostsRef.push();
        final String push_id = userpost_push.getKey();

        StorageReference posts_image_ref = mStorageref.child("Posts").child(Uid).child(push_id + ".jpg");
        String postDesc = inputPostDesc.getText().toString();
        if (imageUriResultCrop == null && postDesc.isEmpty()) {
            inputPostDesc.setError("Необходимо прикрепить изображение или написать описание");
            mLoadingBar.dismiss();
        } else {
            if (imageUriResultCrop == null) {
                final Map textmap = new HashMap();
                textmap.put("type", "text");
                textmap.put("timestamp", ServerValue.TIMESTAMP);
                textmap.put("by", Uid);
                textmap.put("postDesc", postDesc);
                textmap.put("image", "");
                textmap.put("likes", 0);

                final Map timeby2 = new HashMap();
                timeby2.put("timestamp", ServerValue.TIMESTAMP);
                timeby2.put("by", Uid);
                timeby2.put("liked", "false");

                allPostsRef.child(push_id).setValue(textmap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        for (String id : friends_IdList) {
                            postsToShowRef.child(id).child(push_id).setValue(timeby2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    usersPostRef.child(push_id).setValue(timeby2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toast.makeText(CreatePostActivity.this, "Успешно опубликовано", Toast.LENGTH_SHORT).show();
                                            mLoadingBar.dismiss();
                                            finish();

                                        }
                                    });
                                }
                            });
                        }
                    }
                });

            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                image_data = baos.toByteArray();
                System.out.println("HEEEEEEEEEEEEEEEEEREEEEEE " + posts_image_ref.getDownloadUrl().toString());


                UploadTask uploadTask = posts_image_ref.putBytes(image_data);
//                postImageRef.child(mUser.getUid() + strDate).putFile(image_data);

                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                        @SuppressWarnings("VisibleForTests") String download_url = posts_image_ref.getDownloadUrl().toString();
                        final Map imageMap = new HashMap();
                        imageMap.put("type", "image");
                        imageMap.put("timestamp", ServerValue.TIMESTAMP);
                        imageMap.put("by", Uid);
                        imageMap.put("postDesc", postDesc);
                        imageMap.put("image", download_url);
                        imageMap.put("likes", 0);

                        final Map timeBy = new HashMap();
                        timeBy.put("timestamp", ServerValue.TIMESTAMP);
                        timeBy.put("by", Uid);
                        timeBy.put("liked", "false");

                        allPostsRef.child(push_id).setValue(imageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                for (String id : friends_IdList) {
                                    postsToShowRef.child(id).child(push_id).setValue(timeBy).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            usersPostRef.child(push_id).setValue(timeBy).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(CreatePostActivity.this, "Упешно опубликовано", Toast.LENGTH_SHORT).show();
                                                    mLoadingBar.dismiss();
                                                    finish();

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