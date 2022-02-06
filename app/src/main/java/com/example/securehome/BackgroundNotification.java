package com.example.securehome;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class BackgroundNotification extends Service {
    public String CHANNEL_ID="SECUREHOME_NOTIFICATION_CHANNEL";
    FirebaseFirestore fd;
    DocumentReference ds;
    NotificationCompat.Builder builder;
    NotificationManagerCompat notificationManager;
    boolean USER_NOTIFICATION_CHOICE=true;

    public int createNotificationId(){
        return (int) SystemClock.uptimeMillis();
    }

    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
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

    @Override
    public void onCreate() {
        Log.d("BACKGROUNDSERVICE","STARTSERVICE");
        fd=FirebaseFirestore.getInstance();
        ds=fd.collection("SecureHome").document("1");

        createNotificationChannel();
        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Motion detected")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager = NotificationManagerCompat.from(this);
        USER_NOTIFICATION_CHOICE=getSharedPreferences("NOTIFICATION_PREFERENCES",MODE_PRIVATE).getBoolean("USER_NOTIFICATION_CHOICE",true);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ds.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    Log.d("FIRESTORE SNAPSHOT FAIL","motion error");
                }
                if (value != null && value.exists()) {
                    Log.d("VALUEX",value.get("Motion").toString());
                    if(value.get("Motion").toString().equals("1")){
                        if(USER_NOTIFICATION_CHOICE){
                            Log.d("BACKGROUNDSERVICE","ENTER ON EVENT");
                            notificationManager.notify(createNotificationId(),builder.build());
                        }
                    }

                } else {
                    Log.d( "EVENT SNAPSHOT","NULL");
                }
            }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("BACKGROUNDSERVICE","STOPSERVICE");
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
