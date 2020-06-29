package com.shubham.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageUrl = null;
    private EditText setup_name;
    private boolean isChanged =false;
    private Button setup_button;
    private ProgressBar progressBar;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;

    private static final int IMAGE_REQUEST = 2;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setup_name = findViewById(R.id.setup_name);
        setup_button = findViewById(R.id.setup_button);

        Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        setupImage = findViewById(R.id.setupImage);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id =firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        progressBar = findViewById(R.id.setup_progress);
        setup_button.setEnabled(false);

        setupImage.setImageResource(R.drawable.defaultimage);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageUrl = Uri.parse(image);

                        setup_name.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.defaultimage);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);


                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "error: " + error, Toast.LENGTH_LONG).show();

                }
                progressBar.setVisibility(View.INVISIBLE);
                setup_button.setEnabled(true);

            }
        });

        setup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String user_name = setup_name.getText().toString();
                final StorageReference image_path = storageReference.child("profile_Images").child(user_id + ".png");
                if(!TextUtils.isEmpty(user_name) && mainImageUrl!=null) {
                    if(isChanged){


                        user_id = firebaseAuth.getCurrentUser().getUid();
                        progressBar.setVisibility(View.VISIBLE);

                        image_path.putFile(mainImageUrl).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                storeFireStore(task,user_name,image_path);

                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "error: " + error, Toast.LENGTH_LONG).show();
                            }

                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });


                }else {

                        Toast.makeText(SetupActivity.this,"wrong",Toast.LENGTH_SHORT);
                        storeFireStore(null,user_name,image_path);


                }
                }


            }
        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }else {
                        imagepicker();
                    }
                }else{
                    imagepicker();
                }

            }
        });

    }

    private void storeFireStore(Task<UploadTask.TaskSnapshot> task, String user_name, StorageReference image_path) {

        Uri downloadUrl;
        if(task == null){
            downloadUrl = image_path.getDownloadUrl().getResult();
        }else {
            downloadUrl = mainImageUrl;
        }


        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", user_name);
        userMap.put("image", downloadUrl.toString());
        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();


                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "error: " + error, Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void imagepicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUrl = result.getUri();
                setupImage.setImageURI(mainImageUrl);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}