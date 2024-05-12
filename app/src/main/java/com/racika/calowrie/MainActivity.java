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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    EditText emailET;
    EditText passwordET;

    private FirebaseAuth firebaseAuth;
    private static final String LOG_TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailET = findViewById(R.id.loginEmail);
        passwordET = findViewById(R.id.loginPass);

        firebaseAuth = FirebaseAuth.getInstance();


    }

    public void register(View view) {

        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);

    }

    public void login(View view) {

        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();

        if(!email.isEmpty() && !password.isEmpty()){
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        Log.i(LOG_TAG, "Succesfully logged in user"+ email);
                        overviewPage();

                    }else{
                        Log.i(LOG_TAG, "Something went wrong while loging in user");
                        Toast.makeText(MainActivity.this,"Something went wrong while loging in user : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            Toast.makeText(MainActivity.this,"All fields are required",Toast.LENGTH_LONG).show();
        }
    }

    public void overviewPage(){

        Intent intent = new Intent(this, OverviewActivity.class);
        startActivity(intent);

    }

}