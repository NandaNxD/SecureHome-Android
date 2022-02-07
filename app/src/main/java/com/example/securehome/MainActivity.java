package com.example.securehome;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    TextView alertTextView;
    ImageView imageView;
    boolean USER_NOTIFICATION_CHOICE=false;
    FirebaseFirestore db;
    private StorageReference mStorageRef;
    TextView imageInfo;
    ConstraintLayout constraintLayout;
    LottieAnimationView lottieAnimationView;
    MaterialButton emergencyCall;

    DocumentReference motion;
    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;

    public String getDate(long millis){
        DateFormat obj = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
        // we create instance of the Date and pass milliseconds to the constructor
        Date res = new Date(millis);
        // now we format the res by using SimpleDateFormat
        return (obj.format(res));
    }

    public void displayImage(View view){
        StorageReference storageReference=mStorageRef.child("image.jpeg");
        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                imageInfo.setText(getDate(storageMetadata.getCreationTimeMillis()));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_SHORT).show();
            }
        });
        GlideApp.with(this).load(storageReference).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(imageView);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        constraintLayout=findViewById(R.id.constraintLayout);
        alertTextView=findViewById(R.id.alertTextView);
        imageView=findViewById(R.id.imageView);
        lottieAnimationView=findViewById(R.id.animation_view);
        emergencyCall=findViewById(R.id.emergencyCall);
        imageInfo=findViewById(R.id.imageInfo);

        db=FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        motion=db.collection("SecureHome").document("1");

        preferences=getSharedPreferences("NOTIFICATION_PREFERENCES",MODE_PRIVATE);
        preferencesEditor=preferences.edit();

        USER_NOTIFICATION_CHOICE=preferences.getBoolean("USER_NOTIFICATION_CHOICE",true);
        motion.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    Log.d("FIRESTORE SNAPSHOT FAIL","motion error");
                }
                if (value != null && value.exists()) {
                    Log.d("VALUEX",value.get("Motion").toString());
                    if(value.get("Motion").toString().equals("1")){
                        alertTextView.setText("MOTION DETECTED!!!!");
                        constraintLayout.setBackgroundColor(Color.RED);
                        emergencyCall.setEnabled(true);
                        emergencyCall.setVisibility(View.VISIBLE);
                    }
                    else{
                        alertTextView.setText("NO MOTION DETECTED:)");
                        constraintLayout.setBackgroundColor(Color.rgb(255,51,153));
                        emergencyCall.setEnabled(false);
                        emergencyCall.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Log.d( "EVENT SNAPSHOT","NULL");
                }
            }
        });
        displayImage(new View(this));

        emergencyCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + "100"));
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item=menu.getItem(0);

        USER_NOTIFICATION_CHOICE=preferences.getBoolean("USER_NOTIFICATION_CHOICE",false);
        item.setChecked(USER_NOTIFICATION_CHOICE);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        USER_NOTIFICATION_CHOICE= !USER_NOTIFICATION_CHOICE;
        preferencesEditor.putBoolean("USER_NOTIFICATION_CHOICE",USER_NOTIFICATION_CHOICE);
        preferencesEditor.commit();
        item.setChecked(USER_NOTIFICATION_CHOICE);

        if(USER_NOTIFICATION_CHOICE){
            Log.d("BACKGROUNDSERVICE","mainactivity START");
            startService(new Intent(getApplication(),BackgroundNotification.class));
        }
        else{
            Log.d("BACKGROUNDSERVICE","mainactivity STOP");
            stopService(new Intent(getApplication(),BackgroundNotification.class));
        }

        return true;
    }
}