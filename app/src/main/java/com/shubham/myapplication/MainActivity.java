package com.shubham.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
 private Toolbar mainToolbar;
 private FirebaseAuth mAuth;
 private FloatingActionButton addButton;
 private FirebaseFirestore firebaseFirestore;
 private String currentUserid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            mAuth = FirebaseAuth.getInstance();
            firebaseFirestore = FirebaseFirestore.getInstance();



            mainToolbar = findViewById(R.id.main_toolbar);
            setSupportActionBar(mainToolbar);
            getSupportActionBar().setTitle("Blogger's Hub");

            addButton = findViewById(R.id.add_button);
            addButton.setImageResource(R.drawable.add);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(MainActivity.this,NewPost.class);
                    startActivity(intent);
                    
                }
            });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // User is signed in
          sendToLogin();

        }else {

            currentUserid = mAuth.getCurrentUser().getUid();
           firebaseFirestore.collection("Users").document(currentUserid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
               @Override
               public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                   if(task.isSuccessful()) {

                       if (!task.getResult().exists()) {
                           Intent setup = new Intent(MainActivity.this, SetupActivity.class);
                           startActivity(setup);
                           finish();

                       }


                   }else {
                       String error = task.getException().getMessage();
                       Toast.makeText(MainActivity.this, "error: "+error, Toast.LENGTH_SHORT).show();
                   }

               }
           });
        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_logout_btn:
                logout();
                return true;
            case R.id.action_setting_btn:
                Intent intent = new Intent(MainActivity.this,SetupActivity.class);
                startActivity(intent);

            default:
                return false;

        }


    }

    private void logout() {

        mAuth.signOut();
        sendToLogin();

    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}