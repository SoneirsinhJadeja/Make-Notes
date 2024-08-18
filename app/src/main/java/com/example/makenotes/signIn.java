package com.example.makenotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class signIn extends AppCompatActivity {

    EditText ET_Email, ET_Pass;
    Button btn;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    CheckBox Ckbox;
    SharedPreferences sharedPreferences, sharedPreferences_Universer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize variables
        ET_Email = findViewById(R.id.Email);
        ET_Pass = findViewById(R.id.Password);
        btn = findViewById(R.id.submit);
        progressBar = findViewById(R.id.progressBar);
        Ckbox = findViewById(R.id.remember_me_checkbox);

        // Initialize Firebase Auth and SharedPreferences
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("NotesMySharedPref", MODE_PRIVATE);

        String savedUsername = sharedPreferences.getString("Email", "");
        String savedPassword = sharedPreferences.getString("Password", "");

        ET_Email.setText(savedUsername);
        ET_Pass.setText(savedPassword);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                String Email = ET_Email.getText().toString();
                String Pass = ET_Pass.getText().toString();

                if (Email.isEmpty() || Pass.isEmpty()) {
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                    Toast.makeText(signIn.this, "Field is Empty", Toast.LENGTH_LONG).show();
                } else {
                    if (Ckbox.isChecked()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("Email", Email);
                        editor.putString("Password", Pass);
                        editor.apply();
                    } else {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("Email");
                        editor.remove("Password");
                        editor.apply();
                    }

                    mAuth.signInWithEmailAndPassword(Email, Pass)
                            .addOnCompleteListener(signIn.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE); // Hide progress bar

                                    if (task.isSuccessful()) {
                                        saveUserAuth(Email, Pass);
                                        Intent intent = new Intent(signIn.this, MainActivity.class);
                                        intent.putExtra("Email", getUserNameFromEmail(Email));
                                        startActivity(intent);
                                        finish();  // Close the sign-in activity
                                    } else {
                                        Toast.makeText(signIn.this, "Login Unsuccessful: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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

    private void alreadyAuth() {
        sharedPreferences_Universer = getSharedPreferences("Notes-CheckUserExists", MODE_PRIVATE);
        String Email = sharedPreferences_Universer.getString("Email", "");
        String Pass = sharedPreferences_Universer.getString("Password", "");

        if (!Pass.isEmpty() && !Email.isEmpty()) {
            Intent intent = new Intent(signIn.this, MainActivity.class);
            intent.putExtra("Email", getUserNameFromEmail(Email));
            startActivity(intent);
            finish();  // Finish MainActivity to prevent going back to it
        }
    }

    private void saveUserAuth(String Email, String Pass) {
        SharedPreferences.Editor Editer = sharedPreferences_Universer.edit();
        Editer.putString("Email", Email);
        Editer.putString("Password", Pass);
        Editer.apply();
    }

    private String getUserNameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }
}
