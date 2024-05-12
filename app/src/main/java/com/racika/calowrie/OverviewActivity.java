package com.racika.calowrie;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.Manifest;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.firestore.FieldValue;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.racika.calowrie.R;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class OverviewActivity extends AppCompatActivity {

    private static final String LOG_TAG = OverviewActivity.class.getName();
    public static final int CAMERA_PERMISSION_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    TextView usernameTV,emailTV,ageTV;
    CircleImageView profilePic;
    ImageButton changeProfilePicButton,cameraButton;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ProgressBar waterProgress;
    ProgressBar foodProgress;
    StorageReference storageReference;
    private AlertDialog alertDialog;

    int preferedTheme;
    boolean isFirstLoad = true;

    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_overview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });


        usernameTV = findViewById(R.id.overviewUsername);
        emailTV = findViewById(R.id.overviewEmail);
        ageTV = findViewById(R.id.overviewAge);

        profilePic = findViewById(R.id.profilePic);
        changeProfilePicButton = findViewById(R.id.changeProfilePic);
        cameraButton = findViewById(R.id.cameraButton);

        waterProgress = findViewById(R.id.waterProgress);
        foodProgress = findViewById(R.id.foodProgress);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        userID = firebaseAuth.getUid();


        StorageReference profilePicRef = storageReference.child("users/"+userID+"/profile.jpg");
        profilePicRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profilePic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // If profile picture doesn't exist, load default picture
                StorageReference defaultProfilePicRef = storageReference.child("users/default/profile.jpg");
                defaultProfilePicRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri defaultUri) {
                        Picasso.get().load(defaultUri).into(profilePic);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle failure to load default profile picture if needed
                    }
                });
            }
        });



        DocumentReference documentReference = firestore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                usernameTV.setText(value.getString("username"));
                emailTV.setText(value.getString("email"));
                ageTV.setText(value.getString("age"));

                if(value.getString("preferedTheme").equals("default")){
                    preferedTheme = R.drawable.background_gradient;


                }else if (value.getString("preferedTheme").equals("tomato")){
                    preferedTheme = R.drawable.background_tomato;
                }

                ConstraintLayout constraintLayout = findViewById(R.id.main);
                constraintLayout.setBackgroundResource(preferedTheme);


                if(isFirstLoad){

                    Long water = value.getLong("water");
                    int waterValue = water != null ? water.intValue() : 0;
                    waterProgress.setProgress(waterValue);


                    Long food = value.getLong("calories");
                    int foodValue = food != null ? food.intValue() : 0;
                    foodProgress.setProgress(foodValue);

                    isFirstLoad = false;
                }
            }
        });


        changeProfilePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, 1000);

            }
        });
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                askCameraPermission();
                //Toast.makeText(OverviewActivity.this, "Camera clicked ", Toast.LENGTH_LONG ).show();

            }
        });



    }

    private void notificationsShowCheck(String message, String barName){
        DocumentReference documentReference = firestore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                String notificationStatus = value.getString("notifications");


                if (notificationStatus != null && notificationStatus.equals("on")) {

                    if (waterProgress.getProgress() >= 90 && foodProgress.getProgress() >= 100){
                        SettingsActivity.showNotificationFromSettings(OverviewActivity.this, "Both Goals Reached for Today!", 100);

                        documentReference.update("notifications", "shown")
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.e(LOG_TAG, "Succes on updating notification status");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Log.e(LOG_TAG, "Failed to update notification status", e);
                                    }
                                });
                    } else if(waterProgress.getProgress() >= 90 && barName.equals("water")){
                        SettingsActivity.showNotificationFromSettings(OverviewActivity.this, message, 100);
                        // Update "notifications" attribute to "shown"

                    } else if (foodProgress.getProgress() >= 80 && barName.equals("food")) {
                        SettingsActivity.showNotificationFromSettings(OverviewActivity.this, message, 100);
                    }

                }

            }
        });
    }

    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);

        }else{
            openCamera();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.welcome_back_anim);
        TextView usernameTV = findViewById(R.id.welcomeText);
        usernameTV.startAnimation(animation);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == CAMERA_PERMISSION_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {

        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, CAMERA_REQUEST_CODE);
        //Toast.makeText(this, "Camera Open", Toast.LENGTH_SHORT).show();

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();

                //profilePic.setImageURI(imageUri);

                uploadImage(imageUri);

            }
        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap cameraImage = (Bitmap) extras.get("data");
                if (cameraImage != null) {

                    uploadCapturedImage(cameraImage);
                }
            }
        }



    }

    private void uploadCapturedImage(Bitmap image) {
        Uri imageUri = getImageUri(this, image);

        if (imageUri == null) {
            Toast.makeText(this, "Failed to create image URI", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = firebaseAuth.getUid();
        if (userID == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
            return;
        }

        StorageReference userStorageReference = storageReference.child("users/" + userID + "/profile.jpg");

        userStorageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                userStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profilePic);
                        Toast.makeText(OverviewActivity.this, "Uploaded image!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OverviewActivity.this, "Couldn't upload image: " + e, Toast.LENGTH_LONG).show();
                Log.i(LOG_TAG, e.toString());
            }
        });
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "profile", null);
        return Uri.parse(path);
    }


    private void uploadImage(Uri imageUri) {

        String userID = firebaseAuth.getUid();
        if (userID == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
            return;
        }


        StorageReference userStorageReference = storageReference.child("users/" + userID + "/profile.jpg");

        userStorageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Toast.makeText(OverviewActivity.this,"Uploaded image!", Toast.LENGTH_LONG).show();

                userStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profilePic);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OverviewActivity.this,"Couldnt upload image :( " + e , Toast.LENGTH_LONG).show();
                Log.i(LOG_TAG, e.toString());
            }
        });

    }

    public void addWater(View view) {
        DocumentReference documentReference = firestore.collection("users").document(userID);


        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Long currentWater = documentSnapshot.getLong("water");
                        int currentLevel = currentWater != null ? currentWater.intValue() : 0;


                        int newLevel = currentLevel + 10;


                        documentReference.update("water", FieldValue.increment(10))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Animate the ProgressBar from the current to the new level
                                        ProgressBarAnimation anim = new ProgressBarAnimation(waterProgress, currentLevel, newLevel);
                                        anim.setDuration(1000); // Set the animation duration
                                        waterProgress.startAnimation(anim);
                                        notificationsShowCheck("Daily water intake complete!", "water");
                                        //Toast.makeText(OverviewActivity.this, "Added 10 to water", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(LOG_TAG, "Error adding water", e);
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "Error fetching water level", e);
                    }
                });
    }

    public void addFood(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.food_popup_layout, null);
        builder.setView(dialogView);

        ListView listView = dialogView.findViewById(R.id.foodListView);

        String[] foodItems = {"Chicken & Rice - 480kcal", "Some Fruit - 200kcal", "Coffee - 100kcal"};
        int[] caloricIntake = {24, 10, 5};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, foodItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int calories = caloricIntake[position];
                updateFoodBar(calories);
                alertDialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    public void updateFoodBar(int calories){
        DocumentReference documentReference = firestore.collection("users").document(userID);


        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Long currentFood = documentSnapshot.getLong("calories");
                        int currentLevel = currentFood != null ? currentFood.intValue() : 0;


                        int newLevel = currentLevel + calories;


                        documentReference.update("calories", FieldValue.increment(calories))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        ProgressBarAnimation anim = new ProgressBarAnimation(foodProgress, currentLevel, newLevel);
                                        anim.setDuration(1000);
                                        foodProgress.startAnimation(anim);
                                        notificationsShowCheck("Daily Food Intake Complete!", "food");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(LOG_TAG, "Error adding food", e);
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "Error fetching food level", e);
                    }
                });
    }
}