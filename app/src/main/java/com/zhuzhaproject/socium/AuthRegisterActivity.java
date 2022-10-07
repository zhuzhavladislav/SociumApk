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

public class AuthRegisterActivity extends AppCompatActivity {

    private TextInputLayout inputEmail, inputPassword, inputConfirmPassword;
    Button btnRegister;
    TextView alreadyHaveAccount;
    FirebaseAuth mAuth;
    ProgressDialog mLoadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputPassword);
        inputConfirmPassword=findViewById(R.id.inputConfirmPassword);
        btnRegister=findViewById(R.id.btnRegister);
        alreadyHaveAccount=findViewById(R.id.alreadyHaveAccount);
        mAuth=FirebaseAuth.getInstance();
        mLoadingBar=new ProgressDialog(this);

        btnRegister.setOnClickListener(v -> AttemptRegistration());
        alreadyHaveAccount.setOnClickListener(v -> {
            Intent intent = new Intent(AuthRegisterActivity.this, AuthLoginActivity.class);
            startActivity(intent);
        });
    }

    private void AttemptRegistration() {
        String email= Objects.requireNonNull(inputEmail.getEditText()).getText().toString();
        String password= Objects.requireNonNull(inputPassword.getEditText()).getText().toString();
        String confirmPassword= Objects.requireNonNull(inputConfirmPassword.getEditText()).getText().toString();


        if (!email.contains("@") || !email.contains("."))
        {
            showError(inputEmail, "Электронный адрес не введен или введен неправильно");
        }else if(password.isEmpty() || password.length()<=5)
        {
            showError(inputPassword, "Пароль должен быть больше 5 символов");
        }else if(!confirmPassword.equals(password))
        {
            showError(inputConfirmPassword, "Пароли не совпадают!");
        }else
        {
            mLoadingBar.setTitle("Регистрация");
            mLoadingBar.setMessage("Пожалуйста подождите, пока мы вас регистрируем");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
                    mLoadingBar.dismiss();
                    Toast.makeText(AuthRegisterActivity.this,"Вы успешно зарегистрировались", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AuthRegisterActivity.this, ProfileEditOrSetupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    mLoadingBar.dismiss();
                    Toast.makeText(AuthRegisterActivity.this,"Ошибка регистрации", Toast.LENGTH_SHORT).show();

                }
            });

        }

    }

    private void showError(TextInputLayout field, String text) {
        field.setError(text);
        field.requestFocus();
    }
}