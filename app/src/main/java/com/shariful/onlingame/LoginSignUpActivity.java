package com.shariful.onlingame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginSignUpActivity extends AppCompatActivity {
    TextView goSignupTV,goLoginTv;
    LinearLayout loginLayout,signupLayout;
    private Button loginBtn,signUpBtn;
    private EditText emailEt_l,passwordEt_l,emailEt_s,passwordEt_s,nameEt,phoneEt;

    private FirebaseAuth mAuth;
    DatabaseReference refSignup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign_up);

        goSignupTV = findViewById(R.id.goSignUpTv_id);
        goLoginTv = findViewById(R.id.goLoginTv);

        loginLayout = findViewById(R.id.layoutLogin_id);
        signupLayout = findViewById(R.id.layoutSignup_id);
        loginBtn = findViewById(R.id.loginBtn_id);
        signUpBtn = findViewById(R.id.registerBtn_id);

        emailEt_l = findViewById(R.id.emailET_login);
        passwordEt_l = findViewById(R.id.passwordET_login);

        emailEt_s = findViewById(R.id.emailET_id);
        passwordEt_s = findViewById(R.id.passwordET_id);
        nameEt = findViewById(R.id.nameEt_id);
        phoneEt = findViewById(R.id.phoneET_id);

        refSignup= FirebaseDatabase.getInstance().getReference("userList");
        mAuth = FirebaseAuth.getInstance();


        goLoginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupLayout.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
            }
        });
        goSignupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginLayout.setVisibility(View.GONE);
                signupLayout.setVisibility(View.VISIBLE);
            }
        });


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });


    }
    public  void loginUser() {
        final String email = emailEt_l.getText().toString().trim();
        String password = passwordEt_l.getText().toString().trim();

        if (email.isEmpty()) {
            emailEt_l.setError("Enter an email address");
            emailEt_l.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt_l.setError("Enter a valid email address");
            emailEt_l.requestFocus();
            return;
        }

        //checking the validity of the password
        if (password.isEmpty()) {
            passwordEt_l.setError("Enter a password");
            passwordEt_l.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            SharedPreferences sharedPreferences = getSharedPreferences("info", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("emailkey",email);
                            editor.commit();
                            startActivity(new Intent(LoginSignUpActivity.this,MainActivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginSignUpActivity.this, "Failed To Login !!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    public  void registerUser() {

        String name = nameEt.getText().toString();
        String email = emailEt_s.getText().toString();
        String phone = phoneEt.getText().toString();
        String password = passwordEt_s.getText().toString();
        if(name.isEmpty())
        {
            nameEt.setError("Enter an email address");
            nameEt.requestFocus();
            return;
        }
        if(phone.isEmpty())
        {
            phoneEt.setError("Enter an email address");
            phoneEt.requestFocus();
            return;
        }

        if(email.isEmpty())
        {
            emailEt_s.setError("Enter an email address");
            emailEt_s.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            emailEt_s.setError("Enter a valid email address");
            emailEt_s.requestFocus();
            return;
        }

        //checking the validity of the password
        if(password.isEmpty())
        {
            passwordEt_s.setError("Enter a password");
            passwordEt_s.requestFocus();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(LoginSignUpActivity.this, "Registration Successful!!", Toast.LENGTH_SHORT).show();
                            saveUserInfo();

                        } else {
                            // If sign in fails, display a message to the user.
                            if(task.getException() instanceof FirebaseAuthUserCollisionException)
                            {
                                Toast.makeText(getApplicationContext(), "User is already registered !!", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "Error : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }

          private void saveUserInfo(){

              String userName = nameEt.getText().toString();
              String email = emailEt_s.getText().toString().trim();
              String phone = phoneEt.getText().toString();
              String password = passwordEt_s.getText().toString();

              String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();

              refSignup.child(uId).child("name").setValue(userName);
              refSignup.child(uId).child("email").setValue(email);
              refSignup.child(uId).child("phone").setValue(phone);
              refSignup.child(uId).child("balance").setValue("100");
              refSignup.child(uId).child("password").setValue(password);
              refSignup.child(uId).child("uid").setValue(uId);
              refSignup.child(uId).child("temp_score").setValue("0");
              Intent intent=new Intent(LoginSignUpActivity.this,MainActivity.class);
              startActivity(intent);
              finish();
          }



}