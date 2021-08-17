package com.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.transition.Transition;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.protobuf.UInt64Value;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpProfilActivity extends AppCompatActivity {

    private EditText edName ;
    private CircleImageView circleImageView ;
    private MaterialButton buSave ;
    private Toolbar toolbar;
    private FirebaseAuth auth ;
    private Uri imgUri= null ;
    private StorageReference storageReference ;
    private FirebaseFirestore firestore;
    private String Uid ;
    private ProgressBar progressBar ;

 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_profil);

        toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        edName = findViewById(R.id.edNameID);
        circleImageView = findViewById(R.id.circleImageViewID);
        buSave = findViewById(R.id.buSaveID);
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
        Uid = auth.getCurrentUser().getUid();
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.INVISIBLE);

        // Remplir Nom et Image par des données retourner par Firebase
        firestore.collection("Users").document(Uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists())
                    {
                        String nom = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        edName.setText(nom);
                        Glide.with(SetUpProfilActivity.this).load(image).into(circleImageView);
                    }
                }
            }
        });

        buSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);

                String name = edName.getText().toString();

                if (!name.isEmpty() && imgUri != null)
                {
                    StorageReference imgRef = storageReference.child("Profile_pics").child(Uid + ".jpg");
                    imgRef.putFile(imgUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful())
                            {
                                imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        saveToFirestore(task , name , uri);
                                    }
                                });
                            }
                            else
                            {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(SetUpProfilActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpProfilActivity.this, "Please select picture and write your name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // open gallery in your app
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if (ContextCompat.checkSelfPermission(SetUpProfilActivity.this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(SetUpProfilActivity.this , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE} , 1);
                    }
                    else {
                        /*// start picker to get image for cropping and then use the image in cropping activity
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetUpProfilActivity.this);*/

                        Intent galleryIntent = new Intent();
                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent , 2);
                    }
                }
            }
        });

    }



    private void saveToFirestore(Task<UploadTask.TaskSnapshot> task, String name, Uri downloadUri) {
            HashMap<String , Object> hashMap = new HashMap<>();
            hashMap.put("name" , name);
            hashMap.put("image" , downloadUri.toString());

            firestore.collection("Users").document(Uid).set(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SetUpProfilActivity.this, "Profile settings saved", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SetUpProfilActivity.this , MainActivity.class));
                        finish();
                    }
                    else
                    {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SetUpProfilActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    // select image from gallery to your app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (requestCode == RESULT_OK){
                imgUri = result.getUri();
                circleImageView.setImageURI(imgUri);
                Toast.makeText(SetUpProfilActivity.this, "Image success", Toast.LENGTH_SHORT).show();
            }
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toast.makeText(SetUpProfilActivity.this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }*/

        if (requestCode == 2 && resultCode == RESULT_OK && data != null){
            imgUri = data.getData();
            circleImageView.setImageURI(imgUri);


        }

    }





}