package com.racika.calowrie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegisterActivity.class.getName();

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    String userID;
    EditText usernameET;
    EditText emailET;

    EditText ageET;
    EditText passwordET;
    EditText passwordAgainET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameET = findViewById(R.id.registerName);
        emailET = findViewById(R.id.registerEmail);
        ageET = findViewById(R.id.registerAge);
        passwordET = findViewById(R.id.registerPassword);
        passwordAgainET = findViewById(R.id.registerPasswordRepeat);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


    }

    public void cancel(View view) {

        finish();

    }

    public void registerUser(View view) {

        Log.i(LOG_TAG, "Inside the registerUser function!");

        String username = usernameET.getText().toString();
        String email = emailET.getText().toString();
        String age = ageET.getText().toString();
        String password = passwordET.getText().toString();
        String passwordAgain = passwordAgainET.getText().toString();

        if (!username.isEmpty() && !email.isEmpty() && !age.isEmpty() && !password.isEmpty() && !passwordAgain.isEmpty()){
            if(password.equals(passwordAgain)){

                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            Log.i(LOG_TAG, "Succesful register of " + email);
                            userID = firebaseAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = firestore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();

                            user.put("username", username);
                            user.put("email", email);
                            user.put("age", age);
                            user.put("preferedTheme", "default");
                            user.put("notifications", "off");
                            user.put("water", null);
                            user.put("calories", null);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    if (task.isSuccessful()){
                                        //Log.i(LOG_TAG, "Succesfully added information about user to firestore cloud");
                                    }
                                }
                            });
                            Toast.makeText(RegisterActivity.this,"Succes! Please log in :D ", Toast.LENGTH_LONG).show();
                            loginPage();
                        }else{
                            Log.i(LOG_TAG, "Something went wrong while creating user");
                            Toast.makeText(RegisterActivity.this,"Something went wrong while creating user : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                });

            }else{
                Log.i(LOG_TAG, "Passwords not matching");
            }
        }else{
            Log.i(LOG_TAG, "Not all required fields given");
        }

    }

    public void loginPage(){
        Intent login = new Intent(this, MainActivity.class);
        startActivity(login);
    }

}