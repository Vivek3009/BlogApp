package rob.myappcompany.myblogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class setupActivity extends AppCompatActivity {

    private Toolbar setupToolBar;
    private boolean isChanged=false;
    private CircleImageView setupImageView;
    private Uri mainImageUri=null;
    private EditText nameEditText;
    private Button saveToAccountBtn;
    private ProgressBar set_upProgressBar;
    private Bitmap compressedImageFile;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private  String user_id;
    private FirebaseFirestore firebaseFirestore;
    private String thumbProfileImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setSupportActionBar((androidx.appcompat.widget.Toolbar) findViewById(R.id.setupToolBar));
        getSupportActionBar().setTitle("Account Setup");

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        nameEditText=findViewById(R.id.nameEditText);
        saveToAccountBtn=findViewById(R.id.saveAccountBtn);
        setupImageView=findViewById(R.id.circularImageView);
        set_upProgressBar=findViewById(R.id.set_up_progressBar);


        user_id=firebaseAuth.getCurrentUser().getUid();

        set_upProgressBar.setVisibility(View.VISIBLE);
        saveToAccountBtn.setEnabled(false);
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(Objects.requireNonNull(task.getResult()).exists())
                    {
                        String user_name=task.getResult().getString("name");
                        String user_imageUrl=task.getResult().getString("image");
                        thumbProfileImageUri=task.getResult().getString("thumb_image_uri");
                        //Toast.makeText(setupActivity.this, thumbProfileImageUri, Toast.LENGTH_SHORT).show();
                        nameEditText.setText(user_name);

                        mainImageUri=Uri.parse(user_imageUrl);
                        RequestOptions placeHolderRequest=new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.default_image);
                        Glide.with(setupActivity.this).applyDefaultRequestOptions(placeHolderRequest).load(user_imageUrl).into(setupImageView);

                    }
                }
                else
                {
                    String error=task.getException().getMessage();
                    Toast.makeText(setupActivity.this, "FireStore Retrieve Error : "+error, Toast.LENGTH_SHORT).show();
                }
                set_upProgressBar.setVisibility(View.INVISIBLE);
                saveToAccountBtn.setEnabled(true);
            }
        });

        saveToAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = nameEditText.getText().toString();
                if (!TextUtils.isEmpty(name) && mainImageUri != null)

                 {
                     set_upProgressBar.setVisibility(View.VISIBLE);
                    if (isChanged) {

                        user_id = firebaseAuth.getCurrentUser().getUid();
                        final StorageReference image_path = storageReference.child("profile_image").child(user_id + ".jpg");
                        image_path.putFile(mainImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri1) {
                                        final Uri image_download_uri = uri1;

                                        File newImageFile=new File(mainImageUri.getPath());
                                        try {
                                            compressedImageFile = new Compressor(setupActivity.this)
                                                    .setMaxHeight(50)
                                                    .setMaxWidth(50)
                                                    .setQuality(1).compressToBitmap(newImageFile);

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        ByteArrayOutputStream baos=new ByteArrayOutputStream();
                                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                        byte[] thumbData=baos.toByteArray();
                                        UploadTask uploadTask=storageReference.child("profile_image/thumbs").child(user_id+".jpg").putBytes(thumbData);

                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                if(taskSnapshot.getMetadata()!=null){
                                                    if(taskSnapshot.getMetadata().getReference()!=null){
                                                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                thumbProfileImageUri=uri.toString();
                                                                check(image_download_uri,name,thumbProfileImageUri);
                                                            }
                                                        });
                                                    }

                                                }
                                            }
                                        });



                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                String error = e.getMessage();
                                Toast.makeText(setupActivity.this, "Image Error: " + error, Toast.LENGTH_SHORT).show();
                                set_upProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    else
                    {
                       // Toast.makeText(setupActivity.this, thumbProfileImageUri, Toast.LENGTH_SHORT).show();
                        check(null,name,thumbProfileImageUri);
                    }
                }
                else
                {
                    Toast.makeText(setupActivity.this, "Please Fulfill All Requirements", Toast.LENGTH_SHORT).show();
                    set_upProgressBar.setVisibility(View.INVISIBLE);


                }
            }
        });
        setupImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(setupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(setupActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                        //Toast.makeText(setupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        BuildImagePicker();
                    }
                }
                else
                {
                    // in this case we get permission in play store directly
                    BuildImagePicker();
                }
            }
        });


    }

    private void check(Uri download_uri,String name,String thhumbImageUri) {
        if(download_uri==null)
        {
            download_uri=mainImageUri;
            thhumbImageUri=thumbProfileImageUri;
        }
        Map<String, String> userMap = new HashMap<>();
        //Toast.makeText(this, thhumbImageUri, Toast.LENGTH_SHORT).show();
        userMap.put("name", name);
        userMap.put("image", download_uri.toString());
        userMap.put("thumb_image_uri",thhumbImageUri);

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(setupActivity.this, "Settings Are Updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(setupActivity.this, MainActivity.class));
                    finish();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(setupActivity.this, "Firebase Error :" + error, Toast.LENGTH_SHORT).show();
                }
                set_upProgressBar.setVisibility(View.INVISIBLE);
            }
        });



    }

    private void BuildImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(setupActivity.this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();

                setupImageView.setImageURI(mainImageUri);
                isChanged=true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
