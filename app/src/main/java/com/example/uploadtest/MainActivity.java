package com.example.uploadtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {


    private TextView upload;
    private Button photo, submit;

    DatabaseReference userDatabaseRef;
     


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        upload= findViewById(R.id.upload);
        photo= findViewById(R.id.photo);
        submit= findViewById(R.id.submit);




        /**
         * Upload a photo uri
         */
        photo.setOnClickListener(new View.OnClickListener() {
            static final int REQUEST_IMAGE_GET=1;
            @Override
            public void onClick(View view) {

                Intent intent= new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,REQUEST_IMAGE_GET);


            }
        });






    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode== RESULT_OK){

            Bitmap thumbnail= data.getParcelableExtra("data");
            Uri fullphotoUri= data.getData();

            final String photouri= fullphotoUri.getPath().trim();

            upload.setText(photouri);

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    if(fullphotoUri!=null){
                        final StorageReference filepath= FirebaseStorage.getInstance().getReference()
                                .child("profile_images");
                        Bitmap bitmap=null;
                        try{
                            bitmap= MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),fullphotoUri);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,20,byteArrayOutputStream);
                        byte[]data= byteArrayOutputStream.toByteArray();

                        UploadTask uploadTask= filepath.putBytes(data);

                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this,"Upload failed",Toast.LENGTH_SHORT).show();


                            }
                        });
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                if(taskSnapshot.getMetadata()!=null&&taskSnapshot.getMetadata().getReference()!=null){
                                    Task<Uri> result=taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String imageuri=uri.toString();
                                            Map getimagemap=new HashMap();
                                            getimagemap.put("profileimage",imageuri);

                                            userDatabaseRef.updateChildren(getimagemap).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(MainActivity.this,"Image uploaded successfuly",Toast.LENGTH_SHORT);
                                                    }else{
                                                        Toast.makeText(MainActivity.this,task.getException().toString(),Toast.LENGTH_SHORT);
                                                    }

                                                }
                                            });
                                            finish();
                                        }
                                    });

                                }

                            }
                        });







                    }

                }
            });

        }


    }
}