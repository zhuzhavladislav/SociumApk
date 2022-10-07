package com.zhuzhaproject.socium;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class AuthLoginActivity extends AppCompatActivity {

    private TextInputLayout inputEmail, inputPassword;
    private ProgressDialog mLoadingBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        //TextView forgotPassword = findViewById(R.id.forgotPassword);
        TextView createNewAccount = findViewById(R.id.createNewAccount);
        mLoadingBar=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();

        createNewAccount.setOnClickListener(v -> {
            Intent intent = new Intent(AuthLoginActivity.this, AuthRegisterActivity.class);
            startActivity(intent);
        });
        btnLogin.setOnClickListener(v -> AttemptLogin());

    }

    private void AttemptLogin() {
        String email= Objects.requireNonNull(inputEmail.getEditText()).getText().toString();
        String password= Objects.requireNonNull(inputPassword.getEditText()).getText().toString();
        if (!email.contains("@") || !email.contains("."))
        {
            showError(inputEmail, "Электронный адрес не введен или введен неправильно");
        }else if(password.isEmpty() || password.length()<=5)
        {
            showError(inputPassword, "Пароль должен быть больше 5 символов");
        }else
        {
            mLoadingBar.setTitle("Вход");
            mLoadingBar.setMessage("Пожалуйста подождите");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if(task.isSuccessful())
                {
                    mLoadingBar.dismiss();
                    Toast.makeText(AuthLoginActivity.this,"Вы успешно вошли", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AuthLoginActivity.this, SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }else
                {
                    mLoadingBar.dismiss();
                    Toast.makeText(AuthLoginActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void showError(TextInputLayout field, String text) {
        field.setError(text);
        field.requestFocus();
    }
}