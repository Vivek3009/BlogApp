package rob.myappcompany.myblogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
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
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;


public class addPostActivity extends AppCompatActivity {


    private EditText addPostDescription;
    private ImageView addPostImageView;
    private Button addPostSaveButton;
    private Toolbar addPostToolBar;
    private Uri uri=null;
    private ProgressBar addPostProgressBar;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    public String desc=null;
    private Bitmap compressedImageFile;
    String thumbimageUri;
    String download_uri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);


        setSupportActionBar((Toolbar) findViewById(R.id.addPostToolBar));
         getSupportActionBar().setTitle("Adding Post");



        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        addPostDescription = findViewById(R.id.addPostDescription);
        addPostImageView = findViewById(R.id.addPostImageView);
        addPostSaveButton = findViewById(R.id.addPostSaveBtn);
        addPostProgressBar = findViewById(R.id.addPostProgressBar);



        user_id = firebaseAuth.getCurrentUser().getUid();

        addPostImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,5121)
                        .setAspectRatio(1, 1)
                        .start(addPostActivity.this);
            }
        });
        addPostSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                  desc=addPostDescription.getText().toString();
                  addPostSaveButton.setEnabled(false);
                if(!TextUtils.isEmpty(desc)&&uri!=null)
                {
                    addPostProgressBar.setVisibility(View.VISIBLE);
                    final String random_string= UUID.randomUUID().toString();
                   final StorageReference image_path = storageReference.child("post_image").child( random_string+ ".jpg");
                    image_path.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri1) {
                                   File newImageFile=new File(uri.getPath());
                                    try {
                                        compressedImageFile = new Compressor(addPostActivity.this)
                                                .setMaxHeight(200)
                                                .setMaxWidth(200)
                                                .setQuality(2).compressToBitmap(newImageFile);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    download_uri = uri1.toString();
                                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                    byte[] thumbData=baos.toByteArray();

                                    UploadTask uploadTask=storageReference.child("post_image/thumbs").child(random_string+".jpg").putBytes(thumbData);
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            if (taskSnapshot.getMetadata() != null) {
                                                if (taskSnapshot.getMetadata().getReference() != null) {
                                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                             thumbimageUri = uri.toString();

                                                            //Toast.makeText(addPostActivity.this, thumbimageUri, Toast.LENGTH_SHORT).show();
                                                            //createNewPost(imageUrl);
                                                            Map<String,Object> postMap=new HashMap<>();
                                                            postMap.put("image_url",download_uri);
                                                            postMap.put("desc",desc);
                                                            postMap.put("thumb_image",thumbimageUri);
                                                            postMap.put("user_id",user_id);
                                                            postMap.put("timestamp",FieldValue.serverTimestamp());

                                                            firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                    if(task.isSuccessful()){

                                                                        Toast.makeText(addPostActivity.this, "Post Was Added", Toast.LENGTH_SHORT).show();
                                                                        startActivity(new Intent(addPostActivity.this,MainActivity.class));
                                                                        finish();
                                                                    }else
                                                                    {
                                                                        String error=task.getException().getMessage();
                                                                        Toast.makeText(addPostActivity.this, "Error : "+error, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    addPostProgressBar.setVisibility(View.INVISIBLE);
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        }});




                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String error = e.getMessage();
                            Toast.makeText(addPostActivity.this, "Image Error: " + error, Toast.LENGTH_SHORT).show();
                            addPostProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
                addPostSaveButton.setEnabled(true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uri = result.getUri();

                addPostImageView.setImageURI(uri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
