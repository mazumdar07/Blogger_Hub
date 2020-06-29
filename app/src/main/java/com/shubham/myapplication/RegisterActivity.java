package com.shubham.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText reg_email_field,reg_passwprd_field,reg_confirm_password;
    private Button reg_btn,reg_login_btn;
    private ProgressBar reg_progress;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth =FirebaseAuth.getInstance();
        reg_email_field = findViewById(R.id.register_email);
        reg_passwprd_field = findViewById(R.id.register_password);
        reg_confirm_password = findViewById(R.id.confirm_password);
        reg_btn = findViewById(R.id.register_button);
        reg_login_btn = findViewById(R.id.registration_login_button);
        reg_progress =findViewById(R.id.registration_progress);

        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = reg_email_field.getText().toString();
                String password = reg_passwprd_field.getText().toString();
                String confirm = reg_confirm_password.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirm)){
                    if(password.equals(confirm)){

                        reg_progress.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Intent setupIntent = new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();

                                }else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error :" +errorMessage, Toast.LENGTH_SHORT).show();
                                }
                                reg_progress.setVisibility(View.INVISIBLE);
                            }
                        });

                    }else {
                        Toast.makeText(RegisterActivity.this, " confirm password doesnt matches with password", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            sendToMain();

        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}