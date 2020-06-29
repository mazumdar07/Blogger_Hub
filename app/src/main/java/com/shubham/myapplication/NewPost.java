package com.shubham.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NewPost extends AppCompatActivity {

    private Toolbar addToolbar;
    private ImageView imageView;
    private EditText titleAddPost;
    private EditText description;
    private Button add_post_button;
    private Uri imageUri = null;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        currentUserId =firebaseAuth.getCurrentUser().getUid();

        addToolbar = findViewById(R.id.add_toolbar);
        setSupportActionBar(addToolbar);
        getSupportActionBar().setTitle("New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView =findViewById(R.id.postImage);
        titleAddPost = findViewById(R.id.title);
        description = findViewById(R.id.description);
        add_post_button = findViewById(R.id.add_post_button);
        progressBar = findViewById(R.id.progressBar);

        imageView.setImageResource(R.drawable.defaultimage); //sets the default image in add post

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(16,9)
                        .start(NewPost.this);
            }
        });
        add_post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String desc = description.getText().toString();
                final String title = titleAddPost.getText().toString();
                //checking whether any field is empty or not
                if(!TextUtils.isEmpty(desc) && !TextUtils.isEmpty(title) && imageUri !=null){

                    progressBar.setVisibility(View.VISIBLE);

                    Random rand = new Random();
                    int n = rand.nextInt(10000);
                    final StorageReference filePath = storageReference.child("post_images").child(n +".png");
                    filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                Uri downloadUri = filePath.getDownloadUrl().getResult();


                                Map<String , Object> postMap = new HashMap<>();
                                postMap.put("image_url",downloadUri);
                                postMap.put("description",desc);
                                postMap.put("title",title);
                                postMap.put("user_id",currentUserId);


                                firebaseFirestore.collection("Posts").document(currentUserId).set(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            Toast.makeText(NewPost.this, "post Uploaded", Toast.LENGTH_LONG).show();
                                            Intent mainIntent = new Intent(NewPost.this,MainActivity.class);
                                            startActivity(mainIntent);
                                            finish();

                                        }else {

                                            String error = task.getException().getMessage();
                                            Toast.makeText(NewPost.this, "error: " + error, Toast.LENGTH_LONG).show();


                                        }
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }else {
                                progressBar.setVisibility(View.INVISIBLE);
                                String error = task.getException().getMessage();
                                Toast.makeText(NewPost.this, "error: " + error, Toast.LENGTH_LONG).show();


                            }
                        }
                    });

                }

            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();
                imageView.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}