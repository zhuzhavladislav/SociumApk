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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    CircleImageView profileImageView;
    EditText inputUsername, inputCity, inputCountry, inputProfession;
    Button btnSave;
    Uri imageUri;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRef;
    StorageReference StorageRef;

    ProgressDialog mLoadingBar;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        toolbar=findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profileImageView=findViewById(R.id.profile_image);
        inputUsername=findViewById(R.id.inputUsername);
        inputCity=findViewById(R.id.inputCity);
        inputCountry=findViewById(R.id.inputCountry);
        inputProfession=findViewById(R.id.inputProfession);
        btnSave=findViewById(R.id.btnUpdate);
        mLoadingBar=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        mRef= FirebaseDatabase.getInstance().getReference().child("Users");
        StorageRef= FirebaseStorage.getInstance().getReference().child("ProfileImage");

        //changing statusbar color
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }


        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,REQUEST_CODE);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveData();
            }
        });
    }

    private void SaveData() {

        String username=inputUsername.getText().toString();
        String city=inputCity.getText().toString();
        String country=inputCountry.getText().toString();
        String profession=inputProfession.getText().toString();

        if(username.isEmpty() || username.length()<3)
        {
            showError(inputUsername,"Имя и фамилия введены неверно");
        }else if(city.isEmpty() || city.length()<3)
        {
            showError(inputCity,"Город введен неверно");
        }else if(country.isEmpty() || country.length()<3)
        {
            showError(inputCountry,"Страна введена неверно");
        }else if(profession.isEmpty() || profession.length()<3) {
            showError(inputProfession, "Профессия введена неверно");
        }else if(imageUri==null)
        {
            Toast.makeText(this,"Пожалуйста выберите фотографию", Toast.LENGTH_SHORT).show();
        }else
        {

            mLoadingBar.setTitle("Настройка профиля");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
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
                                hashMap.put("status","Socium");
                                hashMap.put("city", city);
                                hashMap.put("country", country);
                                hashMap.put("profession", profession);
                                hashMap.put("profileImage", uri.toString());
                                hashMap.put("device_token", FirebaseInstanceId.getInstance().getToken());


                                mRef.child(mUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        Intent intent=new Intent(SetupActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        mLoadingBar.dismiss();
                                        Toast.makeText(SetupActivity.this, "Настройка профиля завершена", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mLoadingBar.dismiss();
                                        Toast.makeText(SetupActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }

    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE && resultCode==RESULT_OK && data!=null)
        {
            imageUri=data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

}