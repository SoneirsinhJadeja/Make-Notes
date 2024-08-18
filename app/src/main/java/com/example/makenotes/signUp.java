package com.example.makenotes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signUp extends AppCompatActivity {

    FirebaseFirestore db;
    TextView signIn;
    EditText ET_Email, ET_Pass, ET_Name;
    Button btn;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize variables
        db = FirebaseFirestore.getInstance();
        signIn = findViewById(R.id.signIn);
        ET_Name = findViewById(R.id.Name);
        ET_Email = findViewById(R.id.Email);
        ET_Pass = findViewById(R.id.Password);
        btn = findViewById(R.id.submit);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();

        // Set onClickListener for sign-in text view
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(signUp.this, signIn.class);
                startActivity(intent);
            }
        });

        // Set onClickListener for sign-up button
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an instance of Store_User_Details and fetch data from EditText fields
                String name = ET_Name.getText().toString();
                String email = ET_Email.getText().toString();
                String pass = ET_Pass.getText().toString();



                // Validate the input fields
                if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(signUp.this, "Field is Empty", Toast.LENGTH_LONG).show();
                } else if (!email.contains("@")) {
                    Toast.makeText(signUp.this, "Please enter a valid email containing '@'", Toast.LENGTH_SHORT).show();
                } else {
                    // Activate progress bar
                    progressBar.setVisibility(View.VISIBLE);

                    // Create a new user with Firebase Authentication
                    mAuth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(signUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(signUp.this, "Account created successfully", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.INVISIBLE);

                                        // Store user email in Firestore
                                        Map<String, Object> StoreEmail = new HashMap<>();
                                        StoreEmail.put("D_Email", getUserNameFromEmail(email));

                                        Map<String, Object> userStoreDetails = new HashMap<>();
                                        userStoreDetails.put("D_Email", getUserNameFromEmail(email));
                                        userStoreDetails.put("D_Name", name);
                                        userStoreDetails.put("D_Password", pass);

                                        Map<String, Object> NoteCounter = new HashMap<>();
                                        NoteCounter.put("Counter", 0);

                                        db.collection("users")
                                                .document(getUserNameFromEmail(email))
                                                .set(StoreEmail)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        db.collection(getUserNameFromEmail(email))
                                                                .document("User Detail")
                                                                .set(userStoreDetails)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {}
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(signUp.this, "Data Not store successfully...", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });

                                                        db.collection(getUserNameFromEmail(email))
                                                                .document("Notes Counter")
                                                                .set(NoteCounter)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {

                                                                        saveUserAuth(email, pass);

                                                                        Toast.makeText(signUp.this, "Data stored successfully: " + getUserNameFromEmail(email), Toast.LENGTH_SHORT).show();
                                                                        // Pass user details to another activity
                                                                        Intent intent = new Intent(signUp.this, MainActivity.class);
                                                                        intent.putExtra("Email", getUserNameFromEmail(email));
                                                                        startActivity(intent);
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(signUp.this, "Data Not store successfully...", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
//                                                        Toast.makeText(signUp.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        ET_Email.setText(e.getMessage());
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(signUp.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        alreadyAuth();
    }
    private void alreadyAuth(){
        sharedPreferences = getSharedPreferences("Notes-CheckUserExists", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String Email = sharedPreferences.getString("Email", "");
        String Pass = sharedPreferences.getString("Password", "");
        editor.apply();

        if (!Pass.equals("") && !Email.equals("")) {
            Intent intent = new Intent(signUp.this, MainActivity.class);
            intent.putExtra("Email", getUserNameFromEmail(Email));
            startActivity(intent);
        }
    }
    private void saveUserAuth(String Email, String Pass){
        sharedPreferences = getSharedPreferences("Notes-CheckUserExists", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Email", Email);
        editor.putString("Password", Pass);
        editor.apply();
    }

    // Helper method to extract username from email
    private String getUserNameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }
}