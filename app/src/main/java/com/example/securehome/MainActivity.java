package com.example.securehome;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class MainActivity extends AppCompatActivity {
    public String CHANNEL_ID="SECUREHOME_NOTIFICATION_CHANNEL";
    TextView alertTextView;
    ImageView imageView;
    boolean USER_NOTIFICATION_CHOICE=true;
    FirebaseFirestore db;
    private StorageReference mStorageRef;
    MenuItem notificationCheckbox;
    DocumentReference motion;
    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;


    public void displayImage(View view){
        StorageReference storageReference=mStorageRef.child("image.jpeg");
        GlideApp.with(this).load(storageReference).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(imageView);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SecureHomeChannel";
            String description = "SecureHome Notification Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alertTextView=findViewById(R.id.alertTextView);
        imageView=findViewById(R.id.imageView);


        db=FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        motion=db.collection("SecureHome").document("1");

        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Motion detected")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

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
                        alertTextView.setText("Motion Detected!!!!");
                        if(USER_NOTIFICATION_CHOICE){
                            notificationManager.notify(1,builder.build());
                        }
                    }
                    else{
                        alertTextView.setText("No Motion :)");
                    }
                } else {
                    Log.d( "EVENT SNAPSHOT","NULL");
                }
            }
        });

        displayImage(new View(this));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item=menu.getItem(0);
        USER_NOTIFICATION_CHOICE=preferences.getBoolean("USER_NOTIFICATION_CHOICE",true);
        item.setChecked(USER_NOTIFICATION_CHOICE);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        USER_NOTIFICATION_CHOICE= !USER_NOTIFICATION_CHOICE;
        preferencesEditor.putBoolean("USER_NOTIFICATION_CHOICE",USER_NOTIFICATION_CHOICE);
        preferencesEditor.commit();
        item.setChecked(USER_NOTIFICATION_CHOICE);
        return true;
    }
}