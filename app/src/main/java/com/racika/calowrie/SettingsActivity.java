package com.racika.calowrie;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.usage.NetworkStats;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class SettingsActivity extends AppCompatActivity {
    private static final String LOG_TAG = OverviewActivity.class.getName();
    private static final int SWITCH_ON_NOTIFICATION_ID = 100;
    static final int SIX_HOURS_NOTIFICATION_ID = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;
    private Button switchThemeButton;
    int preferedTheme = R.drawable.background_gradient;
    Switch notifSwitch;
    String userID;
    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        ConstraintLayout constraintLayout = findViewById(R.id.main);
        constraintLayout.setBackgroundResource(preferedTheme);

        switchThemeButton = findViewById(R.id.switchThemeButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getUid();



        DocumentReference documentReference = firestore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value.getString("preferedTheme").equals("default")) {
                    preferedTheme = R.drawable.background_gradient;
                } else if (value.getString("preferedTheme").equals("tomato")) {
                    preferedTheme = R.drawable.background_tomato;
                }

                ConstraintLayout constraintLayout = findViewById(R.id.main);
                constraintLayout.setBackgroundResource(preferedTheme);

            }
        });

        createNotificationChannel();


        switchThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemePopup();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);


        notifSwitch = findViewById(R.id.notificationSwitch);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String notificationStatus = document.getString("notifications");
                        if (notificationStatus != null) {
                            if (notificationStatus.equals("on") || notificationStatus.equals("shown")) {
                                notifSwitch.setChecked(true);
                            } else if (notificationStatus.equals("off")) {
                                notifSwitch.setChecked(false);
                            }
                        }
                    }
                } else {
                    Log.d(LOG_TAG, "get failed with ", task.getException());
                }
            }
        });

        notifSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkAndSwitch(isChecked);
            }
        });


    }

    private void checkAndSwitch(boolean isChecked) {
        DocumentReference documentReference = firestore.collection("users").document(userID);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String notificationStatus = document.getString("notifications");

                        if (isChecked) {
                            if (notificationStatus != null && notificationStatus.equals("off")) {
                                showNotification("From now on, you will get notifications for your health!", SWITCH_ON_NOTIFICATION_ID);
                                documentReference.update("notifications", "on");
                            }
                        } else {
                            if (notificationStatus.equals("shown") || notificationStatus.equals("on")) {
                                cancelNotification(SWITCH_ON_NOTIFICATION_ID);
                                documentReference.update("notifications", "off");
                            }
                        }
                    } else {
                        Log.d(LOG_TAG, "No such document");
                    }
                } else {
                    Log.d(LOG_TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "default_channel";
            String description = "Default Notification Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("default_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    void showNotification(String message, int notificationId) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default_channel")
                .setSmallIcon(R.drawable.forkicon)
                .setContentTitle("Notifications!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
    }
    public static void showNotificationFromSettings(Context context, String message, int notificationId) {

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default_channel")
                .setSmallIcon(R.drawable.forkicon)
                .setContentTitle("Congratulations!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }




    private void cancelNotification(int notificationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(notificationId);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showNotification("From now on , you will get notifications for your health!", SWITCH_ON_NOTIFICATION_ID);
                //Toast.makeText(this, "Dark Theme is ON", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied, cannot show notification", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_home) {

            Intent intent = new Intent(this, OverviewActivity.class);
            startActivity(intent);
            finish();
            //Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show();
            return true;

        } else if (itemId == R.id.action_settings) {

            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            finish();
            //Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showThemePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.theme_popup, null);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);

        builder.setView(dialogView)
                .setTitle("Switch Theme")
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    RadioButton selectedRadio = dialogView.findViewById(selectedId);

                    String selectedTheme = "default";
                    if(selectedRadio.getText().equals("Default Theme")){
                        selectedTheme = "default";
                    } else if (selectedRadio.getText().equals("Tomato Theme")){
                        selectedTheme = "tomato";
                    }


                    updateUserTheme(selectedTheme);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updateUserTheme(String selectedTheme) {

        String userID = firebaseAuth.getUid();
        if (userID == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
            return;
        }


        DocumentReference userDocRef = firestore.collection("users").document(userID);


        userDocRef.update("preferedTheme", selectedTheme)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Theme updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    //Toast.makeText(this, "Failed to update theme: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void logout(View view) {

        //Toast.makeText(SettingsActivity.this, "Logout clicked", Toast.LENGTH_SHORT).show();
        Intent logout = new Intent(this, MainActivity.class);
        startActivity(logout);
        finish();

    }




}
